package org.orbisgis.tinterface.main;

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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKTWriter;

import processing.core.PImage;

/**
 * Constructor of the class Map
 * 
 * @author patrick
 * 
 */
public class Map extends MTRectangle {
	
	public final MainFrame frame;
	public final MapContext mapContext;
	public MTApplication mtApplication;
	
	public Map(MTApplication mtApplication, MainScene mainScene, float buffersize)
			throws Exception {
		super(mtApplication, mtApplication.width*buffersize, mtApplication.height*buffersize);
		this.setNoStroke(true);
		this.setPositionGlobal(new Vector3D(mtApplication.width/2, mtApplication.height/2));
		this.mtApplication=mtApplication;
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
        int i;
        for (i=0;i<mapContext.getLayers().length;i++){
        	System.out.println(mapContext.getLayers()[i].getName());
        }


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

	private static MapContext getSampleMapContext()
			throws IllegalStateException, LayerException {
		MapContext mapContext = new OwsMapContext();
		InputStream fileContent = Map.class.getResourceAsStream("Iris.ows");
		mapContext.read(fileContent);
		mapContext.open(null);
		return mapContext;
	}

	/**
	 * This function get the informations corresponding the the position of the input vector
	 * @param vector the vector corresponding the the position
	 * @return the information about this position (String)
	 * @throws DriverException 
	 * @throws ParseException 
	 * @throws DataSourceCreationException 
	 */
	public String getInfos(Vector3D vector) throws DriverException, DataSourceCreationException, ParseException {
		ILayer layer = mapContext.getLayers()[2];
		DataSource sds = layer.getDataSource();
		Envelope extent = frame.mapTransform.getExtent();
		String sql = null;
		GeometryFactory gf = new GeometryFactory();
		double minx = extent.getMinX()+vector.getX()*extent.getWidth()/mtApplication.width-10;
		double miny = extent.getMinY()+vector.getY()*extent.getHeight()/mtApplication.height-10;
		double maxx = extent.getMinX()+vector.getX()*extent.getWidth()/mtApplication.width+10;
		double maxy = extent.getMinY()+vector.getY()*extent.getHeight()/mtApplication.height+10;

		Coordinate lowerLeft = new Coordinate(minx, miny);
		Coordinate upperRight = new Coordinate(maxx, maxy);
		LinearRing envelopeShell = gf.createLinearRing(new Coordinate[] {
		lowerLeft, new Coordinate(minx, maxy), upperRight,
		new Coordinate(maxx, miny), lowerLeft, });
		Geometry geomEnvelope = gf.createPolygon(envelopeShell,
		new LinearRing[0]);
		WKTWriter writer = new WKTWriter();
		sql = "select * from " + layer.getName() + " where ST_intersects("
		+ sds.getMetadata().getFieldName(sds.getSpatialFieldIndex()) + ", ST_geomfromtext('"
		+ writer.write(geomEnvelope) + "'));";
		DataSource sds2 = ((DataManager) Services
				.getService(DataManager.class)).getDataSourceFactory()
				.getDataSourceFromSQL(sql);
		sds2.open();
		int i;
		String result = "";
		System.out.println("Nb lignes : "+sds2.getRowCount());
		if (sds2.getRowCount()==0){
			result = "No Information Available";
		}
		else{
			for (i=1;i<sds2.getFieldCount();i++){
				result = result + sds2.getFieldName(i)+" : "+sds2.getFieldValue(0, i).toString()+"\n";
			}
		}

		return result;
//		} catch (DriverLoadException e) {
//			throw new RuntimeException(e);
////		} catch (DataSourceCreationException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
//		} catch (DriverException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
////		} catch (ParseException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
//		}
////				try {
////				Services.getService(InformationManager.class)
////				.setContents(ds);
////				} catch (DriverException e) {
////				Services.getErrorManager().error(
////				"Cannot show the data", e);
////				}

	}

	public PImage getThumbnail() {
		BufferedImage im = frame.mapTransform.getImage();
		PImage image = new PImage(im);
		return image;
	}

	public void scale(float scaleFactorX, float scaleFactorY) {
		Envelope extent = frame.mapTransform.getExtent();
		frame.mapTransform.setExtent(
				new Envelope(extent.getMinX()+(scaleFactorX-1)*extent.getWidth(), extent.getMaxX()-(scaleFactorX-1)*extent.getWidth(),
					extent.getMinY()+(scaleFactorY-1)*extent.getHeight(), extent.getMaxY()-(scaleFactorY-1)*extent.getHeight()));
		frame.mapTransform.setImage(new BufferedImage(frame.mapTransform.getWidth(), frame.mapTransform.getHeight(), BufferedImage.TYPE_INT_ARGB));
        mapContext.draw(frame.mapTransform, new NullProgressMonitor());

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
}
