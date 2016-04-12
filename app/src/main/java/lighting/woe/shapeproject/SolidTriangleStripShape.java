package lighting.woe.shapeproject;

import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;

import static lighting.woe.shapeproject.Constants.BYTES_PER_FLOAT;
import static lighting.woe.shapeproject.Constants.COORDS_PER_VERTEX;

public class SolidTriangleStripShape extends TriangleStripShape {
    private final float[] mColor;

    SolidTriangleStripShape(int color, PointF... v) {
        super(v);

        mColor = new float[]{
                Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f,
                Color.alpha(color) / 255f,
        };
    }

    @Override
    public void draw(float[] mvpMatrix, int program) {
         GLES20.glUseProgram(program);
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);

        int matrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(
                positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX * BYTES_PER_FLOAT,
                mVertexBuffer);

        int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        GLES20.glUniform4fv(colorHandle, 1, mColor, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount * COORDS_PER_VERTEX);

        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    @Override
    public boolean isTextured() {
        return false;
    }
}
