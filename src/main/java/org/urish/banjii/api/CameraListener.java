package org.urish.banjii.api;

public interface CameraListener {
	public void onCameraMovement(int cameraId, int markerId, double x, double y, double z, double[] matrix);
}
