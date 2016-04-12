package lighting.woe.shapeproject;

import android.graphics.PointF;

public class Quad extends SolidTriangleStripShape {
    public Quad(PointF v1, PointF v2, PointF v3, PointF v4, int color) {
        super(color, v1, v2, v3, v4);
    }
}
