package org.urish.banjii.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;

public class CameraManager {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());
	private static final int MAX_CAMERAS = 4;
	private static final double CALIBRATION_MARKER_DISTANCE = 0.16; /* Meters */

	private final PlayerManager playerManager = PlayerManager.instance;
	private final List<Camera> cameras = new ArrayList<Camera>();

	private CameraManager() {
		super();
		for (int i = 0; i < MAX_CAMERAS; i++) {
			Camera camera = new Camera(i);
			camera.setPosition(new Vector3(0, 1, 2.5));
			camera.setOrientation(new Matrix3(0, 0, 1, 0, 1, 0, 1, 0, 0));
			camera.setScale(1 / 200.);
			cameras.add(camera);
		}
		cameras.get(1).setPosition(new Vector3(5, 1, 2.5));
		cameras.get(1).setOrientation(new Matrix3(0, 0, -1, 0, 1, 0, -1, 0, 0));
		cameras.get(2).setPosition(new Vector3(2.5, 1, 0));
		cameras.get(2).setOrientation(new Matrix3(1, 0, 0, 0, 1, 0, 0, 0, 1));
	}

	public void updateCameraConnection(int cameraId)
	{
		logger.info("Camera " + cameraId + " found no markers but is still connected");
		Camera camera = cameras.get(cameraId);
		if (camera != null) {
			camera.setLastConnectedTime(new Date().getTime());
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
				marker1Position = new Vector3(matrices[0].getTranslation());
				distance = matrices[0].getTranslation().distance(matrices[1].getTranslation());
			}
		}
		if (marker1Position != null) {
			double scale = CALIBRATION_MARKER_DISTANCE / distance;
			logger.info("Camera calibrated, distance scale: " + scale + ", position: " + marker1Position);
			Vector3 cameraPosition = new Vector3(marker1Position);
			cameraPosition.multiplyLocal(scale);
			cameraPosition.addLocal(new Vector3(2.5, 2, 2.5));
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
		ReadOnlyVector2 playerPosition = new Vector2(point.getX(), point.getZ());
		camera.addMarkerHistory(player, new MarkerInfo(playerPosition, new Date()));
		player.setX(playerPosition.getX());
		player.setY(playerPosition.getY());
		player.setLastUpdated(new Date());
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
