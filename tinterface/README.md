orbisgis-multi-touch / tinterface
=================================

OrbisGIS GUI that target multi-touch devices

Installation instructions :
---------------------------

- Modify the Layers.ows file in the ressources part of tinterface to indicate the files you want to use for the map //need more details
- Build the application : mvn compile (in the tinterface folder)
- If you're not using Windows 7, you can directly launch the application with : mvn exec:exec (in the tinterface folder)
- If you're using Windows 7 :
	- You need to create a runnable jar : mvn assembly:assembly (in the tinterface folder)
	- You need to download native library (in order to use the multi-touch integrated with Windows 7) :
		- https://code.google.com/p/mt4j/source/browse/trunk/Win7Touch.dll
		- https://code.google.com/p/mt4j/source/browse/trunk/Win7Touch64.dll
	- To launch the jar, execute in the folder containing the jar (normally tinterface/target) :
		java -Djava.library.path=/folder/containing/downloaded/native/library -Dfile.encoding=UTF-8 -jar orbisgis-tinterface-1.0-jar-with-dependencies.jar
