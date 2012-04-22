package org.urish.banjii.model;

import java.util.HashSet;
import java.util.Set;

public class Player {
	private final int id;
	private boolean visible;
	private double x;
	private double y;

	private final Set<PlayerListener> listeners = new HashSet<PlayerListener>();

	public Player(int id) {
		super();
		this.id = id;
		this.visible = true;
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
}
