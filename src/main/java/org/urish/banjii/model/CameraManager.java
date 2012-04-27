package org.urish.banjii.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class CameraManager {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());
	private static final int MAX_CAMERAS = 2;
	private static final double CALIBRATION_MARKER_DISTANCE = 0.06; /* Meters */

	private final PlayerManager playerManager = PlayerManager.instance;
	private final List<Camera> cameras = new ArrayList<Camera>();

	private CameraManager() {
		super();
		for (int i = 0; i < MAX_CAMERAS; i++) {
			Camera camera = new Camera(i);
			camera.setPosition(new Vector3(0, 1, 2.5));
			camera.setOrientation(new Matrix3(0, 0, 1, 0, 1, 0, 1, 0, 0));
			camera.setScale(1 / 150.);
			cameras.add(camera);
		}
	}

	public void onMarkerMovement(int cameraId, int markerId, double[] matrix) {
		PositMatrix posit = PositMatrix.load(matrix);
		logger.info("Camera " + cameraId + " detected marker " + markerId + " at " + posit);
		Camera camera = cameras.get(cameraId);
		Player player = playerManager.getPlayers().get(markerId);

		if (camera != null) {
			camera.setLastActiveTime(new Date().getTime());
			if (camera.isCalibrating()) {
				calibrateCamera(camera, markerId, posit);
			} else if (player != null) {
				updateCamera(camera, player, posit);
			}
		}

	}

	private void calibrateCamera(Camera camera, int markerId, PositMatrix posit) {
		PositMatrix[] matrices = camera.getCalibrationMatrices();
		double distance = Double.NaN;
		ReadOnlyVector3 marker1Position = null;
		synchronized (matrices) {
			if ((markerId >= 0) && (markerId <= 1)) {
				matrices[markerId] = posit;
			}
			if (matrices[0] != null && (matrices[1] != null)) {
				marker1Position = matrices[0].getTranslation();
				distance = matrices[0].getTranslation().distance(matrices[1].getTranslation());
			}
		}
		if (marker1Position != null) {
			double scale = CALIBRATION_MARKER_DISTANCE / distance;
			logger.info("Camera calibrated, distance scale: " + scale + ", position: " + marker1Position);
			Vector3 cameraPosition = new Vector3(marker1Position);
			cameraPosition.multiplyLocal(scale);
			camera.setScale(scale);
			camera.setPosition(cameraPosition);
			camera.setCalibrating(false);
		}
	}

	private void updateCamera(Camera camera, Player player, PositMatrix posit) {
		Transform cameraTransform = new Transform();
		cameraTransform.setScale(camera.getScale());
		cameraTransform.setTranslation(camera.getPosition());
		cameraTransform.setRotation(camera.getOrientation());
		Vector3 point = new Vector3(posit.getTranslation());
		cameraTransform.applyForward(point);
		player.setX(point.getX());
		player.setY(point.getZ());
	}

	public List<Camera> getCameras() {
		return cameras;
	}

	public void startCalibration(Camera camera) {
		camera.setCalibrating(true);
		PositMatrix[] matrices = camera.getCalibrationMatrices();
		synchronized (matrices) {
			matrices[0] = null;
			matrices[1] = null;
		}
	}
}
