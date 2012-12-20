package org.orbisgis.tinterface.main;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKTWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.sql.engine.ParseException;
import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.util.math.Vector3D;
import org.orbisgis.core.DataManager;
import org.orbisgis.core.Services;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.layerModel.ILayer;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.layerModel.OwsMapContext;
import org.orbisgis.core.workspace.CoreWorkspace;
import org.orbisgis.progress.NullProgressMonitor;
import processing.core.PImage;

/**
 * Constructor of the class Map
 * Creates a new Map, a mt4j rectangle element, textured with the OrbisGIS map
 *
 * @author patrick
 *
 */
public class Map extends MTRectangle {

        private final OrbisGISMap frame;
        private final MapContext mapContext;
        /**
         * The mtApplication
         */
        private MTApplication mtApplication;
        /**
         * The number by which the size of the screen is multiplied to draw the rectangle
         */
        private float buffersize;

        public Map(MTApplication mtApplication, MainScene mainScene, float buffersize)
                throws Exception {
        		//Create the rectangle (that will contain the map)
                super(mtApplication, mtApplication.width * buffersize, mtApplication.height * buffersize);
                
                //Delete the white borders of the rectangle
                this.setNoStroke(true);
                //Place the rectangle in the middle of the screen
                this.setPositionGlobal(new Vector3D(mtApplication.width / 2, mtApplication.height / 2));
                //Initialize attributes
                this.mtApplication = mtApplication;
                this.buffersize = buffersize;
                //Remove the predefined gestures for the rectangle
                this.unregisterAllInputProcessors();
                this.removeAllGestureEventListeners();

                //Initialize the map with OrbisGIS
                MainContext.initConsoleLogger(true);
                CoreWorkspace workspace = new CoreWorkspace();
                File workspaceFolder = new File(System.getProperty("user.home"),
                        "OrbisGIS_MT" + File.separator);
                workspace.setWorkspaceFolder(workspaceFolder.getAbsolutePath());
                MainContext mainContext = new MainContext(true, workspace, true);
                frame = new OrbisGISMap();
                mapContext = loadMapContext();
                frame.init(mapContext, (int) (mtApplication.width * buffersize), (int) (mtApplication.height * buffersize));
                Envelope extent = frame.getMapTransform().getExtent();
                double facteur = (buffersize - 1) / 2;
                frame.getMapTransform().setExtent(
                        new Envelope(extent.getMinX() - facteur * extent.getWidth(), extent.getMaxX() + facteur * extent.getWidth(),
                        extent.getMinY() - facteur * extent.getHeight(), extent.getMaxY() + facteur * extent.getHeight()));

                //Draw an image of the map
                mapContext.draw(frame.getMapTransform(), new NullProgressMonitor());

                //Put the image as texture of the rectangle
                BufferedImage im = frame.getMapTransform().getImage();
                PImage image = new PImage(im);
                this.setTexture(image);
                
                //Add the rectangle to the main scene
                mainScene.getCanvas().addChild(this);
        }

        public float getBuffersize() {
                return this.buffersize;
        }

        public MapContext getMapContext() {
                return this.mapContext;
        }

        /**
         * Method used to move the map from the parameter in input
         *
         * @param x the number of pixel the map need to be moved (in x)
         * @param y the number of pixel the map need to be moved (in y)
         */
        public void move(float x, float y) {
        		//Modify the extent of the map
                Envelope extent = frame.getMapTransform().getExtent();
                double dx = x * extent.getWidth() / (this.getWidthXYGlobal());
                double dy = y * extent.getHeight() / (this.getHeightXYGlobal());
                frame.getMapTransform().setExtent(
                        new Envelope(extent.getMinX() - dx, extent.getMaxX() - dx,
                        extent.getMinY() + dy, extent.getMaxY() + dy));
                
                //Draw the new image
                frame.getMapTransform().setImage(new BufferedImage(frame.getMapTransform().getWidth(), frame.getMapTransform().getHeight(), BufferedImage.TYPE_INT_ARGB));
                mapContext.draw(frame.getMapTransform(), new NullProgressMonitor());

                //Put this new image as texture as the rectangle
                BufferedImage im = frame.getMapTransform().getImage();
                PImage image = new PImage(im);
                this.setTexture(image);
        }

