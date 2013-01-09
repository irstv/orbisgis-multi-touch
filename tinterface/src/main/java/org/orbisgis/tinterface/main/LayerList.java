package org.orbisgis.tinterface.main;

import java.util.*;
import org.mt4j.MTApplication;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.input.gestureAction.TapAndHoldVisualizer;
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
import org.orbisgis.core.layerModel.ILayer;

/**
 * Class used to create and manage the layer list
 *
 */
public class LayerList extends MTList {

        /**
         * The map
         */
        protected Map map;
        /**
         * The list of colors
         */
        protected LinkedList<MTColor> listColors;
        protected LinkedList<MTColor> listUsedColors;

        /**
         * Constructor of the list
         *
         * @param mainScene
         * @param mtApplication
         */
        public LayerList(MainScene mainScene, MTApplication mtApplication) {

                super(mtApplication, 0, 0, 152, mtApplication.getHeight());
                this.map = mainScene.getMap();

                /* We get the maximum of the numbers of characters (if >15) of the labels of the layers to define the width of the cell and the panel */
                int maxCharactersNumber = 15;
                for (ILayer layers : map.getMapContext().getLayers()) {
                        if (layers.getName().length() > maxCharactersNumber) {
                                maxCharactersNumber = layers.getName().length();
                        }
                }
                float cellWidth = Math.max((int) (maxCharactersNumber * 10.1), 150);

                /* Creates Layer Panel */
                int marginLeftPanelList = 100, marginListCell = 20;
                MTRectangle layerPanel = new MTRectangle(mtApplication, 0, 0, cellWidth + marginLeftPanelList, mtApplication.height);
                layerPanel.setFillColor(new MTColor(45, 45, 45, 180));
                layerPanel.setStrokeColor(new MTColor(45, 45, 45, 180));
                layerPanel.setPositionGlobal(new Vector3D(cellWidth / 2f + marginLeftPanelList / 2, mtApplication.height / 2f));
                layerPanel.translateGlobal(new Vector3D(-cellWidth - marginListCell, 0)); // Initializations position of the menu
                mainScene.getCanvas().addChild(layerPanel);

                /* Initialization of our LayerList itself */
                this.setChildClip(null);
                this.setNoFill(true);
                this.setNoStroke(true);
                this.unregisterAllInputProcessors();
                this.setAnchor(PositionAnchor.UPPER_LEFT);
                this.setPositionRelativeToParent(new Vector3D(marginListCell, 0));
                layerPanel.addChild(this);

                /* Initialization of the settings to generate the cells with the proportions of the mtApplication */
                float cellHeight = (((float) mtApplication.getHeight() / mtApplication.getWidth()) * cellWidth + 40);
                MTColor cellFillColor = new MTColor(new MTColor(45, 45, 45, 180));
                IFont font = FontManager.getInstance().createFont(mtApplication, "SansSerif.Bold", 15, MTColor.WHITE);

                // To define the number of colors/cells to create, we need to get the number of layers that will fit in the list
                int nbCells = map.getMapContext().getLayers().length;

                // Initialization of the lists of colors 
                this.listColors = new LinkedList<MTColor>();
                int baseIntColor = 255;
                int nbLoops = (int) (Math.floor(nbCells / 7)) + 1;
                int intColorLoop = 0;
                // We create 7 colors per loop (distributed in the scope of colors)
                for (int i = 0; i < nbLoops; i++) {
                        intColorLoop = baseIntColor / nbLoops - baseIntColor * i / nbLoops;
                        listColors.add(new MTColor(intColorLoop, 0, 0, 210));
                        listColors.add(new MTColor(0, intColorLoop, 0, 210));
                        listColors.add(new MTColor(0, 0, intColorLoop, 210));
                        listColors.add(new MTColor(intColorLoop, intColorLoop, 0, 210));
                        listColors.add(new MTColor(0, intColorLoop, intColorLoop, 210));
                        listColors.add(new MTColor(intColorLoop, 0, intColorLoop, 210));
                        listColors.add(new MTColor((int) Math.abs(intColorLoop - 20), (int) Math.abs(intColorLoop - 20), (int) Math.abs(intColorLoop - 20), 210));
                }

                this.listUsedColors = new LinkedList<MTColor>();

                /* Generation of the cells */
                LayerCell cellCreated = null;
                for (ILayer layerPossible : map.getMapContext().getLayers()) {
                        // We create the cell
                        cellCreated = this.createListCell(layerPossible, font, cellWidth, cellHeight, cellFillColor, mtApplication, mainScene);

                        //  If it is displayed at start : we color this cell
                        if (layerPossible.isVisible()) {
                                setNewColorTo(cellCreated);
                        }
                        // We add the cell to the list
                        this.addListElement(cellCreated);
                }

                // Slide out animation
                final IAnimation slideOut = new AniAnimation(0, cellWidth, 700, AniAnimation.BACK_OUT, layerPanel);
                slideOut.addAnimationListener(new IAnimationListener() {
                        public void processAnimationEvent(AnimationEvent ae) {
                                float delta = ae.getDelta();
                                ((IMTComponent3D) ae.getTarget()).translateGlobal(new Vector3D(delta, 0, 0));
                                switch (ae.getId()) {
                                        case AnimationEvent.ANIMATION_ENDED:
                                                doSlideIn = true;
                                                animationRunning = false;
                                                break;
                                }
                        }
                });

                // SlideIn Animation
                final IAnimation slideIn = new AniAnimation(0, cellWidth, 700, AniAnimation.BACK_OUT, layerPanel);
                slideIn.addAnimationListener(new IAnimationListener() {
                        public void processAnimationEvent(AnimationEvent ae) {
                                float delta = -ae.getDelta();
                                ((IMTComponent3D) ae.getTarget()).translateGlobal(new Vector3D(delta, 0, 0));
                                switch (ae.getId()) {
                                        case AnimationEvent.ANIMATION_ENDED:
                                                doSlideIn = false;
                                                animationRunning = false;
                                                break;
                                }
                        }
                });

                /* Initialization of the gesture to use on the Layer Panel (Tap to slidein or slideout) */
                layerPanel.unregisterAllInputProcessors();
                layerPanel.registerInputProcessor(new TapProcessor(mtApplication, 50));
                layerPanel.addGestureListener(TapProcessor.class, new IGestureEventListener() {
                        public boolean processGestureEvent(MTGestureEvent ge) {
                                if (((TapEvent) ge).getTapID() == TapEvent.TAPPED) {
                                        if (!animationRunning) {
                                                animationRunning = true;
                                                if (doSlideIn) {
                                                        slideIn.start();
                                                } else {
                                                        slideOut.start();
                                                }
                                        }
                                }
                                return false;
                        }
                }); 
        }
        private boolean animationRunning = false;
        private boolean doSlideIn = false;

