package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;

public class TexturePointF {
    public final PointF mPointF;
    public final float mTextureU;
    public final float mTextureV;

    public TexturePointF(PointF mPointF, float mTextureU, float mTextureV) {
        this.mPointF = mPointF;
        this.mTextureU = mTextureU;
        this.mTextureV = mTextureV;
    }
}
