package org.urish.banjii.model;

public class Camera {
	private final int id;
	private final boolean active;
	private double x;
	private double y;

	private Camera(int id) {
		super();
		this.id = id;
		this.active = true;
	}

}
