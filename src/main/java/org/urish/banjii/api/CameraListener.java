package org.urish.banjii.api;

public interface CameraListener {
	public void onMarkerMovement(int cameraId, int markerId, double[] matrix);
}
