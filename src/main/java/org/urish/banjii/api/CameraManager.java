package org.urish.banjii.api;

import java.util.logging.Logger;

import org.urish.banjii.Scene;

public class CameraManager implements CameraListener {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());

	private Scene scene = null;

	public void onCameraMovement(int cameraId, int markerId, double x, double y, double z, double[] matrix) {
		logger.info("Camera " + cameraId + " detected marker " + markerId + " at <" + x + ", " + y + ", " + z + ">");
		if (scene != null) {
			scene.setPlayerPosition(markerId, x/100, y/100, z/100);
		}
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}
}
