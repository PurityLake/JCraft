package com.jcraft.collision;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;

public class GameObject extends ModelInstance implements Disposable {
    public final btCollisionObject body;
    private boolean moving;

    public GameObject(Model model, String node, btCollisionShape shape) {
        super(model, node);
        body = new btCollisionObject();
        body.setCollisionShape(shape);
    }

    public boolean isMoving() {
        return moving;
    }

    public void setIsMoving(boolean value) {
        moving = value;
    }

    @Override
    public void dispose() {
        body.dispose();
    }
}
