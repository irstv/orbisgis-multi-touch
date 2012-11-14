orbisgis-multi-touch
====================

OrbisGIS GUI that target multi-touch devices

tview package :

The tile manager, it handle the renderer compenents of orbisgis.core
in order to render the map context transformation (drag/zoom) with a minimum framerate.

mtcontroler package :

Bridge between MT4J and tview, the tiled view component respond to multi touch commands,
 other components may be added in this controler for selecting layers and map context.