package org.lwjglb.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjglb.engine.GameItem;
import org.lwjglb.engine.IGameLogic;
import org.lwjglb.engine.MouseInput;
import org.lwjglb.engine.Window;
import org.lwjglb.engine.graph.*;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_POS_STEP = 0.05f;

    private final Vector3f cameraInc;
    private final Camera camera;

    private final Renderer renderer;

    private GameItem[] gameItems;

    private PointLight pointLight;
    private DirectionalLight directionalLight;

    private float lightAngle;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        lightAngle = 180;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        float reflectance = 1f;

        Mesh mesh = OBJLoader.loadMesh("/models/tichonov.obj");
        Texture texture = new Texture("/textures/tih.png");
        Material material = new Material(texture, reflectance);

        mesh.setMaterial(material);
        GameItem gameItem = new GameItem(mesh);
        gameItem.setScale(1.0f);
        gameItem.setPosition(0, 0, 0);
        gameItems = new GameItem[]{gameItem};

        Vector3f lightColour = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(0, 7, 0);
        float lightIntensity = 5.0f;
        pointLight = new PointLight(lightColour, lightPosition, lightIntensity);
        pointLight.setAttenuation(new PointLight.Attenuation(0.0f, 0.0f, 1.0f));

        lightPosition = new Vector3f(-1, 0, 0);
        lightColour = new Vector3f(1, 1, 1);
        directionalLight = new DirectionalLight(lightColour, lightPosition, lightIntensity);
    }

    @Override
    public void input(Window window) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Q)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_E)) {
            cameraInc.y = 1;
        }
        float lightPosZ = pointLight.getPosition().z;
        float  lightPosX = pointLight.getPosition().x;
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            this.pointLight.getPosition().z = lightPosZ + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            this.pointLight.getPosition().z = lightPosZ - 0.1f;
        }else if (window.isKeyPressed(GLFW_KEY_UP)){
            this.pointLight.getPosition().x = lightPosX + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            this.pointLight.getPosition().x = lightPosX - 0.1f;
        }

        if (window.isKeyPressed(GLFW_KEY_Z)) {
            lightAngle -= 1.5f;
            if (lightAngle < -90) {
                lightAngle = -90;
            }
        } else if (window.isKeyPressed(GLFW_KEY_X)) {
            lightAngle += 1.5f;
            if (lightAngle > 90) {
                lightAngle = 90;
            }
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);

        // Update camera based on mouse            
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        // Update directional light direction, intensity and colour
        if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, gameItems, pointLight, directionalLight);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}