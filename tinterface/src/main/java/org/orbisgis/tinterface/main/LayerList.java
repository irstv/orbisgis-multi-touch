package org.orbisgis.tinterface.main;

import org.mt4j.components.visibleComponents.widgets.MTList;
import org.mt4j.MTApplication;
import org.mt4j.components.interfaces.IMTComponent3D;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
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

public class LayerList extends MTList {

        /**
         * The map
         */
        protected Map m;

        /**
         * Constructor of the list
         *
         * @param mainScene
         * @param mtApplication
         */
        public LayerList(MainScene mainScene, MTApplication mtApplication) {
                super(mtApplication, 0, 0, 152, 730);
                m = mainScene.getMap();

                /// Create Layer menu \\\
                MTRectangle mapMenu = new MTRectangle(mtApplication, 0, 0, 240, 750);
                mapMenu.setFillColor(new MTColor(45, 45, 45, 180));
                mapMenu.setStrokeColor(new MTColor(45, 45, 45, 180));
                mapMenu.setPositionGlobal(new Vector3D(mtApplication.width / 2f, mtApplication.height / 2f));
                mapMenu.translateGlobal(new Vector3D(-mtApplication.width / 2f - 80, -10)); // Initializations position of the menu
                mainScene.getCanvas().addChild(mapMenu);

                float cellWidth = 155, cellHeight = 90;
                MTColor cellFillColor = new MTColor(new MTColor(0, 0, 0, 210));
                MTColor cellPressedFillColor = new MTColor(new MTColor(20, 20, 20, 220));
                IFont font = FontManager.getInstance().createFont(mtApplication, "SansSerif.Bold", 15, MTColor.WHITE);

                this.setChildClip(null); //FIXME TEST -> do no clipping for performance
                this.setNoFill(true);
                this.setNoStroke(true);
                this.unregisterAllInputProcessors();
                this.setAnchor(PositionAnchor.CENTER);
                this.setPositionRelativeToParent(mapMenu.getCenterPointLocal());
                mapMenu.addChild(this);

                for (int i = 0; i < 20; i++) {
                        this.addListElement(this.createListCell("Element" + i, font, cellWidth, cellHeight, cellFillColor, cellPressedFillColor, mtApplication));
                }

                // Slide out animation
                final IAnimation slideOut = new AniAnimation(0, 170, 700, AniAnimation.BACK_OUT, mapMenu);
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
                final IAnimation slideIn = new AniAnimation(0, 170, 700, AniAnimation.BACK_OUT, mapMenu);
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

                mapMenu.unregisterAllInputProcessors();
                mapMenu.registerInputProcessor(new TapProcessor(mtApplication, 50));
                mapMenu.addGestureListener(TapProcessor.class, new IGestureEventListener() {
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

                //updateTagContainerScale(); //needed to initialize..if not i observed strange behavior with the photo tags 
        }
        private boolean animationRunning = false;
        private boolean doSlideIn = false;

        //  Has to be modified by paulo : Have to create an draggable Element with a picture and a label
        private MTListCell createListCell(final String label, IFont font, float cellWidth, float cellHeight, final MTColor cellFillColor, final MTColor cellPressedFillColor, MTApplication mtApplication) {
                final MTListCell cell = new MTListCell(mtApplication, cellWidth, cellHeight);

                cell.setChildClip(null); //FIXME TEST, no clipping for performance!

                cell.setFillColor(cellFillColor);

                // To get an image that is at a path
		/* String path ="map.PNG";
                 MTImage imageCell;
                 imageCell.setTexture(path);
                 imageCell.getImage();
                 cell.addChild((MTComponent) imageCell);*/
                cell.addChild(new MTRectangle(mtApplication, 50, 10, 50, 50));

                MTTextArea listLabel = new MTTextArea(mtApplication, font);
                listLabel.setNoFill(true);
                listLabel.setNoStroke(true);
                listLabel.setText(label);
                cell.addChild(listLabel);
                Vector3D positionText = cell.getCenterPointLocal();
                positionText.y += 30;
                listLabel.setPositionRelativeToParent(positionText);
                cell.unregisterAllInputProcessors();
                cell.registerInputProcessor(new TapProcessor(mtApplication, 15));

                // (Drag & drop = not change provider but AddLayer, tap to remove Layer)
                cell.addGestureListener(TapProcessor.class, new IGestureEventListener() {
                        public boolean processGestureEvent(MTGestureEvent ge) {
                                TapEvent te = (TapEvent) ge;
                                switch (te.getTapID()) {
                                        case TapEvent.TAP_DOWN:
                                                cell.setFillColor(cellPressedFillColor);
                                                break;
                                        case TapEvent.TAP_UP:
                                                cell.setFillColor(cellFillColor);
                                                break;
                                        case TapEvent.TAPPED:
//						System.out.println("Button clicked: " + label);
                                                cell.setFillColor(cellFillColor);
                                                break;
                                }
                                return false;
                        }
                });
                return cell;
        }
}
