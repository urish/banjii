package org.urish.banjii.model;

public class Camera {
	private final int id;
	private boolean active;
	private boolean calibrating;
	private double x;
	private double y;

	private Camera(int id) {
		super();
		this.id = id;
		this.active = true;
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

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

}
