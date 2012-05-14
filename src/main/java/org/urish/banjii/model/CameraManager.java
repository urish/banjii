package org.urish.banjii.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Logger;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;

public class CameraManager {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());
	private static final int MAX_CAMERAS = 8;

	private final PlayerManager playerManager = PlayerManager.instance;
	private final List<Camera> cameras = new ArrayList<Camera>();
	private final static String CAMERA_PROPERTIES_FILE_PATH = "camera.properties";

	private final static float CAMERA_TO_METERS = 1 / 300f;
	private final static ReadOnlyVector3 ROOM_SIZE = new Vector3(4.30f, 2.20f, 3.30f);

	private Properties cameraProperties;

	private CameraManager() {
		super();
		cameraProperties = new Properties();
		try {
			cameraProperties.load(new FileInputStream(CAMERA_PROPERTIES_FILE_PATH));
		} catch (IOException e) {
			System.out.println("Could not load camera properties file at: " + CAMERA_PROPERTIES_FILE_PATH);
		}
		for (int i = 0; i < MAX_CAMERAS; i++) {
			Camera camera = new Camera(i);
			Vector3 savedPosition = stringToVector(cameraProperties.getProperty("camera" + i + ".position", "0,0,0"));
			Matrix3 savedOrientation = stringToMatrix(cameraProperties.getProperty("camera" + i + ".orientation",
					"0,0,1,0,1,0,1,0,0"));
			camera.setPosition(savedPosition);
			camera.setOrientation(savedOrientation);
			cameras.add(camera);
		}
	}

	private Vector3 stringToVector(String vectorString) {
		String[] coordinates = vectorString.split(",");
		Vector3 vector = new Vector3();
		vector.setX(Double.valueOf(coordinates[0]));
		vector.setY(Double.valueOf(coordinates[1]));
		vector.setZ(Double.valueOf(coordinates[2]));

		return vector;
	}

	private Matrix3 stringToMatrix(String matrixData) {
		String[] parts = matrixData.split(",");
		return new Matrix3(Double.valueOf(parts[0]), Double.valueOf(parts[1]), Double.valueOf(parts[2]),
				Double.valueOf(parts[3]), Double.valueOf(parts[4]), Double.valueOf(parts[5]), Double.valueOf(parts[6]),
				Double.valueOf(parts[7]), Double.valueOf(parts[8]));
	}

	private String vectorToString(ReadOnlyVector3 vector) {
		StringBuffer vectorString = new StringBuffer();
		vectorString.append(vector.getX() + "," + vector.getY() + "," + vector.getZ());

		return vectorString.toString();
	}

	private String matrixToString(ReadOnlyMatrix3 matrix) {
		double[] values = matrix.toArray(null);
		return values[0] + "," + values[1] + "," + values[2] + "," + values[3] + "," + values[4] + "," + values[5] + ","
				+ values[6] + "," + values[7] + "," + values[8];
	}

	/**
	 * the scale of the sliders runs between [0...100]
	 * 
	 * @param camera
	 * @param newPosition
	 * @param orientationMatrix
	 */
	public void updateCameraParameters(Camera camera, Vector3 newPosition, Matrix3 orientationMatrix) {
		camera.setPosition(newPosition);
		camera.setOrientation(orientationMatrix);

		cameraProperties.setProperty("camera" + camera.getId() + ".position", vectorToString(camera.getPosition()));
		cameraProperties.setProperty("camera" + camera.getId() + ".orientation", matrixToString(camera.getOrientation()));
		try {
			cameraProperties.store(new FileOutputStream(CAMERA_PROPERTIES_FILE_PATH), null);
		} catch (IOException e) {
			System.out.println("Could not save camera properties file at: " + CAMERA_PROPERTIES_FILE_PATH);
		}
	}

	/**
	 * the scale of the slider runs between [0...360]
	 * 
	 * @param camera
	 * @param orientation
	 */
	public void updateCameraOrientation(Camera camera, Vector3 orientation) {

	}

	public void updateCameraConnection(int cameraId) {
		// logger.info("Camera " + cameraId +
		// " found no markers but is still connected");
		Camera camera = cameras.get(cameraId);
		if (camera != null) {
			camera.setLastConnectedTime(new Date().getTime());
		}
	}

	public void onMarkerMovement(int cameraId, int markerId, double[] matrix) {
		PositMatrix posit = PositMatrix.load(matrix);
		logger.info("Camera " + cameraId + " detected marker " + markerId + " at " + posit.getTranslation());
		Camera camera = cameras.get(cameraId);
		Player player = playerManager.getPlayers().get(markerId);

		if (camera != null) {
			camera.setLastActiveTime(new Date().getTime());
			if (!camera.isMuted()) {
				updateCamera(camera, player, posit);
			}
		}
	}

	private void updateCamera(Camera camera, Player player, PositMatrix posit) {
		Transform cameraTransform = new Transform();
		Vector3 cameraScale = new Vector3(RealWorldParameters.ROOM_LENGTH / ROOM_SIZE.getX(),
		RealWorldParameters.ROOM_HEIGHT / ROOM_SIZE.getY(), RealWorldParameters.ROOM_WIDTH / ROOM_SIZE.getZ());
		cameraScale.multiplyLocal(CAMERA_TO_METERS);
		cameraTransform.setScale(cameraScale);
		cameraTransform.setTranslation(camera.getPosition());
		cameraTransform.setRotation(camera.getOrientation());
		Vector3 point = new Vector3(posit.getTranslation());
		cameraTransform.applyForward(point);
		Matrix3 playerRotation = new Matrix3();
		cameraTransform.getMatrix().multiply(posit.getRotation(), playerRotation);
		ReadOnlyVector2 playerPosition = new Vector2(point.getX(), point.getZ());
		MarkerInfo detectedPosition = new MarkerInfo(playerPosition, new Date());
		camera.addMarkerHistory(player, detectedPosition);
//		camera.printPlayerMarkerHistory(player);
		player.setX(playerPosition.getX());
		player.setY(playerPosition.getY());
		player.setAngle(playerRotation.toAngles(null)[1]);
		player.setLastUpdated(new Date());
	}

	public List<Camera> getCameras() {
		return cameras;
	}

	// maximal time difference, in milliseconds, in which to take a marker
	// recording into account
	final static long MAXIMAL_TIME_DIFF = 500;

	/**
	 * 
	 * Calculates a player's position by taking into account all cameras that
	 * detected it within the MAXIMAL_TIME_DIFF timewindow, using a weighted
	 * average algorithm.
	 * 
	 * @param player
	 *            the player for which we are calculating the weighted avergae
	 *            position
	 * 
	 * */
	public ReadOnlyVector2 getWeightedAveragePosition(Player player) {

		final long currentTime = new Date().getTime();

		double allCamerasAccumulatingX = 0;
		double allCamerasAccumulatingY = 0;
		double allCamerasWeight = 0;

		for (Camera camera : cameras) {
			double singleCameraWeightedAvgX = 0;
			double singleCameraWeightedAvgY = 0;
			double singleCameraWeight = 0;

			// fetch a single camera's history for a single player
			Queue<MarkerInfo> markerInfoQueue = camera.getHistory().get(player);
			if (markerInfoQueue != null) {
				// calculate the camera's weight - based on the "freshness" of
				// its marker timestamp and other data
				singleCameraWeight = calculateCameraWeight(markerInfoQueue.peek(), currentTime);

				Iterator<MarkerInfo> it = markerInfoQueue.iterator();
				double accumulatingX = 0;
				double accumulatingY = 0;
				double weightCounter = 0;
				while (it.hasNext()) {
					MarkerInfo iteratorValue = it.next();
					// work only with marker info instances which are "fresh"
					// enough
					double timeDiff = currentTime - iteratorValue.getTimestamp().getTime();
					if (timeDiff < MAXIMAL_TIME_DIFF) {
						double weight = MAXIMAL_TIME_DIFF - timeDiff;
						accumulatingX += iteratorValue.getPosition().getX() * weight;
						accumulatingY += iteratorValue.getPosition().getY() * weight;
						weightCounter += weight;
					} else {
						// if we passed the allowed time difference, we don't
						// care about the older results
						break;
					}
				}

				singleCameraWeightedAvgX = accumulatingX / weightCounter;
				singleCameraWeightedAvgY = accumulatingY / weightCounter;

				System.out.println("weighted X for camera " + camera.getId() + "= " + singleCameraWeightedAvgX);
				System.out.println("weighted Y for camera " + camera.getId() + "= " + singleCameraWeightedAvgY);
				System.out.println("====================================================");
			}

			allCamerasAccumulatingX += singleCameraWeightedAvgX * singleCameraWeight;
			allCamerasAccumulatingY += singleCameraWeightedAvgY * singleCameraWeight;
			allCamerasWeight += singleCameraWeight;
		}

		double allCamerasWeightedAvgX = allCamerasAccumulatingX / allCamerasWeight;
		double allCamerasWeightedAvgY = allCamerasAccumulatingY / allCamerasWeight;

		System.out.println("weighted X from ALL cameras = " + allCamerasWeightedAvgX);
		System.out.println("weighted Y from ALL cameras = " + allCamerasWeightedAvgY);
		System.out.println("====================================================");

		ReadOnlyVector2 weightedPlayerPosition = new Vector2(allCamerasWeightedAvgX, allCamerasWeightedAvgY);

		return weightedPlayerPosition;
	}

	private double calculateCameraWeight(MarkerInfo freshestMarker, long currentTime) {
		double timeDiff = currentTime - freshestMarker.getTimestamp().getTime();
		double weight = MAXIMAL_TIME_DIFF - timeDiff;
		return weight;
	}
}
