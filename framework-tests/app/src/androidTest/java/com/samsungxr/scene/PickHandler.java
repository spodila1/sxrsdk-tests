package com.samsungxr.scene;

import net.jodah.concurrentunit.Waiter;

import com.samsungxr.SXRPicker;
import com.samsungxr.SXRNode;
import com.samsungxr.IPickEvents;
import com.samsungxr.utility.Log;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
     * PickHandler records the number of times onEnter, onExit and onInside
     * are called for each scene object picked. Records number of onNoPick
     * and onPick events.
     */
class PickHandler implements IPickEvents
{
    public final class PickInfo
    {
        public SXRNode PickedObj;
        public int  NumEnter;
        public int  NumExit;
        public int  NumInside;
        public ArrayList<Vector3f> EnterHits;
        public ArrayList<Vector3f> InsideHits;
        public ArrayList<Vector2f> EnterTexCoords;
        public ArrayList<Vector2f> InsideTexCoords;
        public PickInfo()
        {
            PickedObj = null;
            NumEnter = 0;
            NumExit = 0;
            NumInside = 0;
            EnterHits = new ArrayList<Vector3f>();
            InsideHits = new ArrayList<Vector3f>();
            EnterTexCoords = new ArrayList<Vector2f>();
            InsideTexCoords = new ArrayList<Vector2f>();
        }
    }

    private Map<String, PickInfo> mPicked = null;
    private int mNumNoPick = 0;
    private int mNumPick = 0;
    Waiter mWaiter;


    public PickHandler(Waiter waiter)
    {
        mWaiter = waiter;
        mPicked = new HashMap<String, PickInfo>();
    }

