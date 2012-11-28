package tinterface.src.main.java.org.orbisgis.tinterface.main;

import java.util.*;

import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.MTApplication;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.util.MTColor;
import org.mt4j.util.animation.AnimationEvent;
import org.mt4j.util.animation.IAnimation;
import org.mt4j.util.animation.IAnimationListener;
import org.mt4j.util.animation.ani.AniAnimation;
import org.mt4j.util.font.FontManager;
import org.mt4j.util.font.IFont;
import org.mt4j.util.math.Vector3D;

import com.modestmaps.TestInteractiveMap;
import com.modestmaps.providers.AbstractMapProvider;
import com.modestmaps.providers.Microsoft;

public class LayerList extends MTList{

	/** The map */
	protected TestInteractiveMap m;

	/** The list of colors */
	protected LinkedList<MTColor> listColors;
	protected LinkedList<MTColor> listUsedColors;
	
	/**
	 * Constructor of the list
	 * @param mainScene
	 * @param mtApplication
	 */
	public LayerList(MainScene mainScene, MTApplication mtApplication){
		super(mtApplication,0, 0, 152, 730); 
		m = mainScene.getMap();

		// Initialize the lists of colors 
		this.listColors = new LinkedList<MTColor>();
		listColors.add(new MTColor(200,0,0,210));
		listColors.add(new MTColor(0,200,0,210));
		listColors.add(new MTColor(0,0,200,210));
		listColors.add(new MTColor(200,200,0,210));
		listColors.add(new MTColor(0,200,200,210));
		listColors.add(new MTColor(200,0,200,210));
		listColors.add(new MTColor(200,200,200,210));
		listColors.add(new MTColor(100,0,200,210));
		listColors.add(new MTColor(200,0,100,210));
		listColors.add(new MTColor(0,100,200,210));

		this.listUsedColors = new LinkedList<MTColor>();

		/// Create Layer menu \\\
		MTRectangle mapMenu = new MTRectangle(mtApplication,0,0,240, 770);
		mapMenu.setFillColor(new MTColor(45,45,45,180));
		mapMenu.setStrokeColor(new MTColor(45,45,45,180));
		mapMenu.setPositionGlobal(new Vector3D(mtApplication.width/2f, mtApplication.height/2f));
		mapMenu.translateGlobal(new Vector3D(-mtApplication.width/2f - 80,0)); // Initializations position of the menu
		mainScene.getCanvas().addChild(mapMenu);

		float cellWidth = 155, cellHeight = 90; 
		MTColor cellFillColor = new MTColor(new MTColor(0,0,0,210));
		MTColor cellPressedFillColor = new MTColor(new MTColor(20,20,20,220));
		IFont font = FontManager.getInstance().createFont(mtApplication, "SansSerif.Bold", 15, MTColor.WHITE);

		this.setChildClip(null); //FIXME TEST -> do no clipping for performance
		this.setNoFill(true);
		this.setNoStroke(true);
		this.unregisterAllInputProcessors();
		this.setAnchor(PositionAnchor.CENTER);
		this.setPositionRelativeToParent(mapMenu.getCenterPointLocal());
		mapMenu.addChild(this);

		for(int i=0;i<10;i++)
			this.addListElement(this.createListCell("Element"+i, font, new Microsoft.AerialProvider(), cellWidth, cellHeight, cellFillColor, cellPressedFillColor, mtApplication));

		// Slide out animation
		final IAnimation slideOut = new AniAnimation(0, 170, 700, AniAnimation.BACK_OUT, mapMenu);
		slideOut.addAnimationListener(new IAnimationListener() {
			public void processAnimationEvent(AnimationEvent ae) {
				float delta = ae.getDelta();
				((IMTComponent3D)ae.getTarget()).translateGlobal(new Vector3D(delta,0,0));
				switch (ae.getId()) {
				case AnimationEvent.ANIMATION_ENDED:
					doSlideIn = true;
					animationRunning = false;
					break;
				}
			}
		});

		// SlideIn Animation
		final IAnimation slideIn = new AniAnimation(0, 170, 700, AniAnimation.BACK_OUT, mapMenu);
		slideIn.addAnimationListener(new IAnimationListener() {
			public void processAnimationEvent(AnimationEvent ae) {
				float delta = -ae.getDelta();
				((IMTComponent3D)ae.getTarget()).translateGlobal(new Vector3D(delta,0,0));
				switch (ae.getId()) {
				case AnimationEvent.ANIMATION_ENDED:
					doSlideIn = false;
					animationRunning = false;
					break;
				}
			}
		});

		mapMenu.unregisterAllInputProcessors();
		mapMenu.registerInputProcessor(new TapProcessor(mtApplication, 50));
		mapMenu.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				if (((TapEvent)ge).getTapID() == TapEvent.TAPPED){
					if (!animationRunning){
						animationRunning = true;
						if (doSlideIn){
							slideIn.start();
						}else{
							slideOut.start();
						}
					}
				}
				return false;
			}
		});

		//updateTagContainerScale(); //needed to initialize..if not i observed strange behavior with the photo tags 
	}


	private boolean animationRunning = false;
	private boolean doSlideIn = false;

	//  create a draggable Element with a picture and a label
	private MTListCell createListCell(final String label, IFont font, final AbstractMapProvider mapProvider, float cellWidth, float cellHeight, final MTColor cellFillColor, final MTColor cellPressedFillColor,MTApplication mtApplication){
		final MTListCell cell = new MTListCell(mtApplication, cellWidth, cellHeight);

		cell.setChildClip(null); //FIXME TEST, no clipping for performance!

		cell.setFillColor(cellFillColor);
		// Thumbnail is added here
		cell.addChild(new MTRectangle(mtApplication, 50,10,50,50));

		MTTextArea listLabel = new MTTextArea(mtApplication, font);
		listLabel.setNoFill(true);
		listLabel.setNoStroke(true);
		// Label is added here
		listLabel.setText(label);
		cell.addChild(listLabel);
		Vector3D positionText = cell.getCenterPointLocal();
		positionText.y +=30; 
		listLabel.setPositionRelativeToParent(positionText);
		cell.unregisterAllInputProcessors();

		// Drag & Drop to add a Layer
		cell.registerInputProcessor(new DragProcessor(mtApplication));

		cell.addGestureListener(DragProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				/* State of the map */
				boolean mapAlreadyInPlace = false;
				
				DragEvent te = (DragEvent)ge;
				MTColor oldColor = cell.getFillColor();
								
				switch (te.getId()) { 
				case DragEvent.GESTURE_STARTED:
					cell.setPickable(true);
					break;
				case DragEvent.GESTURE_UPDATED:
					Vector3D translationVector = new Vector3D(te.getTranslationVect());
					translationVector.y=0;
					cell.translate(translationVector);
					break;
				case DragEvent.GESTURE_ENDED:
					Vector3D translationVectorInv = new Vector3D(cell.getCenterPointGlobal());
					translationVectorInv.x = -translationVectorInv.x+93;
					translationVectorInv.y = 0;
					cell.translate(translationVectorInv);
					if(Math.abs(translationVectorInv.x) >= 150) {
						while( cell.getFillColor() == oldColor && !mapAlreadyInPlace) {
							if(oldColor.equals(cellFillColor)){
								if(listColors.size()>listUsedColors.size()){
									MTColor newColor =(listColors.get((int)Math.floor(Math.random()*listColors.size())));
	
									if(!listUsedColors.contains(newColor)){
										cell.setFillColor(newColor);
										listUsedColors.add(newColor);
									}
								}
								else {
									cell.setFillColor(new MTColor(125,125,125,125));
								}
							}
							else {
								mapAlreadyInPlace = true;
							}

						}


						// m.addLayer(cell.Layer);
					}
					else {
						cell.setFillColor(oldColor);
					}

					break;
				}
				return false;
			}
		});

		// tap and hold to remove Layer
		TapAndHoldProcessor tap = new TapAndHoldProcessor(mtApplication);
		tap.setHoldTime(1000);
		cell.registerInputProcessor(tap);

		cell.addGestureListener(TapAndHoldProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				TapAndHoldEvent te = (TapAndHoldEvent)ge;
				MTColor oldColor = cell.getFillColor();

				switch (te.getId()) { 
				case TapAndHoldEvent.GESTURE_STARTED:
					System.out.println("Tap & Hold started");
					break;
				case TapAndHoldEvent.GESTURE_UPDATED:
					System.out.println("Tap & Hold updated");
					//cell.setFillColor(cellPressedFillColor);
					break;
				case TapAndHoldEvent.GESTURE_ENDED:
					if(te.isHoldComplete()){
						System.out.println("Tap & Hold finished");
						cell.setFillColor(cellFillColor);
						listUsedColors.remove(oldColor);
						//m.removeLayer(cell.Layer);
					}
					else{
						System.out.println("Tap And Hold finished but Canceled");
						cell.setFillColor(oldColor);
					}

					break;
				}
				return false;
			}
		});

		return cell;
	}

}
