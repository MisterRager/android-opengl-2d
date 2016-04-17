package lighting.woe.shapeproject.shapes;

import android.graphics.Color;

public class GLColor {
    public final float red;
    public final float green;
    public final float blue;
    public final float alpha;

    public GLColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public GLColor(int androidColor){
        this(
                Color.red(androidColor),
                Color.green(androidColor),
                Color.blue(androidColor),
                Color.alpha(androidColor));
    }

    public float[] rgbaArray(){
        return new float[]{red, green, blue, alpha};
    }
}
