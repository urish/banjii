package org.urish.banjii;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.urish.banjii.model.Camera;
import org.urish.banjii.model.CameraListener;
import org.urish.banjii.model.CameraManager;
import org.urish.banjii.model.Player;
import org.urish.banjii.model.PlayerListener;
import org.urish.banjii.model.PlayerManager;

import com.ardor3d.extension.ui.UIButton;
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
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.util.ReadOnlyTimer;

public class UserInterface {
	private static final ReadOnlyColorRGBA DARK_GREEN = new ColorRGBA(0f, .6f, 0f, 1f);

	private final Canvas canvas;
	private final PhysicalLayer physicalLayer;
	private final LogicalLayer logicalLayer;
	private final CameraManager cameraManager = CameraManager.instance;
	private final Set<Camera> updatedCameras = new HashSet<Camera>();
	private final Map<Camera, CameraListener> cameraListeners = new HashMap<Camera, CameraListener>();

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
		pane.add(makePlayersPanel(), "Players");
		pane.add(makeCamerasPanel(), "Cameras");
		pane.add(makeInfoPanel(), "Info");

		frame = new UIFrame("Banjii Control Panel");
		frame.setContentPanel(pane);
		frame.updateMinimumSizeFromContents();
		frame.layout();
		frame.pack();

		frame.setUseStandin(true);
		frame.setOpacity(1f);
		frame.setHudXY(5, canvas.getCanvasRenderer().getCamera().getHeight() - frame.getLocalComponentHeight() - 5);
		frame.setName("UI");

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

	private UIComponent makePlayersPanel() {
		final UIPanel playersPanel = new UIPanel(new GridLayout());
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
			playersPanel.add(playerCheckbox);
		}
		return playersPanel;
	}

	private UIComponent makeCamerasPanel() {
		final UIPanel camerasPanel = new UIPanel(new GridLayout());
		for (final Camera camera : cameraManager.getCameras()) {
			final UILabel cameraLabel = new UILabel("Cam" + camera.getId());
			final UILabel activeLabel = new UILabel("off");
			activeLabel.setForegroundColor(ColorRGBA.RED);
			final UIButton calibrateButton = new UIButton("Calibrate");
			final UILabel infoLabel = new UILabel("[]");
			calibrateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					cameraManager.startCalibration(camera);
				}
			});
			camerasPanel.add(cameraLabel);
			camerasPanel.add(activeLabel);
			camerasPanel.add(calibrateButton);
			camerasPanel.add(infoLabel);
			infoLabel.setLayoutData(GridLayoutData.WrapAndGrow);

			camera.addListener(new CameraListener() {
				public void onCameraUpdate(Camera camera) {
					synchronized (updatedCameras) {
						updatedCameras.add(camera);
					}
				}
			});
			cameraListeners.put(camera, new CameraListener() {
				public void onCameraUpdate(Camera camera) {
					if (camera.isActive()) {
						activeLabel.setText("ON");
						activeLabel.setForegroundColor(DARK_GREEN);
					} else {
						activeLabel.setText("off");
						activeLabel.setForegroundColor(ColorRGBA.RED);
					}
					calibrateButton.setVisible(!camera.isCalibrating());
					Vector3 cameraPosition = camera.getPosition();
					infoLabel.setText("[" + (int) (1 / camera.getScale()) + " <" + cameraPosition.getX() + ","
							+ cameraPosition.getY() + "," + cameraPosition.getZ() + ">]");
				}
			});
		}
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

		Set<Camera> updatedCamerasCopy = new HashSet<Camera>();
		synchronized (updatedCameras) {
			updatedCamerasCopy.addAll(updatedCameras);
			updatedCameras.clear();
		}
		for (Camera camera : updatedCamerasCopy) {
			CameraListener listener = cameraListeners.get(camera);
			listener.onCameraUpdate(camera);
		}
	}

	public void updateLogicalLayer(ReadOnlyTimer timer) {
		hud.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
	}

	public void render(Renderer renderer) {
		renderer.draw(hud);
	}
}
