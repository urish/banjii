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
import org.urish.banjii.model.RealWorldParameters;

import com.ardor3d.extension.ui.Orientation;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UICheckBox;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UISlider;
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
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
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

	private Camera calibrationCamera;
	private UIPanel calibrationPanel;
	private UISlider sliderXPosition;
	private UISlider sliderYPosition;
	private UISlider sliderZPosition;

	private UISlider sliderXOrientation;
	private UISlider sliderYOrientation;
	private UISlider sliderZOrientation;

	private UIFrame calibrationFrame;

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
		makeCalibrationFrame();

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
			calibrateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					calibrationCamera = camera;
					loadCameraCalibration(camera);
					hud.add(calibrationFrame);
				}
			});

			camerasPanel.add(cameraLabel);
			camerasPanel.add(activeLabel);
			camerasPanel.add(calibrateButton);
			calibrateButton.setLayoutData(GridLayoutData.WrapAndGrow);

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
						activeLabel.setText("ACT");
						activeLabel.setForegroundColor(ColorRGBA.BLUE);
					} else if (camera.isConnected()) {
						activeLabel.setText("ON");
						activeLabel.setForegroundColor(DARK_GREEN);
					} else {
						activeLabel.setText("off");
						activeLabel.setForegroundColor(ColorRGBA.RED);
					}
				}
			});
		}

		return camerasPanel;
	}

	private void makeCalibrationFrame() {
		calibrationPanel = new UIPanel(new GridLayout());
		calibrationPanel.setLayoutData(GridLayoutData.Span(4));

		CameraParametersListener calibrationListener = new CameraParametersListener();

		// camera position and orientation sliders
		final UILabel positionLabel = new UILabel("Position");
		sliderXPosition = new UISlider(Orientation.Horizontal, 0, 100, 0);
		sliderYPosition = new UISlider(Orientation.Horizontal, 0, 100, 0);
		sliderZPosition = new UISlider(Orientation.Horizontal, 0, 100, 0);
		sliderXPosition.addActionListener(calibrationListener);
		sliderYPosition.addActionListener(calibrationListener);
		sliderZPosition.addActionListener(calibrationListener);
		sliderXPosition.setContentWidth(80);
		sliderYPosition.setContentWidth(80);
		sliderZPosition.setContentWidth(80);
		sliderZPosition.setLayoutData(GridLayoutData.Wrap);

		final UILabel orientationLabel = new UILabel("Orientation");
		sliderXOrientation = new UISlider(Orientation.Horizontal, 0, 360, 0);
		sliderYOrientation = new UISlider(Orientation.Horizontal, 0, 360, 0);
		sliderZOrientation = new UISlider(Orientation.Horizontal, 0, 360, 0);
		sliderXOrientation.addActionListener(calibrationListener);
		sliderYOrientation.addActionListener(calibrationListener);
		sliderZOrientation.addActionListener(calibrationListener);
		sliderXOrientation.setContentWidth(80);
		sliderYOrientation.setContentWidth(80);
		sliderZOrientation.setContentWidth(80);
		sliderZOrientation.setLayoutData(GridLayoutData.Wrap);

		UIButton doneButton = new UIButton("Done");
		doneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				calibrationFrame.close();
			}
		});

		calibrationPanel.add(positionLabel);
		calibrationPanel.add(sliderXPosition);
		calibrationPanel.add(sliderYPosition);
		calibrationPanel.add(sliderZPosition);

		calibrationPanel.add(orientationLabel);
		calibrationPanel.add(sliderXOrientation);
		calibrationPanel.add(sliderYOrientation);
		calibrationPanel.add(sliderZOrientation);
		calibrationPanel.add(doneButton);

		calibrationFrame = new UIFrame("Camera Calibration");
		calibrationFrame.setContentPanel(calibrationPanel);
		calibrationFrame.updateMinimumSizeFromContents();
		calibrationFrame.layout();
		calibrationFrame.pack();

		calibrationFrame.setUseStandin(true);
		calibrationFrame.setOpacity(1f);
		calibrationFrame.setHudXY(canvas.getCanvasRenderer().getCamera().getWidth() - calibrationFrame.getLocalComponentWidth()
				- 5, canvas.getCanvasRenderer().getCamera().getHeight() - calibrationFrame.getLocalComponentHeight() - 5);
		calibrationFrame.setName("Calibration");
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

	private void loadCameraCalibration(Camera camera) {
		ReadOnlyVector3 position = camera.getPosition();
		sliderXPosition.setValue((int) (position.getX() * 100 / RealWorldParameters.ROOM_LENGTH));
		sliderYPosition.setValue((int) (position.getY() * 100 / RealWorldParameters.ROOM_HEIGHT));
		sliderZPosition.setValue((int) (position.getZ() * 100 / RealWorldParameters.ROOM_WIDTH));

		double[] angles = camera.getOrientation().toAngles(null);
		double fromRadians = 180. / Math.PI;
		sliderXOrientation.setValue((int) (angles[0] * fromRadians));
		sliderYOrientation.setValue((int) (angles[1] * fromRadians));
		sliderZOrientation.setValue((int) (angles[2] * fromRadians));
	}

	private class CameraParametersListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Vector3 positionValues = new Vector3();

			double lengthProportion = RealWorldParameters.ROOM_LENGTH / 100.;
			double widthProportion = RealWorldParameters.ROOM_WIDTH / 100.;
			double heightProportion = RealWorldParameters.ROOM_HEIGHT / 100.;
			positionValues.setX(sliderXPosition.getValue() * lengthProportion);
			positionValues.setY(sliderYPosition.getValue() * heightProportion);
			positionValues.setZ(sliderZPosition.getValue() * widthProportion);

			Matrix3 orientationMatrix = new Matrix3();
			double toRadians = Math.PI / 180.;
			orientationMatrix.fromAngles(sliderXOrientation.getValue() * toRadians, sliderYOrientation.getValue() * toRadians,
					sliderZOrientation.getValue() * toRadians);

			cameraManager.updateCameraPosition(calibrationCamera, positionValues, orientationMatrix);
		}
	}
}
