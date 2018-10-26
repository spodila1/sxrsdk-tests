/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samsungxr.viewmanager.controls;

import com.samsungxr.SXRAndroidResource;
import com.samsungxr.SXRCameraRig;
import com.samsungxr.SXRContext;
import com.samsungxr.SXRMesh;
import com.samsungxr.SXRNode;
import com.samsungxr.SXRTexture;
import com.samsungxr.tests.R;
import com.samsungxr.animation.SXRAnimation;
import com.samsungxr.animation.SXROnFinish;
import com.samsungxr.animation.SXRRelativeMotionAnimation;
import com.samsungxr.animation.SXRRotationByAxisAnimation;
import com.samsungxr.animation.SXRRotationByAxisWithPivotAnimation;
import com.samsungxr.viewmanager.controls.util.MathUtils;
import com.samsungxr.viewmanager.controls.util.RenderingOrder;
import com.samsungxr.viewmanager.controls.util.Util;

public class Worm extends SXRNode {

    // Chain Data
    private final float CHAIN_DISTANCE_HEAD_MIDDLE = 0.23f;
    private final float CHAIN_DISTANCE_MIDDLE_END = 0.19f;

    private final float CHAIN_SPEED_HEAD_MIDDLE = 0.055f;
    private final float CHAIN_SPEED_MIDDLE_END = 0.065f;
    private final float WORM_INITIAL_Z = -3;
    private final float WORM_INITIAL_Y = -0.9f;
    private float MAX_MOVE_DISTANCE = 14.0f;
    private float MIN_MOVE_DISTANCE = 2f;

    private SXRNode head, middle, end, wormParent;

    private boolean isRotatingWorm = false;

    private SXRAnimation wormParentAnimation;

    private MovementDirection wormDirection = MovementDirection.Up;

    public enum MovementDirection {
        Up, Right, Down, Left
    }

    public Worm(SXRContext sxrContext) {

        super(sxrContext);

        SXRMesh mesh = sxrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(sxrContext, R.raw.worm_mesh_head));
        SXRTexture texture = sxrContext.loadTexture(
                new SXRAndroidResource(sxrContext, R.drawable.worm_head_texture));

        wormParent = new SXRNode(sxrContext);

