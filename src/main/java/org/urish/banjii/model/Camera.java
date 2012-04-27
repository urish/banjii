package org.urish.banjii.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class Camera {
	private final int id;
	private final PositMatrix[] calibrationMatrices = new PositMatrix[2];
	private final Set<CameraListener> listeners = new HashSet<CameraListener>();

	private boolean wasActive;
	private boolean wasConnected;

	private boolean calibrating;
	private double scale;
	private long lastConnectedTime;
	private long lastActiveTime;




	private ReadOnlyMatrix3 orientation = new Matrix3();
	private ReadOnlyVector3 position = new Vector3();


	public Camera(int id) {
		super();
		this.id = id;
		this.scale = 1;
	}

	public int getId() {
		return id;
	}

	public boolean isActive() {
		return (new Date().getTime() - getLastActiveTime()) < 1000;
	}

	public boolean isConnected() {
		return isActive() || (new Date().getTime() - getLastConnectedTime()) < 1000;
	}

	public boolean isCalibrating() {
		return calibrating;
	}

	public void setCalibrating(boolean calibrating) {
		if (this.calibrating != calibrating) {
			this.calibrating = calibrating;
			broadcastUpdate();
		}
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		if (this.scale != scale) {
			this.scale = scale;
			broadcastUpdate();
		}
	}

	public long getLastConnectedTime() {
		return lastConnectedTime;
	}

	public void setLastConnectedTime(long lastConnectedTime) {
		this.lastConnectedTime = lastConnectedTime;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		if (this.lastActiveTime != lastActiveTime) {
			this.lastActiveTime = lastActiveTime;
			broadcastUpdate();
		}
	}

	public ReadOnlyVector3 getPosition() {
		return position;
	}

	public void setPosition(ReadOnlyVector3 position) {
		if (!this.position.equals(position)) {
			this.position = position;
			broadcastUpdate();
		}
	}

	public ReadOnlyMatrix3 getOrientation() {
		return orientation;
	}

	public void setOrientation(ReadOnlyMatrix3 orientation) {
		if (!this.orientation.equals(orientation)) {
			this.orientation = orientation;
			broadcastUpdate();
		}
	}

	public PositMatrix[] getCalibrationMatrices() {
		return calibrationMatrices;
	}

	private void broadcastUpdate() {
		for (CameraListener listener : listeners) {
			listener.onCameraUpdate(this);
		}
	}

	public void addListener(CameraListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CameraListener listener) {
		listeners.remove(listener);
	}

	public void update() {
		if (isConnected() != wasConnected) {
			broadcastUpdate();
			wasConnected = isConnected();
		}
		if (isActive() != wasActive) {
			broadcastUpdate();
			wasActive = isActive();
		}
	}

	@Override
	public String toString() {
		return "Camera " + getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj.getClass().isAssignableFrom(Camera.class))) {
			return false;
		}
		return ((Camera) obj).getId() == getId();
	}

	@Override
	public int hashCode() {
		return 13 * id;
	}

}
