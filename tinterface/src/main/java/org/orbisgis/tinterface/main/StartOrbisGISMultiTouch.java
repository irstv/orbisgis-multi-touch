package org.orbisgis.tinterface.main;

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
         * Main method. Use initialize to launch the startUp method
         *
         * @param args
         */
        public static void main(String args[]) {
                initialize();
        }

        /**
         * Create a new instance of MainScene that is added to the application
         */
        @Override
        public void startUp() {
                this.addScene(new MainScene(this, "Map scene"));
        }
}