        head = new SXRNode(sxrContext, mesh, texture);
        head.getTransform().setPosition(0, 0, 0);
        head.getTransform().setScale(0.4f, 0.4f, 0.4f);
        head.getRenderData().setRenderingOrder(RenderingOrder.WORM);
        wormParent.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);

        mesh = sxrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(sxrContext, R.raw.worm_mesh_middle));
        middle = new SXRNode(sxrContext, mesh, texture);
        middle.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        middle.getTransform().setScale(0.4f, 0.4f, 0.4f);
        middle.getRenderData().setRenderingOrder(RenderingOrder.WORM);
        mesh = sxrContext.getAssetLoader().loadMesh(
                new SXRAndroidResource(sxrContext, R.raw.worm_mesh_end));
        end = new SXRNode(sxrContext, mesh, texture);
        end.getTransform().setPosition(0, WORM_INITIAL_Y, WORM_INITIAL_Z);
        end.getTransform().setScale(0.4f, 0.4f, 0.4f);
        end.getRenderData().setRenderingOrder(RenderingOrder.WORM);
        wormParent.addChildObject(head);
        this.addChildObject(wormParent);
        this.addChildObject(middle);
        this.addChildObject(end);
    }

    public void chainMove(SXRContext sxrContext) {

        if (MathUtils.distance(head.getParent(), middle) > CHAIN_DISTANCE_HEAD_MIDDLE) {

            float chainSpeed = CHAIN_SPEED_HEAD_MIDDLE
                    * (float) Util.distance(head.getParent().getTransform(), getSXRContext()
                            .getMainScene().getMainCameraRig().getTransform());

            middle.getTransform().setRotationByAxis(
                    MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
            end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

            float newX = middle.getTransform().getPositionX()
                    + (head.getParent().getTransform().getPositionX() -
                    middle.getTransform().getPositionX()) * chainSpeed;

            float newY = middle.getTransform().getPositionY()
                    + (head.getParent().getTransform().getPositionY() -
                    middle.getTransform().getPositionY()) * chainSpeed;

            float newZ = middle.getTransform().getPositionZ()
                    + (head.getParent().getTransform().getPositionZ() -
                    middle.getTransform().getPositionZ()) * chainSpeed;

            middle.getTransform().setPosition(newX, newY, newZ);
        }
        if (MathUtils.distance(middle, end) > CHAIN_DISTANCE_MIDDLE_END) {

            float chainSpeed = CHAIN_SPEED_MIDDLE_END
                    * (float) Util.distance(head.getParent().getTransform(), getSXRContext()
                            .getMainScene().getMainCameraRig().getTransform());

            middle.getTransform().setRotationByAxis(
                    MathUtils.getYRotationAngle(middle, wormParent), 0, 1, 0);
            end.getTransform().setRotationByAxis(MathUtils.getYRotationAngle(end, middle), 0, 1, 0);

            float newX = end.getTransform().getPositionX() + (middle.getTransform().getPositionX() -
                    end.getTransform().getPositionX()) * chainSpeed;

            float newY = end.getTransform().getPositionY() + (middle.getTransform().getPositionY() -
                    end.getTransform().getPositionY()) * chainSpeed;

            float newZ = end.getTransform().getPositionZ() + (middle.getTransform().getPositionZ() -
                    end.getTransform().getPositionZ()) * chainSpeed;

            end.getTransform().setPosition(newX, newY, newZ);

        }

    }

    public void rotateWorm(MovementDirection movementDirection) {
        if (!isRotatingWorm) {
            isRotatingWorm = true;
            float angle = getRotatingAngle(movementDirection);
            new SXRRotationByAxisAnimation(head, .1f, angle, 0, 1, 0).start(
                    getSXRContext().getAnimationEngine()).setOnFinish(
                    new SXROnFinish() {

                        @Override
                        public void finished(SXRAnimation arg0) {
                            isRotatingWorm = false;
                        }
                    });
        }
    }

    private int getRotatingAngle(MovementDirection movementDirection) {
        int movementDiference = movementDirection.ordinal() - wormDirection.ordinal();
        wormDirection = movementDirection;
        if (movementDiference == 1 || movementDiference == -3) {
            return -90;
        } else if (movementDiference == 2 || movementDiference == -2) {
            return 180;
        } else if (movementDiference == 3 || movementDiference == -1) {
            return 90;
        } else {
            return 0;
        }
    }

    public void moveAlongCameraVector(float duration, float movement) {
        if (wormParentAnimation != null) {
            getSXRContext().getAnimationEngine().stop(wormParentAnimation);
        }
        SXRCameraRig cameraObject = getSXRContext().getMainScene().getMainCameraRig();

        float distance = (float) Util.distance(head.getParent().getTransform(),
                cameraObject.getTransform())
                + movement;
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                head.getParent().getTransform(), distance);

        if (movement < 0
                && MathUtils.distance(cameraObject.getTransform(), newPosition) < MIN_MOVE_DISTANCE)
            return;
        if (movement > 0
                && MathUtils.distance(cameraObject.getTransform(),
                        wormParent.getTransform()) > MAX_MOVE_DISTANCE)
            return;

        wormParentAnimation = new SXRRelativeMotionAnimation(head.getParent(),
                duration, newPosition[0] - head.getParent().getTransform().getPositionX(),
                0,
                newPosition[2] - head.getParent().getTransform().getPositionZ())
                .start(getSXRContext().getAnimationEngine());
    }

    public void rotateAroundCamera(float duration, float degree) {
        if (wormParentAnimation != null) {
            getSXRContext().getAnimationEngine().stop(wormParentAnimation);
        }
        wormParentAnimation = new SXRRotationByAxisWithPivotAnimation(
                head.getParent(), duration, degree, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f)
                .start(getSXRContext().getAnimationEngine());

    }

}
