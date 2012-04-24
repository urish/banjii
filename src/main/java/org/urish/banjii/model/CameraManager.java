package org.urish.banjii.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.urish.banjii.api.CameraListener;

import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;

public class CameraManager implements CameraListener {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());
	private static final int MAX_CAMERAS = 2;

	private final PlayerManager playerManager = PlayerManager.instance;
	private final List<Camera> cameras = new ArrayList<Camera>();

	private CameraManager() {
		super();
		for (int i = 0; i < MAX_CAMERAS; i++) {
			Camera camera = new Camera(i);
			camera.setPosition(new Vector3(0, 0, -2.5));
			camera.setScale(1 / 100.);
			cameras.add(camera);
		}
	}

	public void onCameraMovement(int cameraId, int markerId, double[] matrix) {
		PositMatrix posit = PositMatrix.load(matrix);
		logger.info("Camera " + cameraId + " detected marker " + markerId + " at " + posit);
		Camera camera = cameras.get(cameraId);
		Player player = playerManager.getPlayers().get(markerId);

		if (camera != null) {
			if (camera.isCalibrating()) {
				calibrateCamera(camera, markerId, posit);
			} else if (player != null) {
				updateCamera(camera, player, posit);
			}
		}

	}

	private void calibrateCamera(Camera camera, int markerId, PositMatrix posit) {
		if ((markerId >= 0) && (markerId <= 1)) {
			camera.getCalibrationMatrices()[markerId] = posit;
		}
	}

	private void updateCamera(Camera camera, Player player, PositMatrix posit) {
		posit.multiplyTranslation(camera.getScale());
		posit.addTranslation(camera.getPosition());
		Transform objectTransform = posit.toTransform();
		Vector3 point = new Vector3(0, 0, 0);
		objectTransform.applyForward(point);
		player.setX(point.getX());
		player.setY(point.getZ());
	}
}
