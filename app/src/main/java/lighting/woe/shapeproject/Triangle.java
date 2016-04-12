package lighting.woe.shapeproject;

import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.SolidProgram;

public class Triangle implements GLShape{

    private final float[] mColor;
    private FloatBuffer vertexBuffer;

    // number of coordinates per solid_vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    public Triangle(PointF v1, PointF v2, PointF v3, int color){
        this(
                new float[]{
                        v1.x, v1.y, 0,
                        v2.x, v2.y, 0,
                        v3.x, v3.y, 0,
                },
                new float[]{
                        Color.red(color) / 255f,
                        Color.green(color) / 255f,
                        Color.blue(color) / 255f,
                        Color.alpha(color) / 255f,
                });

    }

    private Triangle(float triangleCoords[], float color[]) {
        // initialize solid_vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * Constants.BYTES_PER_FLOAT);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        mColor = color;
    }

    @Override
    public void draw(float[] mvpMatrix, AbstractProgram program){
        SolidProgram solidProgram = (SolidProgram) program;
        solidProgram.useProgram();
        GLES20.glEnableVertexAttribArray(solidProgram.getPositionHandle());

        GLES20.glUniformMatrix4fv(solidProgram.getMVPMatrixHandle(), 1, false, mvpMatrix, 0);

        GLES20.glVertexAttribPointer(
                solidProgram.getPositionHandle(), COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                COORDS_PER_VERTEX * Constants.BYTES_PER_FLOAT,
                vertexBuffer);

        GLES20.glUniform4fv(solidProgram.getColorHandle(), 1, mColor, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 9);

        GLES20.glDisableVertexAttribArray(solidProgram.getPositionHandle());
    }

    @Override
    public boolean isTextured(){
        return false;
    }
}
