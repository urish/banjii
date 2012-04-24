package org.urish.banjii.model;

import com.ardor3d.math.Vector3;

public class Camera {
	private final int id;
	private final PositMatrix[] calibrationMatrices = new PositMatrix[2];

	private boolean active;
	private boolean calibrating;
	private double scale;
	private long lastActiveTime;
	private Vector3 position = new Vector3();

	public Camera(int id) {
		super();
		this.id = id;
		this.active = true;
		this.scale = 1;
	}

	public int getId() {
		return id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isCalibrating() {
		return calibrating;
	}

	public void setCalibrating(boolean calibrating) {
		this.calibrating = calibrating;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}

	public PositMatrix[] getCalibrationMatrices() {
		return calibrationMatrices;
	}
}
