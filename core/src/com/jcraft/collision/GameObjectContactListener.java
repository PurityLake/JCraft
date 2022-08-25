package com.jcraft.collision;

import com.badlogic.gdx.physics.bullet.collision.ContactListener;

import java.util.ArrayList;

public class GameObjectContactListener extends ContactListener {
    private ArrayList<GameObject> instances;

    public GameObjectContactListener(ArrayList<GameObject> instances) {
        super();
        this.instances = instances;
    }

    @Override
    public boolean onContactAdded(int userValue0, int partId0, int index0, int userValue1, int partId1, int index1) {
        if (userValue1 == 0) {
            instances.get(userValue0).setIsMoving(false);
        } else if (userValue0 == 0) {
            instances.get(userValue1).setIsMoving(false);
        }
        return true;
    }
}
