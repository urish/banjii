package org.urish.banjii;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.urish.banjii.api.CameraManager;
import org.urish.banjii.model.Player;
import org.urish.banjii.model.PlayerListener;
import org.urish.banjii.model.PlayerManager;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.example.ExampleBase;
import com.ardor3d.framework.Canvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.BoundingPickResults;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.MaterialState.MaterialFace;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Capsule;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.scenegraph.visitor.UpdateModelBoundVisitor;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

public class Scene extends ExampleBase {
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(Scene.class.getName());

	/** Text fields used to present info about the example. */
	private final BasicText _exampleInfo[] = new BasicText[8];

	private BoundingPickResults _pickResults;
	private BasicText _text;

	private final List<Spatial> players = new ArrayList<Spatial>();
	private MaterialState playerMaterial;
	private MaterialState playerHighlightMaterial;

	private TextureState playerHeadTexture;
	private UserInterface userInterface;

	@Override
	protected void initExample() {
		CameraManager.instance.setScene(this);

		_canvas.setTitle("Banjii Viewer");
		_canvas.getCanvasRenderer().getCamera().setLocation(new Vector3(5, 5, 5));
		_canvas.getCanvasRenderer().getCamera().lookAt(new Vector3(0, 0, 0), Vector3.UNIT_Y);

		_logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.SPACE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				updateText();
			}
		}));

		final RenderPass rootPass = new RenderPass();
		rootPass.add(_root);

		final TextureState ts = new TextureState();
		ts.setEnabled(true);
		ts.setTexture(TextureManager.load("images/ardor3d_white_256.jpg", Texture.MinificationFilter.Trilinear, true));
		_root.setRenderState(ts);

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		_root.setRenderState(ms);

		_root.attachChild(createObjects());

		userInterface = new UserInterface(_canvas, _physicalLayer, _logicalLayer);
		userInterface.init();

		// Setup textfields for presenting example info.
		final Node textNodes = new Node("Text");
		final RenderPass renderPass = new RenderPass();
		renderPass.add(textNodes);
		textNodes.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
		textNodes.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		final double infoStartY = _canvas.getCanvasRenderer().getCamera().getHeight() / 2;
		for (int i = 0; i < _exampleInfo.length; i++) {
			_exampleInfo[i] = BasicText.createDefaultTextLabel("Text", "", 16);
			_exampleInfo[i].setTranslation(new Vector3(10, infoStartY - i * 20, 0));
			textNodes.attachChild(_exampleInfo[i]);
		}

		textNodes.updateGeometricState(0.0);
		updateText();

		_logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ONE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// TODO
			}
		}));
	}

	/**
	 * Update text information.
	 */
	private void updateText() {
		_exampleInfo[0].setText("Hello !");
	}

	private Spatial createPlayer(String name) {
		Node player = new Node(name);

		Capsule body = new Capsule("Body", 5, 5, 5, 0.35, 1.5);
		body.setTranslation(0, 0.75, 0);
		body.updateModelBound();
		Sphere head = new Sphere("Head", 16, 16, 0.35);
		Matrix3 rotation = new Matrix3();
		rotation.fromAngles(Math.PI / 2, Math.PI, 0);
		head.setTranslation(0, 2.25, 0);
		head.setRotation(rotation);
		head.setScale(0.8, 1, 1.5);
		head.updateModelBound();
		head.setRenderState(playerHeadTexture);
		player.attachChild(body);
		player.attachChild(head);
		player.setRenderState(playerMaterial);
		player.setScale(0.4);
		players.add(player);

		return player;
	}

	/**
	 * Creates the scene objects.
	 * 
	 * @return the node containing the objects
	 */
	private Node createObjects() {
		final Node objects = new Node("objects");

		playerMaterial = new MaterialState();
		playerMaterial.setDiffuse(MaterialFace.FrontAndBack, ColorRGBA.WHITE);
		playerHighlightMaterial = new MaterialState();
		playerHighlightMaterial.setDiffuse(MaterialFace.FrontAndBack, ColorRGBA.YELLOW);

		playerHeadTexture = new TextureState();
		Texture t0 = TextureManager.load("textures/head.jpg", Texture.MinificationFilter.BilinearNearestMipMap, false);
		t0.setWrap(Texture.WrapMode.Clamp);
		playerHeadTexture.setTexture(t0);

		for (Player player : PlayerManager.instance.getPlayers()) {
			final Spatial playerObject = createPlayer(player.getName());
			objects.attachChild(playerObject);
			playerObject.setTranslation(player.getId() / 3, 0, player.getId() % 3);
			player.addListener(new PlayerListener() {
				public void onPlayerUpdate(Player player) {
					if (player.isVisible()) {
						objects.attachChild(playerObject);
					} else {
						playerObject.removeFromParent();
					}
				}
			});
		}

		TextureState floorTexture = new TextureState();
		t0 = TextureManager.load("textures/floor.jpg", Texture.MinificationFilter.BilinearNearestMipMap, true);
		t0.setWrap(Texture.WrapMode.Repeat);
		floorTexture.setTexture(t0);

		Box box = new Box("Floor", new Vector3(0, -.1, 0), new Vector3(5, 0, 5));
		box.setTranslation(new Vector3(-2.5, 0, -2.5));
		box.setRenderState(floorTexture);
		box.setModelBound(new BoundingBox());
		objects.attachChild(box);

		// Set up a reusable pick results
		_pickResults = new BoundingPickResults();
		_pickResults.setCheckDistance(true);

		// Set up our pick label
		_text = BasicText.createDefaultTextLabel("", "pick");
		_text.setTranslation(10, 10, 0);
		_text.getSceneHints().setCullHint(CullHint.Always);
		objects.attachChild(_text);

		return objects;
	}

	@Override
	protected void registerInputTriggers() {
		super.registerInputTriggers();

		_logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// Put together a pick ray
				final Vector2 pos = Vector2.fetchTempInstance().set(inputStates.getCurrent().getMouseState().getX(),
						inputStates.getCurrent().getMouseState().getY());
				final Ray3 pickRay = Ray3.fetchTempInstance();
				_canvas.getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
				Vector2.releaseTempInstance(pos);

				// Do the pick
				_pickResults.clear();
				PickingUtil.findPick(_root, pickRay, _pickResults);
				Ray3.releaseTempInstance(pickRay);

				String text = "";
				_text.getSceneHints().setCullHint(CullHint.Never);

				Spatial highlightPlayer = null;
				for (int i = 0; i < _pickResults.getNumber(); i++) {
					final PickData pick = _pickResults.getPickData(i);
					if (pick.getTarget() instanceof Spatial) {
						Spatial pickParent = ((Spatial) pick.getTarget()).getParent();
						if (players.contains(pickParent)) {
							highlightPlayer = pickParent;
							text = pickParent.getName();
							break;
						}
					}
				}
				for (Spatial player : players) {
					if (player.equals(highlightPlayer)) {
						player.setRenderState(playerHighlightMaterial);
					} else {
						player.setRenderState(playerMaterial);
					}
				}
				_text.setText(text);
			}
		}));
	}

	@Override
	protected void updateExample(final ReadOnlyTimer timer) {
		userInterface.update(timer);
	}

	@Override
	protected void updateLogicalLayer(final ReadOnlyTimer timer) {
		userInterface.updateLogicalLayer(timer);
	}

	@Override
	protected void renderExample(final Renderer renderer) {
		super.renderExample(renderer);
		renderer.renderBuckets();
		userInterface.render(renderer);
	}

	public void setPlayerTransform(int playerId, ReadOnlyVector3 position, ReadOnlyMatrix3 rotation) {
		if (playerId >= players.size()) {
			logger.warning("Player " + playerId + " not found! Ignoring request");
			return;
		}
		players.get(playerId).setTranslation(position);
		players.get(playerId).setRotation(rotation);
		players.get(playerId).acceptVisitor(new UpdateModelBoundVisitor(), false);
	}
}
