package lighting.woe.shapeproject;

import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;

import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.SolidProgram;

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
    public void draw(float[] mvpMatrix, AbstractProgram program) {
        SolidProgram solidProgram = (SolidProgram) program;
        program.useProgram();
        GLES20.glEnableVertexAttribArray(solidProgram.getPositionHandle());

        GLES20.glUniformMatrix4fv(solidProgram.getMVPMatrixHandle(), 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(
                solidProgram.getPositionHandle(), COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX * BYTES_PER_FLOAT,
                mVertexBuffer);

        GLES20.glUniform4fv(solidProgram.getColorHandle(), 1, mColor, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount * COORDS_PER_VERTEX);

        GLES20.glDisableVertexAttribArray(solidProgram.getPositionHandle());
    }

    @Override
    public boolean isTextured() {
        return false;
    }
}
