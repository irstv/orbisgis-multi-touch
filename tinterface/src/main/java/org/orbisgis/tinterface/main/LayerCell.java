package org.orbisgis.tinterface.main;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTImage;
import org.mt4j.components.visibleComponents.widgets.MTListCell;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.util.MTColor;
import org.mt4j.util.font.IFont;
import org.mt4j.util.math.Vector3D;
import org.orbisgis.core.layerModel.ILayer;

/**
 * Cell of the LayerList that has the properties of a MTListCell and new
 * attributes and methods to link the cell with a layer
 *
 * @author Adrien
 *
 */
public class LayerCell extends MTListCell {

        /**
         * State of the map (displayed or not)
         */
        private boolean mapAlreadyInPlace;
        /**
         * the Map/Layer
         */
        private ILayer layer;
        /**
         * the Thumbnail
         */
        private MTRectangle thumbnail;
        /**
         * the Label
         */
        private String label;
        /**
         * The actual color
         */
        private MTColor actualColor = this.getFillColor();
        /**
         * The width
         */
        private float width;

        public float getWidth() {
                return width;
        }

        public boolean isMapAlreadyInPlace() {
                return mapAlreadyInPlace;
        }

        public void setMapAlreadyInPlace(boolean mapAlreadyInPlace) {
                this.mapAlreadyInPlace = mapAlreadyInPlace;
        }

        public String getLabel() {
                return label;
        }
        // Only for initialization

        public void setLabel(String label, MTApplication mtApplication, IFont font) {
                this.label = label;

                // Label is initialized here ..
                MTTextArea listLabel = new MTTextArea(mtApplication, font);
                listLabel.setNoFill(true);
                listLabel.setNoStroke(true);
                listLabel.setText(label);
                Vector3D positionText = this.getCenterPointLocal();
                positionText.y += (((float) mtApplication.getHeight() / mtApplication.getWidth()) * this.width) / 2;
                listLabel.setPositionRelativeToParent(positionText);
                // .. and added here
                this.addChild(listLabel);

        }

        public void setThumbnail(MTImage thumbnail) {
                this.thumbnail = thumbnail;
        }

        public void setActualColor(MTColor actualColor) {
                this.setFillColor(actualColor);
                this.actualColor = actualColor;
        }

        /**
         * Constructor of our LayerCell
         *
         * @param mtApplication
         * @param font font to write the label
         * @param cellFillColor default color of the cell
         * @param cellWidth Width of the cell
         * @param cellHeight Height of the cell
         * @param layer layer that the cell represents
         * @param map
         */
        public LayerCell(ILayer layer, MTApplication mtApplication, IFont font, final MTColor cellFillColor, float cellWidth, float cellHeight, Map map) {
                super(mtApplication, cellWidth, cellHeight);
                this.layer = layer;
                this.width = cellWidth;
                setMapAlreadyInPlace(layer.isVisible());
                this.setChildClip(null);

                this.setFillColor(cellFillColor);
                // Thumbnail is added here
                thumbnail = new MTRectangle(mtApplication, 0, 1, (int) (Math.floor(cellWidth)), ((float) mtApplication.getHeight() / mtApplication.getWidth()) * cellWidth);
                thumbnail.setTexture(map.getThumbnail(layer));
                thumbnail.setStrokeWeight(0);
                this.addChild(thumbnail);

                this.setLabel(this.layer.getName(), mtApplication, font);
        }
}
