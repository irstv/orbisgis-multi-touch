package org.orbisgis.tinterface.main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.layerModel.OwsMapContext;
import org.orbisgis.core.map.MapTransform;
import org.orbisgis.core.workspace.CoreWorkspace;
import org.orbisgis.progress.NullProgressMonitor;

import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Constructor of the class Map
 * 
 * @author patrick
 * 
 */
public class Map extends MTRectangle {
	public Map(MTApplication mtApplication, MainScene mainScene)
			throws Exception {
		super(mtApplication, 1300, 710);
		this.unregisterAllInputProcessors();
		this.removeAllGestureEventListeners();

		MainContext.initConsoleLogger(true);
		CoreWorkspace workspace = new CoreWorkspace();
		File workspaceFolder = new File(System.getProperty("user.home"),
				"OrbisGIS_MT" + File.separator);
		workspace.setWorkspaceFolder(workspaceFolder.getAbsolutePath());
		MainContext mainContext = new MainContext(true, workspace, true);
		final MainFrame frame = new MainFrame();
		final MapContext mapContext = getSampleMapContext();
		frame.init(mapContext);
        mapContext.draw(frame.mapTransform, new NullProgressMonitor());


		BufferedImage im = frame.mapTransform.getImage();
		PImage image = new PImage(im);

		//this.setFillColor(new MTColor(0, 255, 0));
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
		// TODO Auto-generated method stub
                

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
		InputStream fileContent = Map.class.getResourceAsStream("Iris.ows");
		mapContext.read(fileContent);
		mapContext.open(null);
		return mapContext;
	}

	/**
	 * This function get the informations corresponding the the position of the input vector
	 * @param vector the vector corresponding the the position
	 * @return the information about this position (String)
	 */
	public String getInfos(Vector3D vector) {
		// TODO Auto-generated method stub
		return "Text test";
	}
}
