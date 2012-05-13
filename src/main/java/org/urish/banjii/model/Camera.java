package org.urish.banjii.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class Camera {
	private static int QUEUE_SIZE = 5;
	private final int id;
	private final Map<Player, Queue<MarkerInfo>> history = new HashMap<Player, Queue<MarkerInfo>>(4); // a map of each player to his position history as captured by this camera
	private final Set<CameraListener> listeners = new HashSet<CameraListener>();

	private boolean muted = false;
	private boolean wasActive;
	private boolean wasConnected;

	private long lastConnectedTime;
	private long lastActiveTime;

	private double scale;
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
		return !isMuted() && isActive() || (new Date().getTime() - getLastConnectedTime()) < 1000;
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
			this.position = new Vector3(position);
			broadcastUpdate();
		}
	}

	public ReadOnlyMatrix3 getOrientation() {
		return orientation;
	}

	public void setOrientation(ReadOnlyMatrix3 orientation) {
		if (!this.orientation.equals(orientation)) {
			this.orientation = new Matrix3(orientation);
			broadcastUpdate();
		}
	}

	public void addMarkerHistory(Player player, MarkerInfo markerInfo) {
		Queue<MarkerInfo> playerMarkerHistory = history.get(player);
		// handle first detection
		if (playerMarkerHistory == null)
		{
			playerMarkerHistory = new LinkedList<MarkerInfo>();
		}
		else
		{
			// marker history queue just starting to fill up 
			if (playerMarkerHistory.size() < QUEUE_SIZE)
			{
				playerMarkerHistory.add(markerInfo);
			}
			else
			{
				playerMarkerHistory.poll();
				playerMarkerHistory.add(markerInfo);
			}
		}
		history.put(player, playerMarkerHistory);
	}
	
	public void printPlayerMarkerHistory(Player player)
	{
		Queue<MarkerInfo> playerMarkerHistory = history.get(player);
		if (playerMarkerHistory != null)
		{
			Iterator<MarkerInfo> it=playerMarkerHistory.iterator();
			System.out.println("Printing history queue for player id : "+ player.getId());
	        System.out.println("Size of History Queue : "+ playerMarkerHistory.size());
	        while(it.hasNext())
	        {
	            MarkerInfo iteratorValue= it.next();
	            System.out.println("TIME = " + iteratorValue.getTimestamp());
	            System.out.println("X = " + iteratorValue.getPosition().getX());
	            System.out.println("Y = " + iteratorValue.getPosition().getY());
	        }
	        System.out.println("====================================================");
		}
	}

	public Map<Player, Queue<MarkerInfo>> getHistory() {
		return history;
	}
	
	public void clearHistory() {
		history.clear();
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

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

}
