package com.jcraft.collision;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;

public record GameObjectConstructor(Model model, String node, btCollisionShape shape) implements Disposable {
    public GameObject construct() {
        return new GameObject(model, node, shape);
    }

    @Override
    public void dispose() {
        shape.dispose();
    }
}
