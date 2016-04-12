package lighting.woe.shapeproject;

import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

abstract public class TriangleStripShape implements GLShape {
    protected final FloatBuffer mVertexBuffer;
    protected final int mVertexCount;

    TriangleStripShape(PointF... v) {
        // initialize solid_vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                v.length * Constants.COORDS_PER_VERTEX * Constants.BYTES_PER_FLOAT);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        mVertexBuffer = bb.asFloatBuffer();

        mVertexCount = v.length;
        // add the coordinates to the FloatBuffer
        for(int k = 0; k < mVertexCount; k++){
            mVertexBuffer.put(new float[]{v[k].x, v[k].y, 0});
        }

        // set the buffer to read the first coordinate
        mVertexBuffer.position(0);
    }
}
