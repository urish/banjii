package org.urish.banjii.model;

import java.util.Date;

import com.ardor3d.math.type.ReadOnlyVector2;

public class MarkerInfo {
	private final ReadOnlyVector2 position;
	private final Date timestamp;

	public MarkerInfo(ReadOnlyVector2 position, Date timestamp) {
		super();
		this.position = position;
		this.timestamp = timestamp;
	}

	public ReadOnlyVector2 getPosition() {
		return position;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}