    public void onEnter(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
    {
        String name = sceneObj.getName();
        if (name != null)
        {
            PickInfo p = mPicked.get(name);

            if (p == null)
            {
                p = new PickInfo();
                p.PickedObj = sceneObj;
                p.EnterHits.add(new Vector3f(pickInfo.hitLocation[0], pickInfo.hitLocation[1], pickInfo.hitLocation[2]));
                if (pickInfo.textureCoords != null)
                {
                    p.EnterTexCoords.add(new Vector2f(pickInfo.textureCoords[0], pickInfo.textureCoords[1]));
                }
            }
            p.NumEnter++;
            mPicked.put(name, p);
            Log.d("PICK", "onEnter %s %f, %f, %f", name, pickInfo.hitLocation[0], pickInfo.hitLocation[1], pickInfo.hitLocation[2]);
        }
    }

    public void onExit(SXRNode sceneObj)
    {
        String name = sceneObj.getName();
        if (name != null)
        {
            PickInfo p = mPicked.get(name);

            // onEnter or onPick should be called first
            // but sometimes onExit gets called after the
            // scene has been cleared
            if (p != null)
            {
                p.NumExit++;
                Log.d("PICK", "onExit %s", name);
            }
        }
    }

    public void onInside(SXRNode sceneObj, SXRPicker.SXRPickedObject pickInfo)
    {
        String name = sceneObj.getName();
        if (name != null)
        {
            PickInfo p = mPicked.get(name);

            // onEnter or onPick should be called first
            // It puts the PickInfo in the map
            mWaiter.assertNotNull(p);
            p.NumInside++;
            p.InsideHits.add(new Vector3f(pickInfo.hitLocation[0], pickInfo.hitLocation[1], pickInfo.hitLocation[2]));
            if (pickInfo.textureCoords != null)
            {
                p.InsideTexCoords.add(new Vector2f(pickInfo.textureCoords[0], pickInfo.textureCoords[1]));
            }
            Log.d("PICK", "onInside %s %f, %f, %f", name, pickInfo.hitLocation[0], pickInfo.hitLocation[1], pickInfo.hitLocation[2]);
        }
    }

    public void onNoPick(SXRPicker picker) {
        ++mNumNoPick;
    }

    public void onPick(SXRPicker picker)
    {
        int npick = 0;
        for (SXRPicker.SXRPickedObject pick : picker.getPicked())
        {
            if (pick == null)
            {
                continue;
            }
            SXRNode sceneObj = pick.hitObject;
            String name = sceneObj.getName();
            if (name != null)
            {
                PickInfo p = mPicked.get(name);

                if (p == null)
                {
                    p = new PickInfo();
                    p.PickedObj = sceneObj;
                }
                mWaiter.assertNotNull(p);
                ++npick;
                mPicked.put(name, p);
                Log.d("PICK", "onPick %s", name);
            }
        }
        if (npick > 0)
        {
            ++mNumPick;
        }
    }

    public void checkObject(String name, SXRNode pickedObj, int numEnter, int numExit, int numInside)
    {
        PickInfo p = mPicked.get(name);

        if (numEnter == 0)
        {
            mWaiter.assertNull(p);
            return;
        }
        mWaiter.assertNotNull(p);
        mWaiter.assertEquals(numEnter, p.NumEnter);
        mWaiter.assertEquals(numExit, p.NumExit);
        if (numInside > 0)
        {
            mWaiter.assertTrue(p.NumInside >= 0);
        }
        else
        {
            mWaiter.assertEquals(numInside, p.NumInside);
        }
        if (pickedObj != null)
        {
            mWaiter.assertEquals(pickedObj, p.PickedObj);
        }
    }

    public void countPicks(int numFrames)
    {
        int n = mNumPick + mNumNoPick;

        Log.d("PICK", "countPicks(%d) expected %d", numFrames, n);
        mWaiter.assertTrue(numFrames <= n + 1);
        mWaiter.assertTrue(numFrames >= n - 1);
    }

    public void checkHits(String name, Vector3f[] enterHits, Vector3f[] insideHits)
    {
        PickInfo p = mPicked.get(name);
        mWaiter.assertNotNull(p);
        if (enterHits != null)
        {
            int j = 0;
            mWaiter.assertFalse(p.EnterHits.size() == 0);
            for (Vector3f enterHit : enterHits)
            {
                Vector3f pickHit = p.EnterHits.get(j++);
                Log.d("PICK", "checkHits  %s %f, %f, %f", name, pickHit.x, pickHit.y, pickHit.z);
                mWaiter.assertTrue(pickHit.distance(enterHit) < 0.0001f);
            }
        }
        if (insideHits != null)
        {
            int j = 0;
            mWaiter.assertFalse(p.InsideHits.size() == 0);
            for (Vector3f insideHit : insideHits)
            {
                Vector3f pickHit = p.InsideHits.get(j++);
                mWaiter.assertTrue(pickHit.distance(insideHit) < 0.0001f);
            }
        }
    }

    public void checkTexCoords(String name, Vector2f[] enterTexCoords, Vector2f[] insideTexCoords)
    {
        PickInfo p = mPicked.get(name);
        mWaiter.assertNotNull(p);
        if (enterTexCoords != null)
        {
            int j = 0;
            mWaiter.assertFalse(p.EnterTexCoords.size() == 0);
            for (Vector2f enterTexCoord : enterTexCoords)
            {
                Vector2f pickTexCoord = p.EnterTexCoords.get(j++);
                float d = pickTexCoord.distance(enterTexCoord);
                Log.d("PICK", "checkTexCoords  %s enter distance = %f", name, d);
                mWaiter.assertTrue(d < 0.0001f);
            }
        }
        if (insideTexCoords != null)
        {
            int j = 0;
            mWaiter.assertFalse(p.InsideTexCoords.size() == 0);
            for (Vector2f insideTexCoord : insideTexCoords)
            {
                Vector2f pickTexCoord = p.InsideTexCoords.get(j++);
                float d = pickTexCoord.distance(insideTexCoord);
                Log.d("PICK", "checkTexCoords  %s inside distance = %f", name, d);
                mWaiter.assertTrue(d < 0.0001f);
            }
        }
    }

    public void checkNoHits(String name)
    {
        mWaiter.assertNull(mPicked.get(name));
    }

    public void clearResults()
    {
        mPicked.clear();
        mNumPick = 0;
        mNumNoPick = 0;
    }
}