        /**
         * Get the MapContext with the layers from the configuration file
         * @return the map context
         * @throws IllegalStateException
         * @throws LayerException
         */
        private static MapContext loadMapContext()
                throws IllegalStateException, LayerException {
                MapContext mapContext = new OwsMapContext();
                InputStream fileContent = Map.class.getResourceAsStream("Layers.ows");
                mapContext.read(fileContent);
                mapContext.open(null);
                return mapContext;
        }

        /**
         * This function get the informations corresponding the the position of
         * the input vector
         *
         * @param vector the vector corresponding to the position
         * @return the information about this position (String)
         */
        public String getInfos(Vector3D vector) {
                String information = "";
                GeometryFactory gf = new GeometryFactory();
                int i = 0;

                //Create a square of 20 pixels around the touched point
                Coordinate lowerLeft = this.convert(new Vector3D(vector.getX() - 10, vector.getY() + 10));
                Coordinate upperRight = this.convert(new Vector3D(vector.getX() + 10, vector.getY() - 10));
                Coordinate lowerRight = this.convert(new Vector3D(vector.getX() + 10, vector.getY() + 10));
                Coordinate upperLeft = this.convert(new Vector3D(vector.getX() - 10, vector.getY() - 10));

                LinearRing envelopeShell = gf.createLinearRing(new Coordinate[]{
                                lowerLeft, upperLeft, upperRight, lowerRight, lowerLeft,});
                Geometry geomEnvelope = gf.createPolygon(envelopeShell,
                        new LinearRing[0]);

                //Look for information in all the visible layers (stop when information is found)
                while ((information.isEmpty()) && i < mapContext.getLayers().length) {
                        if (mapContext.getLayers()[i].isVisible()) {
                                information = getInfos(mapContext.getLayers()[i], geomEnvelope);
                        }
                        i++;
                }

                //If no information was found, we return this message
                if (information.isEmpty()) {
                        information = "No Information Available";
                }

                //Return the string (without the last \n)
                return information.trim();
        }

        /**
         * This method return the information present in the layer and the
         * square in parameter
         *
         * @param layer the layer in which we look for information
         * @param geomEnvelope the square in which to look
         * @return the information
         */
        public String getInfos(ILayer layer, Geometry geomEnvelope) {
                String information = "";
                int i;
                String sql;
                try {
                        DataSource dataSourceInitial = layer.getDataSource();

                        //Get the DataSource corresponding to the square in dataSourceSquare
                        WKTWriter writer = new WKTWriter();
                        sql = "select * from " + layer.getName() + " where ST_intersects("
                                + dataSourceInitial.getMetadata().getFieldName(dataSourceInitial.getSpatialFieldIndex()) + ", ST_geomfromtext('"
                                + writer.write(geomEnvelope) + "'));";
                        DataSource dataSourceSquare = ((DataManager) Services
                                .getService(DataManager.class)).getDataSourceFactory()
                                .getDataSourceFromSQL(sql);
                        dataSourceSquare.open();

                        //Put the information from dataSourceSquare in the variable if only one line match the touched rectangle
                        switch ((int) (dataSourceSquare.getRowCount())) {
                                case 0:
                                        information = "";
                                        break;
                                case 1:
                                        for (i = 1; i < dataSourceSquare.getFieldCount(); i++) {
                                                information = information + dataSourceSquare.getFieldName(i) + " : " + dataSourceSquare.getFieldValue(0, i).toString() + "\n";
                                        }
                                        ;
                                        break;
                                default:
                                        information = "Zoom to have more precise informations";
                                        break;
                        }
                } catch (DriverLoadException e) {
                        throw new RuntimeException(e);
                } catch (DataSourceCreationException e) {
                        e.printStackTrace();
                        information = "No Information Available";
                } catch (DriverException e) {
                        e.printStackTrace();
                        information = "No Information Available";
                } catch (ParseException e) {
                        e.printStackTrace();
                        information = "No Information Available";
                }

                return information;
        }

