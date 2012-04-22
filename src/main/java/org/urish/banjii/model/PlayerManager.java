package org.urish.banjii.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerManager implements PlayerListener {
	public static final PlayerManager instance = new PlayerManager();

	private static final int MAX_PLAYERS = 8;

	private final List<Player> players = new ArrayList<Player>();
	private final Set<PlayerListener> listeners = new HashSet<PlayerListener>();

	private PlayerManager() {
		super();
		for (int i = 0; i < MAX_PLAYERS; i++) {
			Player player = new Player(i);
			player.addListener(this);
			players.add(player);
		}
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public void onPlayerUpdate(Player player) {
		for (PlayerListener listener : listeners) {
			listener.onPlayerUpdate(player);
		}		
	}

	public void addListener(PlayerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PlayerListener listener) {
		listeners.remove(listener);
	}
}
