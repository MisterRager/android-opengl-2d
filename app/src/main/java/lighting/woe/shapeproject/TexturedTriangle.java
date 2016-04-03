package lighting.woe.shapeproject;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TexturedTriangle implements GLShape {

    private final float[] mTriangleCoords;
    private final int mTexture;
    private final FloatBuffer mUVBuffer;
    private FloatBuffer vertexBuffer;

    // number of coordinates per solid_vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    public TexturedTriangle(PointF v1, PointF v2, PointF v3, int texture, FloatBuffer uvBuffer) {
        this(
                new float[]{
                        v1.x, v1.y, 0,
                        v2.x, v2.y, 0,
                        v3.x, v3.y, 0,},
                texture, uvBuffer);
    }

    private TexturedTriangle(float triangleCoords[], int texture, FloatBuffer uvBuffer) {
        // initialize solid_vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        mTriangleCoords = triangleCoords;
        mTexture = texture;
        mUVBuffer = uvBuffer;
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
                COORDS_PER_VERTEX * 4,
                vertexBuffer);

        //int colorHandle = GLES20.glGetUniformLocation(program, "vColor");
        //GLES20.glUniform4fv(colorHandle, 1, mColor, 0);
        int texCoordLoc = GLES20.glGetAttribLocation(program, "a_texCoord");
        GLES20.glEnableVertexAttribArray(texCoordLoc);
        GLES20.glVertexAttribPointer(texCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, mUVBuffer);

        int textureLoc = GLES20.glGetUniformLocation(program, "s_texture");
        GLES20.glUniform1i(textureLoc, mTexture);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mTriangleCoords.length);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    @Override
    public boolean isTextured() {
        return true;
    }
}
