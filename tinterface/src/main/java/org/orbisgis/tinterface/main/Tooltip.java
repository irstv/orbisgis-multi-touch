package org.orbisgis.tinterface.main;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;

/**
 * Class used for creating tooltip
 * @author patrick
 *
 */
public class Tooltip extends MTTextArea{
	
	/**
	 * Create a tooltip with the corresponding parameters
	 * @param mtApplication the mtApplication
	 * @param vector the position of the tooltip
	 * @param info the text in the tooltip
	 */
	public Tooltip(MTApplication mtApplication, Vector3D vector, String info){
		super(mtApplication);
		this.setFillColor(new MTColor(0, 0, 0));
		this.setText(info);
		this.setPositionGlobal(vector);
	}

}
