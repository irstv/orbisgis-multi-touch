package org.orbisgis.tinterface.main;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.AbstractVisibleComponent;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.layerModel.LayerException;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.layerModel.OwsMapContext;
import org.orbisgis.core.workspace.CoreWorkspace;

import processing.core.PGraphics;
import processing.core.PImage;

import com.modestmaps.providers.AbstractMapProvider;

public class Map extends MTRectangle {
	public Map(MTApplication mtApplication, MainScene mainScene) throws Exception{
        super(mtApplication, 1300, 710);
        
//        MainContext.initConsoleLogger(true);
//        CoreWorkspace workspace = new CoreWorkspace();
//        File workspaceFolder = new File(System.getProperty("user.home"), "OrbisGIS_MT" + File.separator);
//        workspace.setWorkspaceFolder(workspaceFolder.getAbsolutePath());
//        MainContext mainContext = new MainContext(true, workspace, true);
//        final MainFrame frame = new MainFrame();
//        final MapContext mapContext = getSampleMapContext();
//        BufferedImage im = frame.mapTransform.getImage();
//        PImage image = new PImage(im);

        
		this.setFillColor(new MTColor(0, 255, 0));
		this.setPickable(false);
		//this.setTexture(image);
		//this.setPositionGlobal(new Vector3D(mtApplication.width, mtApplication.height));
        //this.translateGlobal(new Vector3D(-mtApplication.width / 2f - 80, -10)); // Initializations position of the menu
        mainScene.getCanvas().addChild(this);
	}

	public void move(float x, float y) {
		// TODO Auto-generated method stub
		
	}

	public void setMapProvider(AbstractMapProvider mapProvider) {
		// TODO Auto-generated method stub
		
	}



	private static MapContext getSampleMapContext() throws IllegalStateException, LayerException {
        MapContext mapContext = new OwsMapContext();
        InputStream fileContent = Map.class.getResourceAsStream("Iris.ows");
        mapContext.read(fileContent);
        mapContext.open(null);
        return mapContext;
}
}
