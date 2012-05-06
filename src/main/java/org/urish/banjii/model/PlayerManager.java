package org.urish.banjii.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
	public static final PlayerManager instance = new PlayerManager();

	private static final int MAX_PLAYERS = 8;

	private final List<Player> players = new ArrayList<Player>();

	private PlayerManager() {
		super();
		for (int i = 0; i < MAX_PLAYERS; i++) {
			Player player = new Player(i);
			players.add(player);
		}
	}
	
	public List<Player> getPlayers() {
		return players;
	}

	public void addListener(PlayerListener listener) {
		for (Player player: players) {
			player.addListener(listener);
		}
	}

	public void removeListener(PlayerListener listener) {
		for (Player player: players) {
			player.removeListener(listener);
		}
	}
}
