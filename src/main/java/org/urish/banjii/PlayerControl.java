package org.urish.banjii;

import org.urish.banjii.model.Player;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.google.common.base.Predicate;

public class PlayerControl {
	private final LogicalLayer layer;
	private final Player player;

	private InputTrigger keyTrigger;

	public PlayerControl(LogicalLayer layer, Player player) {
		this.layer = layer;
		this.player = player;
		registerTriggers(layer);
	}

	public void registerTriggers(LogicalLayer layer) {
		// WASD control
		final Predicate<TwoInputStates> keysHeld = new Predicate<TwoInputStates>() {
			Key[] keys = new Key[] { Key.LEFT, Key.RIGHT, Key.UP, Key.DOWN, Key.PAGEUP_PRIOR, Key.PAGEDOWN_NEXT, Key.COMMA,
					Key.PERIOD };

			public boolean apply(final TwoInputStates states) {
				for (final Key k : keys) {
					if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
						return true;
					}
				}
				return false;
			}
		};

		final TriggerAction moveAction = new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				move(inputStates.getCurrent().getKeyboardState());
			}
		};
		keyTrigger = new InputTrigger(keysHeld, moveAction);
		layer.registerTrigger(keyTrigger);
	}

	public void removeTriggers() {
		if (keyTrigger != null) {
			layer.deregisterTrigger(keyTrigger);
			keyTrigger = null;
		}
	}

	protected double limit(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	protected void move(final KeyboardState keyboardState) {
		double x = player.getX();
		double y = player.getY();
		double height = player.getHeight();
		double angle = player.getAngle();
		if (keyboardState.isDown(Key.UP)) {
			x -= .01;
		}
		if (keyboardState.isDown(Key.DOWN)) {
			x += .01;
		}
		if (keyboardState.isDown(Key.LEFT)) {
			y += .01;
		}
		if (keyboardState.isDown(Key.RIGHT)) {
			y -= .01;
		}
		if (keyboardState.isDown(Key.PAGEUP_PRIOR)) {
			height += .01;
		}
		if (keyboardState.isDown(Key.PAGEDOWN_NEXT)) {
			height -= .01;
		}
		if (keyboardState.isDown(Key.COMMA)) {
			angle -= .01;
		}
		if (keyboardState.isDown(Key.PERIOD)) {
			angle += .01;
		}
		x = limit(x, 0, 5);
		y = limit(y, 0, 5);
		height = limit(height, 0, 1);
		angle = angle % (2 * Math.PI);
		player.setX(x);
		player.setY(y);
		player.setHeight(height);
		player.setAngle(angle);
	}
}
