package org.urish.banjii;

import org.urish.banjii.model.Player;
import org.urish.banjii.model.PlayerListener;
import org.urish.banjii.model.PlayerManager;

import com.ardor3d.extension.ui.UICheckBox;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UITabbedPane;
import com.ardor3d.extension.ui.UITabbedPane.TabPlacement;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.GridLayout;
import com.ardor3d.extension.ui.layout.GridLayoutData;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.ReadOnlyTimer;

public class UserInterface {
	private final Canvas canvas;
	private final PhysicalLayer physicalLayer;
	private final LogicalLayer logicalLayer;

	private UIHud hud;
	private UIFrame frame;
	private double counter;
	private int frames;
	private UILabel fpsLabel;

	public UserInterface(Canvas canvas, PhysicalLayer physicalLayer, LogicalLayer logicalLayer) {
		super();
		this.canvas = canvas;
		this.physicalLayer = physicalLayer;
		this.logicalLayer = logicalLayer;
	}

	public void init() {
		final UITabbedPane pane = new UITabbedPane(TabPlacement.NORTH);
		pane.add(makeInfoPanel(), "Info");
		pane.add(makeControlsPanel(), "Controls");
		pane.add(makeCamerasPanel(), "Cameras");

		frame = new UIFrame("Banjii Control Panel");
		frame.setContentPanel(pane);
		frame.updateMinimumSizeFromContents();
		frame.layout();
		frame.pack();

		frame.setUseStandin(true);
		frame.setOpacity(1f);
		frame.setLocationRelativeTo(canvas.getCanvasRenderer().getCamera());
		frame.setName("sample");

		hud = new UIHud();
		hud.add(frame);
		hud.setupInput(canvas, physicalLayer, logicalLayer);
	}

	private UIPanel makeInfoPanel() {
		final UIPanel infoPanel = new UIPanel(new RowLayout(false));
		final UILabel topLabel = new UILabel("SoundTracker Project Control");
		fpsLabel = new UILabel("FPS: 0");
		infoPanel.add(topLabel);
		infoPanel.add(fpsLabel);

		return infoPanel;
	}

	private UIComponent makeControlsPanel() {
		final UIPanel controlsPanel = new UIPanel(new GridLayout());
		for (final Player player : PlayerManager.instance.getPlayers()) {
			final UICheckBox playerCheckbox = new UICheckBox(player.getName());
			playerCheckbox.setLayoutData(GridLayoutData.WrapAndGrow);
			playerCheckbox.setSelected(player.isVisible());
			playerCheckbox.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					player.setVisible(playerCheckbox.isSelected());
				}
			});
			player.addListener(new PlayerListener() {
				public void onPlayerUpdate(Player player) {
					playerCheckbox.setSelected(player.isVisible());
				}
			});
			controlsPanel.add(playerCheckbox);
		}
		return controlsPanel;
	}

	private UIComponent makeCamerasPanel() {
		final UIPanel camerasPanel = new UIPanel();
		return camerasPanel;
	}

	public void update(ReadOnlyTimer timer) {
		counter += timer.getTimePerFrame();
		frames++;
		if (counter > 1) {
			final double fps = (frames / counter);
			counter = 0;
			frames = 0;
			fpsLabel.setText("FPS: " + Math.round(fps));
		}
		hud.updateGeometricState(timer.getTimePerFrame());
	}

	public void updateLogicalLayer(ReadOnlyTimer timer) {
		hud.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
	}

	public void render(Renderer renderer) {
		renderer.draw(hud);
	}
}
