package org.urish.banjii;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
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
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;

public class Scene extends ExampleBase {
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(Scene.class.getName());

	/** Text fields used to present info about the example. */
	private final BasicText _exampleInfo[] = new BasicText[8];

	double counter = 0;
	int frames = 0;

	private BoundingPickResults _pickResults;
	private BasicText _text;

	private final List<Spatial> players = new ArrayList<Spatial>();
	private MaterialState playerMaterial;
	private MaterialState playerHighlightMaterial;
	

	@Override
	protected void updateExample(final ReadOnlyTimer timer) {
		counter += timer.getTimePerFrame();
		frames++;
		if (counter > 1) {
			final double fps = (frames / counter);
			counter = 0;
			frames = 0;
			System.out.printf("%7.1f FPS\n", fps);
		}
	}

	@Override
	protected void initExample() {
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
		Sphere head = new Sphere("Head", 16, 16, 0.4);
		head.setTranslation(0, 2, 0);
		head.updateModelBound();
		player.attachChild(head);
		player.attachChild(body);
		player.setRenderState(playerMaterial);
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

		Spatial player = createPlayer("Player 1");
		objects.attachChild(player);

		player = createPlayer("Player 2");
		player.setTranslation(0, 0, 1.5);
		objects.attachChild(player);

		TextureState floorTexture = new TextureState();
		Texture t0 = TextureManager.load("images/ardor3d_white_256.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap, true);
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
				for (Spatial player: players) {
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
}