        //Create a draggable Element of the list with a picture and a label
        private LayerCell createListCell(ILayer layer, IFont font, float cellWidth, float cellHeight, final MTColor cellFillColor, MTApplication mtApplication, MainScene mainScene) {
                final LayerCell cell = new LayerCell(layer, mtApplication, font, cellFillColor, cellWidth, cellHeight, map);

                cell.unregisterAllInputProcessors();
                // We define here the gestures that work on our cells
                // Gesture Drag & Drop to add a Layer
                cell.registerInputProcessor(new DragProcessor(mtApplication));
                cell.addGestureListener(DragProcessor.class, new IGestureEventListener() {
                        public boolean processGestureEvent(MTGestureEvent ge) {
                                DragEvent te = (DragEvent) ge;
                                MTColor oldColor = cell.getFillColor();

                                switch (te.getId()) {
                                        case DragEvent.GESTURE_STARTED:
                                                cell.setPickable(true);
                                                break;
                                        case DragEvent.GESTURE_UPDATED:
                                                Vector3D translationVector = new Vector3D(te.getTranslationVect());
                                                translationVector.y = 0;
                                                cell.translate(translationVector);
                                                break;
                                        case DragEvent.GESTURE_ENDED:
                                                Vector3D translationVectorInv = new Vector3D(cell.getCenterPointGlobal());
                                                translationVectorInv.x = -translationVectorInv.x + cell.getWidth() / 2;
                                                translationVectorInv.y = 0;
                                                cell.translate(translationVectorInv);
                                                if (Math.abs(translationVectorInv.x) >= 150) {
                                                        if (!cell.isMapAlreadyInPlace()) {
                                                                setNewColorTo(cell);
                                                                cell.setMapAlreadyInPlace(map.changeLayerState(cell.getLabel()));
                                                        }
                                                } else {
                                                        cell.setActualColor(oldColor);
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
                
                //Creation of a visualizer for the tap and hold gesture
                TapAndHoldVisualizer visualizer = new TapAndHoldVisualizer(mtApplication, mainScene.getCanvas());
                cell.addGestureListener(TapAndHoldProcessor.class, visualizer);
                
                cell.addGestureListener(TapAndHoldProcessor.class, new IGestureEventListener() {
                        public boolean processGestureEvent(MTGestureEvent ge) {
                                TapAndHoldEvent te = (TapAndHoldEvent) ge;
                                MTColor oldColor = cell.getFillColor();

                                switch (te.getId()) {
                                        case TapAndHoldEvent.GESTURE_STARTED:
                                                break;
                                        case TapAndHoldEvent.GESTURE_UPDATED:
                                                break;
                                        case TapAndHoldEvent.GESTURE_ENDED:
                                                if (te.isHoldComplete() && cell.isMapAlreadyInPlace()) {
                                                        cell.setActualColor(cellFillColor);
                                                        listUsedColors.remove(oldColor);
                                                        cell.setMapAlreadyInPlace(map.changeLayerState(cell.getLabel()));
                                                }
                                                break;
                                }
                                return false;
                        }
                });

                return cell;
        }

        /**
         * Method that allows to set a new color to a cell
         *
         * @param cell : cell for which the color has to be changed
         */
        public void setNewColorTo(LayerCell cell) {
                // Default Color
                MTColor newColor = new MTColor(125, 125, 125, 125);

                // To verify there still are non used colors
                if (listColors.size() > listUsedColors.size()) {

                        if (listUsedColors.contains(newColor)) {
                                while (listUsedColors.contains(newColor)) {
                                        newColor = listColors.get((int) Math.floor(Math.random() * listColors.size()));
                                }
                        } else if (!listUsedColors.contains(newColor)) {
                                cell.setActualColor(newColor);
                                listUsedColors.add(newColor);
                        }

                } else {
                        newColor = new MTColor((int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255), (int) Math.floor(Math.random() * 255), 180);
                }

                cell.setActualColor(newColor);
                listUsedColors.add(newColor);
        }
}
