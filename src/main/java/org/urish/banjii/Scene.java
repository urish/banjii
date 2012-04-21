package org.urish.banjii;

import java.util.logging.Logger;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.example.ExampleBase;
import com.ardor3d.framework.Canvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
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
		_canvas.getCanvasRenderer().getCamera().setLocation(new Vector3(200, 150, 200));
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

		Spatial body = new Capsule("Body", 5, 5, 5, 4, 16);
		Spatial head = new Sphere("Head", 16, 16, 3);
		head.setScale(2);
		head.setTranslation(0, 17, 0);
		player.attachChild(head);
		player.attachChild(body);

		return player;
	}

	/**
	 * Creates the scene objects.
	 * 
	 * @return the node containing the objects
	 */
	private Node createObjects() {
		final Node objects = new Node("objects");

		Spatial player = createPlayer("player1");
		objects.attachChild(player);

		player = createPlayer("player2");
		player.setTranslation(0, 0, 18);
		objects.attachChild(player);
		
		TextureState ts = new TextureState();
		Texture t0 = TextureManager.load("images/ardor3d_white_256.jpg",
				Texture.MinificationFilter.BilinearNearestMipMap, true);
		t0.setWrap(Texture.WrapMode.Repeat);
		ts.setTexture(t0);

		Box box = new Box("Floor", new Vector3(-50, -1, -50), new Vector3(50, 1, 50));
		box.setTranslation(new Vector3(0, -15, 0));
		box.setRenderState(ts);
		box.setModelBound(new BoundingBox());
		objects.attachChild(box);

		return objects;
	}
}