        /**
         * Returns a thumbnail of the selected layer Used to texture the layer
         * list
         *
         * @param layer the layer from which we want the thumbnail
         * @return the thumbnail
         */
        public PImage getThumbnail(ILayer layer) {

                Envelope startExtent = frame.getMapTransform().getExtent();
                ILayer[] layers = mapContext.getLayers();
                Boolean[] layerState = new Boolean[layers.length];

                //Set only the selected layer visible and saves the mapTransform state to set it back later
                for (int i = 0; i < layers.length; i++) {
                        layerState[i] = layers[i].isVisible();
                        if (layer == layers[i]) {
                                frame.getMapTransform().setExtent(layers[i].getEnvelope());
                                try {
                                        layers[i].setVisible(true);
                                } catch (LayerException ex) {
                                }
                        } else {
                                try {
                                        layers[i].setVisible(false);
                                } catch (LayerException ex) {
                                }
                        }
                }

                //getting the thumbnail
                frame.getMapTransform().setImage(new BufferedImage(frame.getMapTransform().getWidth(), frame.getMapTransform().getHeight(), BufferedImage.TYPE_INT_ARGB));
                mapContext.draw(frame.getMapTransform(), new NullProgressMonitor(), layer);
                BufferedImage im = frame.getMapTransform().getImage();
                PImage image = new PImage(im);

                //reset  the map transform at its original state
                for (int i = 0; i < layers.length; i++) {
                        try {
                                layers[i].setVisible(layerState[i]);
                        } catch (LayerException ex) {
                        }
                }
                frame.getMapTransform().setExtent(startExtent);
                return image;
        }

        /**
         * Calculates and sets the new envelope of the map thanks to the zoom
         * parameters. Helps zooming around a point between the user's two
         * fingers.
         *
         * @param scaleFactor
         * @param gesture
         */
        public void scale(float scaleFactor, MTGestureEvent gesture, Vector3D centerPos, float width, float height) {
                Envelope extent = frame.getMapTransform().getExtent();

                double dx = (mtApplication.width/2 - centerPos.x) * extent.getWidth() / width;
                double dy = (mtApplication.height/2 - centerPos.y) * extent.getHeight() / height;

                double minX = extent.getMinX()+extent.getWidth()/2*(1-scaleFactor) + dx;
                double maxX = extent.getMaxX()-extent.getWidth()/2*(1-scaleFactor) + dx;
                double minY = extent.getMinY()+extent.getHeight()/2*(1-scaleFactor)- dy;
                double maxY = extent.getMaxY()-extent.getHeight()/2*(1-scaleFactor)- dy;

                //Set the new extent and get the new image
                frame.getMapTransform().setExtent(new Envelope(minX, maxX, minY, maxY));
                frame.getMapTransform().setImage(new BufferedImage(frame.getMapTransform().getWidth(), frame.getMapTransform().getHeight(), BufferedImage.TYPE_INT_ARGB));
                mapContext.draw(frame.getMapTransform(), new NullProgressMonitor());
                BufferedImage im = frame.getMapTransform().getImage();
                PImage image = new PImage(im);
                this.setTexture(image);
        }

        /**
         * This method change the state (visible or not visible) of the layer
         * whose name is in parameter
         *
         * @param label the name of the layer
         * @return visible boolean saying if the layer is (or not) visible
         */
        public boolean changeLayerState(String label) {
                boolean visible = false;
                //Look for the layer corresponding to the name
                for (ILayer layer : mapContext.getLayers()) {
                        if (layer.getName().equals(label)) {
                                try {
                                        layer.setVisible(!layer.isVisible());

                                } catch (LayerException e) {
                                        e.printStackTrace();
                                }
                                visible = layer.isVisible();
                        }
                }
                
                //Draw the new image in the rectangle
                frame.getMapTransform().setImage(new BufferedImage(frame.getMapTransform().getWidth(), frame.getMapTransform().getHeight(), BufferedImage.TYPE_INT_ARGB));
                mapContext.draw(frame.getMapTransform(), new NullProgressMonitor());
                BufferedImage im = frame.getMapTransform().getImage();
                PImage image = new PImage(im);
                this.setTexture(image);

                return visible;
        }

        /**
         * This method return the coordinate corresponding to the point of the
         * screen (in pixels) in parameter
         *
         * @param vector the point in pixel
         * @return coord the corresponding coordinate
         */
        public Coordinate convert(Vector3D vector) {
                Envelope extent = frame.getMapTransform().getExtent();

                double x = extent.getMinX() + (vector.getX() + mtApplication.width * (buffersize - 1) / 2) * extent.getWidth() / (mtApplication.width * buffersize);
                double y = extent.getMinY() + (mtApplication.height - vector.getY() + mtApplication.height * (buffersize - 1) / 2) * extent.getHeight() / (mtApplication.height * buffersize);

                Coordinate coord = new Coordinate(x, y);
                return coord;
        }

        /**
         * Returns the map width in pixels
         *
         * @return
         */
        public float getWidth() {
                return super.getWidthXYGlobal();
        }

		public float getHeight() {
            return super.getHeightXYGlobal();
		}
}
