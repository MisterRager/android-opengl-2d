package lighting.woe.shapeproject;

import android.graphics.PointF;

public class Quad implements GLShape {
    private final Triangle[] mTriangles;

    public Quad(PointF v1, PointF v2, PointF v3, PointF v4, int color) {
        mTriangles = new Triangle[]{
                new Triangle(v1, v2, v3, color),
                new Triangle(v3, v1, v4, color),
        };
    }

    @Override
    public void draw(float[] mvpMatrix, int program) {
        for (Triangle t : mTriangles) {
            t.draw(mvpMatrix, program);
        }
    }

    @Override
    public boolean isTextured() {
        return false;
    }
}
