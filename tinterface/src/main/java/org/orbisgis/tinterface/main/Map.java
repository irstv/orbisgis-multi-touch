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
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleEvent;
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
 * @author patrick
 *
 */
public class Map extends MTRectangle {

	
	public final MainFrame frame;
	public final MapContext mapContext;
	public MTApplication mtApplication;
	public float buffersize;
	
	public Map(MTApplication mtApplication, MainScene mainScene, float buffersize)
			throws Exception {
		super(mtApplication, mtApplication.width*buffersize, mtApplication.height*buffersize);
		this.setNoStroke(true);
		this.setPositionGlobal(new Vector3D(mtApplication.width/2, mtApplication.height/2));
		this.mtApplication=mtApplication;
		this.buffersize = buffersize;
		this.unregisterAllInputProcessors();
		this.removeAllGestureEventListeners();

		MainContext.initConsoleLogger(true);
		CoreWorkspace workspace = new CoreWorkspace();
		File workspaceFolder = new File(System.getProperty("user.home"),
				"OrbisGIS_MT" + File.separator);
		workspace.setWorkspaceFolder(workspaceFolder.getAbsolutePath());
		MainContext mainContext = new MainContext(true, workspace, true);
		frame = new MainFrame();
		mapContext = getSampleMapContext();
		frame.init(mapContext, (int)(mtApplication.width*buffersize), (int)(mtApplication.height*buffersize));
		Envelope extent = frame.mapTransform.getExtent();
		double facteur = (buffersize-1)/2;
		frame.mapTransform.setExtent(
				new Envelope(extent.getMinX() - facteur*extent.getWidth(), extent.getMaxX() + facteur*extent.getWidth(),
					extent.getMinY() - facteur*extent.getHeight(), extent.getMaxY() + facteur*extent.getHeight()));
	

        mapContext.draw(frame.mapTransform, new NullProgressMonitor());

		BufferedImage im = frame.mapTransform.getImage();
		PImage image = new PImage(im);

		this.setTexture(image);
		mainScene.getCanvas().addChild(this);
	}

	/**
	 * Method used to move the map from the parameter in input
	 * 
	 * @param x
	 *            the number of pixel the map need to be moved (in x)
	 * @param y
	 *            the number of pixel the map need to be moved (in y)
	 */
	public void move(float x, float y) {
                
		Envelope extent = frame.mapTransform.getExtent();
		double dx = x*extent.getWidth()/(this.getWidthXYGlobal());
		double dy = y*extent.getHeight()/(this.getHeightXYGlobal());
		frame.mapTransform.setExtent(
				new Envelope(extent.getMinX() - dx, extent.getMaxX() - dx,
					extent.getMinY() + dy, extent.getMaxY() + dy));
		frame.mapTransform.setImage(new BufferedImage(frame.mapTransform.getWidth(), frame.mapTransform.getHeight(), BufferedImage.TYPE_INT_ARGB));

        mapContext.draw(frame.mapTransform, new NullProgressMonitor());

		BufferedImage im = frame.mapTransform.getImage();
		PImage image = new PImage(im);
		this.setTexture(image);

	}
        
        public void addLayer(File layer) throws LayerException {
                //Adding layer reference to the OWS file
                Map.getSampleMapContext();
        }
        
        public void removeLayer(File layer) throws LayerException {
                //deleting reference from the OWS file
                Map.getSampleMapContext();
        }

	private static MapContext getSampleMapContext()
			throws IllegalStateException, LayerException {
		MapContext mapContext = new OwsMapContext();
		InputStream fileContent = Map.class.getResourceAsStream("Layers.ows");
		mapContext.read(fileContent);
		mapContext.open(null);
		return mapContext;
	}

	/**
	 * This function get the informations corresponding the the position of the input vector
	 * @param vector the vector corresponding to the position
	 * @return the information about this position (String)
	 */
	public String getInfos(Vector3D vector){
		String information = "";
		GeometryFactory gf = new GeometryFactory();
		int i=0;
			
		//Create a square of 20 pixels around the touched point
		Coordinate lowerLeft = this.convert( new Vector3D(vector.getX()-10, vector.getY()+10));
		Coordinate upperRight = this.convert( new Vector3D(vector.getX()+10, vector.getY()-10));
		Coordinate lowerRight = this.convert( new Vector3D(vector.getX()+10, vector.getY()+10));
		Coordinate upperLeft = this.convert( new Vector3D(vector.getX()-10, vector.getY()-10));

		LinearRing envelopeShell = gf.createLinearRing(new Coordinate[] {
				lowerLeft, upperLeft, upperRight, lowerRight, lowerLeft, });
		Geometry geomEnvelope = gf.createPolygon(envelopeShell,
				new LinearRing[0]);
		
		//Look for information in all the visible layers (stop when information is found)
		while ((information.equals("")) && i<mapContext.getLayers().length){
			if (mapContext.getLayers()[i].isVisible()){
				information=getInfos(mapContext.getLayers()[i], geomEnvelope);
			}
			i++;
		}
		
		//If no information was found, we return this message
		if (information.equals("")){
			information = "No Information Available";
		}
		
		//Return the string (without the last \n)
		return information.trim();
	}
	
