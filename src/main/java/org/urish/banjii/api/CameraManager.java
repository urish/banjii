package org.urish.banjii.api;

import java.util.logging.Logger;

import org.urish.banjii.Scene;

public class CameraManager implements CameraListener {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());

	public void onCameraMovement(int cameraId, int markerId, double x, double y, double z, double[] matrix) {
		logger.info("Camera " + cameraId + " detected marker " + markerId + " at <" + x + ", " + y + ", " + z + ">");
		Scene listener = Scene.instance;
		if (listener != null) {
			listener.setPlayerPosition(markerId, x, y, z);
		}
	}	
}
