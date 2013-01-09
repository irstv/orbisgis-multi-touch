package org.orbisgis.tinterface.main;

import java.io.File;
import java.util.Stack;
import org.mt4j.MTApplication;

/**
 * Main class from the application
 *
 * @author patrick
 *
 */
public class StartOrbisGISMultiTouch extends MTApplication {

        private static final long serialVersionUID = 1L;
        /**
         * Map description file.
         */
        private static File mapFilePath;

        private static void printUsage() {
                System.out.println("OrbisGIS MultiTouch prototype version 08/01/2013");
                System.out.println("Usage :");
                System.out.println("java -jar orbisgis-tinterface.jar [options] -m mymap.ows");
                System.out.println("Options :");
                System.out.println("-m mymap.ows : OGC Web Services file path");
        }

        /**
         * Main method. Use initialize to launch the startUp method
         *
         * @param args
         */
        public static void main(String args[]) {
                //Read parameters
                Stack<String> sargs = new Stack<String>();
                for (String arg : args) {
                        sargs.insertElementAt(arg, 0);
                }
                while (!sargs.empty()) {
                        String argument = sargs.pop();
                        if(!sargs.empty()) {
                                if (argument.contentEquals("-m")) {
                                        String filePath = sargs.pop();
                                        mapFilePath = new File(filePath);
                                        if(!mapFilePath.exists()) {
                                                System.err.println("The ows map file does not exists.");
                                        }
                                } else {
                                        System.err.println("Unknown parameter :" + argument);
                                        printUsage();
                                        return;
                                }
                        }
                }
                if (mapFilePath==null) {
                        printUsage();
                        return;
                }
                initialize();
        }

        /**
         * Create a new instance of MainScene that is added to the application
         */
        @Override
        public void startUp() {
                this.addScene(new MainScene(this, "Map scene",mapFilePath));
        }
}
