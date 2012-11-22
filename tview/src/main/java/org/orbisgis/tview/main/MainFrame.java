/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. 
 * 
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 * 
 * This file is part of OrbisGIS.
 * 
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.tview.main;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.progress.NullProgressMonitor;

/**
 * Sample of a component that draw a map context
 */
public class MainFrame extends JFrame {

        private static final Rectangle MAIN_VIEW_POSITION_AND_SIZE = new Rectangle(20, 20, 800, 600);/*!< Bounds of mainView, x,y and width height*/

        private JLabel label = new JLabel();
        private MapContext mapContext;
        private MapTransform mapTransform;

        /**
         * Load the map context and render into a Buffered Image
         */
        public void init(MapContext mapContext) {
                this.mapContext = mapContext;
                // Initialise the Renderer           
                mapTransform = new MapTransform();
                initMapTransform(mapTransform, MAIN_VIEW_POSITION_AND_SIZE.width,
                        MAIN_VIEW_POSITION_AND_SIZE.height);
                mapTransform.setExtent(mapContext.getBoundingBox());
                // Put the buffered image into a JLabel
                getContentPane().add(label);
                // Set the state of this JFrame
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setBounds(MAIN_VIEW_POSITION_AND_SIZE);
                SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                                draw();
                        }
                });
        }

        /**
         * Draw the map context
         */
        public void draw() {
                // Draw the root layer with the map context envelope
                mapContext.draw(mapTransform, new NullProgressMonitor());
                label.setIcon(new ImageIcon(mapTransform.getImage()));
        }

        /**
         * Initialise a MapTransform, by providing a new buffered image
         *
         * @param mapTransform Holder of
         * @param width
         * @param height
         */
        private static void initMapTransform(MapTransform mapTransform, int width, int height) {
                // getting an image to draw in
                GraphicsConfiguration configuration = GraphicsEnvironment
                        .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                        .getDefaultConfiguration();
                BufferedImage inProcessImage = configuration
                        .createCompatibleImage(width, height,
                        BufferedImage.TYPE_INT_ARGB);

                // this is the new image
                // mapTransform will update the AffineTransform
                mapTransform.setImage(inProcessImage);
        }
}
