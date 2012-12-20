package org.orbisgis.tinterface.main;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.gestureAction.TapAndHoldVisualizer;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
import org.mt4j.util.MTColor;
import org.mt4j.util.font.FontManager;

/**
 * Class used for creating and managing tooltip
 * @author patrick
 *
 */
public class Tooltip extends MTTextArea{
	
	/**
	 * Create a tooltip with the corresponding parameters
	 * @param mtApplication the mtApplication
	 * @param vector the position of the tooltip
	 * @param info the text in the tooltip
	 */
	public Tooltip(MTApplication mtApplication, String info){
		super(mtApplication, FontManager.getInstance().createFont(mtApplication, "SansSerif", 12));
		
		//Unregister the default gestures (so that the tooltip can't be moved)
		this.unregisterAllInputProcessors();
		this.removeAllGestureEventListeners();
		
		//Set the parameters of the tooltip
		this.setFillColor(new MTColor(200, 200, 200));
		this.setText(info);
		
		// Add the tap and hold gesture on the tooltip (with 1s hold)(the class
		// TooltipTapAndHold will be used) to delete this tooltip
		TapAndHoldProcessor tooltipTap = new TapAndHoldProcessor(mtApplication);
		tooltipTap.setHoldTime(1000);
		this.registerInputProcessor(tooltipTap);
		this.addGestureListener(TapAndHoldProcessor.class, new TooltipTapAndHold());
		
		//Creation of a visualizer for the tap and hold gesture
		TapAndHoldVisualizer visualizer = new TapAndHoldVisualizer(mtApplication, this);
		this.addGestureListener(TapAndHoldProcessor.class, visualizer);
	}

	
	/**
	 * Nested class used when a tap and hold gesture on the tooltip is detected
	 * 
	 * @author patrick
	 * 
	 */
	private class TooltipTapAndHold implements IGestureEventListener {
		/**
		 * Method called when a tap and hold gesture is detected (to remove this tooltip)
		 */
		public boolean processGestureEvent(MTGestureEvent gest) {
			TapAndHoldEvent gesture = (TapAndHoldEvent) gest;
			//Check if the gesture is finished and if it was completed
			if (gesture.getId() == MTGestureEvent.GESTURE_ENDED && gesture.isHoldComplete()) {
				Tooltip.this.destroy();
			}
			return false;
		}
	}
}
