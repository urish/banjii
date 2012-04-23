package org.urish.banjii.model;

import java.util.logging.Logger;

import org.urish.banjii.api.CameraListener;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;

public class CameraManager implements CameraListener {
	public static final CameraManager instance = new CameraManager();

	private static final Logger logger = Logger.getLogger(CameraManager.class.getName());
	
	private final PlayerManager playerManager = PlayerManager.instance;

	/**
	 * Translates a given POSIT matrix to a standard 3d affine transformation
	 * matrix.
	 * 
	 * @param matrix
	 *            A 12-point POSIT matrix.
	 */
	private Matrix4 positMatrixToAffineMatrix(double[] matrix) {
		return new Matrix4(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5], matrix[6], matrix[7],
				matrix[8], matrix[9], matrix[10], matrix[11], 0, 0, 0, 1);

	}

	public void onCameraMovement(int cameraId, int markerId, double[] matrix) {
		Matrix4 affineMatrix = positMatrixToAffineMatrix(matrix);
		logger.info("Camera " + cameraId + " detected marker " + markerId + " at " + affineMatrix);
		Matrix4 cameraMatrix = new Matrix4();
		cameraMatrix.multiplyLocal(1/100.);
		
		Player player = playerManager.getPlayers().get(markerId);
		if (player != null) {
			affineMatrix.multiplyLocal(cameraMatrix);
			Transform objectTransform = new Transform();
			objectTransform.fromHomogeneousMatrix(affineMatrix);
			objectTransform.translate(0, 0, -2.5);
			Vector3 point = new Vector3(0, 0, 0);
			objectTransform.applyForward(point);
			player.setX(point.getX());
			player.setY(point.getZ());
		}
	}
}
