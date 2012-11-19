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

import java.io.File;
import java.io.InputStream;
import javax.swing.SwingUtilities;
import org.orbisgis.core.context.main.MainContext;
import org.orbisgis.core.layerModel.MapContext;
import org.orbisgis.core.layerModel.OwsMapContext;
import org.orbisgis.core.workspace.CoreWorkspace;

/**
 * Entry point for testing tview project.
 * tview must be used by the orbisgis-mtouch or orbisgis-view
 */
final class Main 
{
    /**
     * Utility class
     */
    private Main() {
        
    }
    
    private static MapContext getSampleMapContext()  throws Exception {
        MapContext mapContext = new OwsMapContext();
        InputStream fileContent = Main.class.getResourceAsStream("Iris.ows");
        mapContext.read(fileContent);
        mapContext.open(null);
        return mapContext;        
    }
    /**
    * First function call
    */
    public static void main( String[] args ) throws Exception
    {
        MainContext.initConsoleLogger(true);
        CoreWorkspace workspace = new CoreWorkspace();
        File workspaceFolder = new File(System.getProperty("user.home"),"OrbisGIS_MT"+File.separator);
        workspace.setWorkspaceFolder(workspaceFolder.getAbsolutePath());
        MainContext mainContext = new MainContext(true,workspace,true);
        final MainFrame frame = new MainFrame();
        final MapContext mapContext = getSampleMapContext();
        
        SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                            // Load the sample map context
                            frame.init(mapContext);
                            frame.setVisible(true);
                    }
            });
    }
}