	/**
	 * This method return the information present in the layer and the square in parameter 
	 * @param layer the layer in which we look for information
	 * @param geomEnvelope the square in which to look
	 * @return the information
	 */
	public String getInfos(ILayer layer, Geometry geomEnvelope){
		String information = "";
		int i;
		String sql = null;
		System.out.println(layer.getName());
		try{
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
			switch ((int)(dataSourceSquare.getRowCount())){
			case 0 :
				information = "";
				break;
			case 1 : 			
				for (i=1;i<dataSourceSquare.getFieldCount();i++){
					information = information + dataSourceSquare.getFieldName(i)+" : "+dataSourceSquare.getFieldValue(0, i).toString()+"\n";
				};
				break;
			default : information = "Zoom to have more precise informations"; break;
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
         * Returns a thumbnail of the selected layer
         * Used to texture the layer list
         * @param layer
         * @return
         */
        public PImage getThumbnail(ILayer layer) {
            
        Envelope startExtent = frame.mapTransform.getExtent();
            
        ILayer[] layers = mapContext.getLayers();
        
        Boolean[] layerState = new Boolean[layers.length];
        
        //Set only the selected layer visible and saves the mapTransform state to set it back later
        for (int i=0; i < layers.length; i++) {
                layerState[i] = layers[i].isVisible();
                if (layer == layers[i]) {
                        frame.mapTransform.setExtent(layers[i].getEnvelope());
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
        frame.mapTransform.setImage(new BufferedImage(frame.mapTransform.getWidth(), frame.mapTransform.getHeight(), BufferedImage.TYPE_INT_ARGB));
        mapContext.draw(frame.mapTransform, new NullProgressMonitor(), layer);
        BufferedImage im = frame.mapTransform.getImage();
        PImage image = new PImage(im);
        
        //reset of the map transform at its original state
        for (int i=0; i < layers.length; i++) {
                    try {
                            layers[i].setVisible(layerState[i]);
                    } catch (LayerException ex) {
                    }
        }
        
        frame.mapTransform.setExtent(startExtent);
        return image;
    }

        /**
         * Calculates and sets the new envelope of the map thanks to the zoom parameters.
         * @param scaleFactor
         * @param gesture
         */
        public void scale(float scaleFactor, MTGestureEvent gesture) {
        Envelope extent = frame.mapTransform.getExtent();
                //System.out.println("xmoy : " + xmoy + "\nxdecal : " + xdecal + "\nminx de base : " + ((extent.getMinX() + (scaleFactorX - 1) * extent.getWidth())) + "\nminx : " + ((extent.getMinX() + (scaleFactorX - 1) * extent.getWidth()) + xdecal));
                //sframe.mapTransform.setExtent(new Envelope(c1., scaleFactorY, scaleFactorY, scaleFactorY));
                System.out.println(frame.mapTransform.getExtent());
                //Getting the start coordinates of the zoom center
                Vector3D scalingPoint = ((ScaleEvent) gesture).getScalingPoint();
                
                Coordinate startCoord = convert(scalingPoint);
                
                float minX = (float) (startCoord.x - (startCoord.x - extent.getMinX())*scaleFactor*buffersize);
                float maxX = (float) (startCoord.x - (startCoord.x - extent.getMaxX())*scaleFactor*buffersize);
                float minY = (float) (startCoord.y - (startCoord.y - extent.getMinY())*scaleFactor*buffersize);
                float maxY = (float) (startCoord.y - (startCoord.y - extent.getMaxY())*scaleFactor*buffersize);
                
                frame.mapTransform.setExtent( new Envelope(minX, maxX, minY, maxY));
        
                frame.mapTransform.setImage(new BufferedImage(frame.mapTransform.getWidth(), frame.mapTransform.getHeight(), BufferedImage.TYPE_INT_ARGB));
                mapContext.draw(frame.mapTransform, new NullProgressMonitor());
                System.out.println(frame.mapTransform.getExtent());
                BufferedImage im = frame.mapTransform.getImage();
                PImage image = new PImage(im);
                
                this.setTexture(image);	
	}

	/**
	 * This method change the state (visible or not visible) of the layer whose name is in parameter
	 * @param label the name of the layer
	 */
	public boolean changeLayerState(String label) {
		boolean visible=false;
		for(ILayer layer:mapContext.getLayers()) {
			if (layer.getName().equals(label)){
				try {
					layer.setVisible(!layer.isVisible());

				} catch (LayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				visible=layer.isVisible();
			}
		}
		frame.mapTransform.setImage(new BufferedImage(frame.mapTransform.getWidth(), frame.mapTransform.getHeight(), BufferedImage.TYPE_INT_ARGB));
		mapContext.draw(frame.mapTransform, new NullProgressMonitor());
		BufferedImage im = frame.mapTransform.getImage();
		PImage image = new PImage(im);
		this.setTexture(image);
		
		return visible;
	}
	
	/**
	 * This method return the coordinate corresponding to the point of the screen (in pixels) in parameter
	 * @param vector the point in pixel
	 * @return coord the corresponding coordinate
	 */
	public Coordinate convert(Vector3D vector){
		Envelope extent = frame.mapTransform.getExtent();
		
		double x = extent.getMinX()+(vector.getX() + mtApplication.width*(buffersize-1)/2)*extent.getWidth()/(mtApplication.width*buffersize);
		double y = extent.getMinY()+(mtApplication.height-vector.getY()+mtApplication.height*(buffersize-1)/2)*extent.getHeight()/(mtApplication.height*buffersize);

		Coordinate coord = new Coordinate(x, y);
		return coord;
	}
        
        /**
         * Returns the map width in pixels
         * @return
         */
        public float getWidth() {
                return super.getWidthXYGlobal();
        }

}
