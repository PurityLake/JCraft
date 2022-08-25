package com.jcraft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.ArrayMap;
import com.jcraft.collision.CollisionFlags;
import com.jcraft.collision.GameObject;
import com.jcraft.collision.GameObjectConstructor;
import com.jcraft.collision.GameObjectContactListener;

import java.util.ArrayList;

public class JCraftGame extends ApplicationAdapter {
	public PerspectiveCamera cam;
	public CameraInputController camController;
	public ModelBatch modelBatch;
	public Model model;
	public Environment environment;
	public ArrayList<GameObject> instances;
	public ArrayMap<String, GameObjectConstructor> constructors;
	public btCollisionConfiguration collisionConfig;
	public btDispatcher dispatcher;
	public GameObjectContactListener listener;
	public float spawnTimer;
	public btBroadphaseInterface broadphase;
	public btCollisionWorld world;

	@Override
	public void create () {
		Bullet.init();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.0f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1.0f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(10.0f, 10.0f,10.0f);
		cam.lookAt(0, 0, 0);
		cam.near = 1.0f;
		cam.far = 300.0f;
		cam.update();

		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		mb.node().id = "ground";
		mb.part("ground", GL30.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
				.box(5f, 1f, 5f);
		mb.node().id = "sphere";
		mb.part("sphere", GL30.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
				.sphere(1f, 1f, 1f, 10, 10);
		mb.node().id = "box";
		mb.part("box", GL30.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
				.box(1f, 1f, 1f);
		mb.node().id = "cone";
		mb.part("cone", GL30.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
				.cone(1f, 2f, 1f, 10);
		mb.node().id = "capsule";
		mb.part("capsule", GL30.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.CYAN)))
				.capsule(0.5f, 2f, 10);
		mb.node().id = "cylinder";
		mb.part("cylinder", GL30.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
				.cylinder(1f, 2f, 1f, 10);
		model = mb.end();

		constructors = new ArrayMap<>(String.class, GameObjectConstructor.class);
		constructors.put("ground", new GameObjectConstructor(model, "ground", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f))));
		constructors.put("sphere", new GameObjectConstructor(model, "sphere", new btSphereShape(0.5f)));
		constructors.put("box", new GameObjectConstructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f))));
		constructors.put("cone", new GameObjectConstructor(model, "cone", new btConeShape(0.5f, 2f)));
		constructors.put("capsule", new GameObjectConstructor(model, "capsule", new btCapsuleShape(.5f, 1f)));
		constructors.put("cylinder", new GameObjectConstructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f))));

		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		broadphase = new btDbvtBroadphase();
		world = new btCollisionWorld(dispatcher, broadphase, collisionConfig);

		instances = new ArrayList<>(2);
		GameObject object = constructors.get("ground").construct();
		instances.add(object);
		world.addCollisionObject(object.body, CollisionFlags.GROUND_FLAG, CollisionFlags.ALL_FLAG);

		listener = new GameObjectContactListener(instances);
	}

	@Override
	public void render () {
		final float delta = Math.min(1.0f/30.0f, Gdx.graphics.getDeltaTime());

		for (GameObject obj : instances) {
			if (obj.isMoving()) {
				obj.transform.trn(0.0f, -delta, 0.0f);
				obj.body.setWorldTransform(obj.transform);
			}
		}

		world.performDiscreteCollisionDetection();

		if ((spawnTimer -= delta) < 0) {
			spawn();
			spawnTimer = 1.5f;
		}

		camController.update();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(cam);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	public void spawn() {
		GameObject obj = constructors.values[1+ MathUtils.random(constructors.size-2)].construct();
		obj.setIsMoving(true);
		obj.transform.setFromEulerAngles(
				MathUtils.random(360.0f),
				MathUtils.random(360.0f),
				MathUtils.random(360.0f));
		obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9.0f, MathUtils.random(-2.5f, 2.5f));
		obj.body.setWorldTransform(obj.transform);
		obj.body.setUserValue(instances.size());
		obj.body.setCollisionFlags(
				obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
		instances.add(obj);
		world.addCollisionObject(obj.body, CollisionFlags.OBJECT_FLAG, CollisionFlags.GROUND_FLAG);
	}

	@Override
	public void dispose () {
		for (GameObject obj : instances) {
			obj.dispose();
		}
		instances.clear();

		for (GameObjectConstructor ctor : constructors.values()) {
			ctor.dispose();
		}
		constructors.clear();

		world.dispose();
		broadphase.dispose();
		dispatcher.dispose();
		collisionConfig.dispose();

		listener.dispose();

		modelBatch.dispose();
		model.dispose();
	}
}
