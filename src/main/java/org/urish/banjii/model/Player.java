package org.urish.banjii.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Player {
	private final int id;
	private boolean visible;
	private double x;
	private double y;
	private double height;
	private double angle;
	private Date lastUpdated;
	private boolean wasActive;
	private boolean playing;

	private final Set<PlayerListener> listeners = new HashSet<PlayerListener>();

	public Player(int id) {
		super();
		this.id = id;
		this.visible = true;
		this.height = 1;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return "Player " + (id + 1);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
			broadcastUpdate();
		}
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		if (this.x != x) {
			this.x = x;
			broadcastUpdate();
		}
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		if (this.y != y) {
			this.y = y;
			broadcastUpdate();
		}
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		if (this.height != height) {
			this.height = height;
			broadcastUpdate();
		}
	}
	
	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		if (this.angle != angle) {
			this.angle = angle;
			broadcastUpdate();
		}
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
		if (lastUpdated.equals(this.lastUpdated)) {
			this.lastUpdated = lastUpdated;
			broadcastUpdate();
		}
	}

	public boolean isActive() {
		if (lastUpdated == null) {
			return false;
		}
		return (new Date().getTime() - lastUpdated.getTime()) < 500;
	}
	
	public boolean isPlaying() {
		if (lastUpdated == null) {
			return false;
		}
		return (new Date().getTime() - lastUpdated.getTime()) < 5000;

	}

	public void update() {
		if (isActive() != wasActive) {
			broadcastUpdate();
			wasActive = isActive();
		}
		if (isPlaying() != playing)
		{
			broadcastUpdate();
			playing = isPlaying();
		}
	}

	private void broadcastUpdate() {
		for (PlayerListener listener : listeners) {
			listener.onPlayerUpdate(this);
		}
	}

	public void addListener(PlayerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PlayerListener listener) {
		listeners.remove(listener);
	}

	@Override
	public String toString() {
		return "Player " + (getId() + 1);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj.getClass().isAssignableFrom(Player.class))) {
			return false;
		}
		return ((Player) obj).getId() == getId();
	}

	@Override
	public int hashCode() {
		return 13 * id;
	}
}
