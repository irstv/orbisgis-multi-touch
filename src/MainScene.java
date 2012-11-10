import org.mt4j.MTApplication;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleEvent;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.math.Vector3D;

import com.modestmaps.TestInteractiveMap;

/**
 * This class create the main scene from the application. The map, the layer
 * list and the temporal line are added as son from this scene
 * 
 * @author patrick
 * 
 */
public class MainScene extends AbstractScene {

	/** The map */
	private TestInteractiveMap map;

	/** The layer list */
	// private LayerList layerList;

	/** The temporal line */
	// private TemporalLine temporalLine;

	/**
	 * Constructor of MainScene
	 * 
	 * @param mtApplication
	 *            the application
	 * @param name
	 *            the name of the scene
	 */
	public MainScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);

		// Add a circle around every point that is touched
		this.registerGlobalInputProcessor(new CursorTracer(mtApplication, this));

		// Instantiate a new map with the default configuration (specify in a
		// configuration file) and add it to the scene
		map = new TestInteractiveMap(mtApplication);
		this.getCanvas().addChild(map);

		// Instantiate a new layer list and add it the the scene
		// layerList = new LayerList();
		// this.getCanvas().addChild(layerList);

		// Instantiate a new temporal line and add it to the scene
		// temporalLine = new TemporalLine();
		// this.getCanvas().addChild(temporalLine);

		// Add the drag gesture on the map (the class MapDrag will be used)
		map.registerInputProcessor(new DragProcessor(mtApplication));
		map.addGestureListener(DragProcessor.class, new MapDrag());

		// Add the scale gesture on the map (the class MapScale will be used)
		map.registerInputProcessor(new ScaleProcessor(mtApplication));
		map.addGestureListener(ScaleProcessor.class, new MapScale());

		// Add the tap and hold gesture on the map (with 3s hold)(the class
		// MapTapAndHold will be used)
		TapAndHoldProcessor tap = new TapAndHoldProcessor(mtApplication);
		tap.setHoldTime(3000);
		map.registerInputProcessor(tap);
		map.addGestureListener(TapAndHoldProcessor.class, new MapTapAndHold());

	}

	/**
	 * Nested class used when a drag gesture on the map is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class MapDrag implements IGestureEventListener {
		/**
		 * Method called when a drag gesture is detected
		 */
		public boolean processGestureEvent(MTGestureEvent gesture) {
			// Get the translation vector
			Vector3D tVect = ((DragEvent) gesture).getTranslationVect();
			// Move the map
			map.move(tVect.x, tVect.y);

			return false;
		}
	}

	/**
	 * Nested class used when a scale gesture on the map is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class MapScale implements IGestureEventListener {
		/**
		 * Method called when a scale gesture is detected
		 */
		public boolean processGestureEvent(MTGestureEvent gesture) {
			// Scale the map
			float scaleX = ((ScaleEvent) gesture).getScaleFactorX();
			map.sc *= scaleX;

			return false;
		}
	}

	/**
	 * Nested class used when a tap and hold gesture on the map is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class MapTapAndHold implements IGestureEventListener {
		/**
		 * Method called when a scale gesture is detected
		 */
		public boolean processGestureEvent(MTGestureEvent gesture) {
			if (gesture.getId() == MTGestureEvent.GESTURE_ENDED) {
				//map.getInfos(((TapAndHoldEvent) gesture).getLocationOnScreen());
			}
			return false;
		}
	}

}