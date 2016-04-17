package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import lighting.woe.shapeproject.Constants;
import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.SolidProgram;

import static lighting.woe.shapeproject.Constants.BYTES_PER_SHORT;
import static lighting.woe.shapeproject.Constants.VERTEX_DIMENS;

public class SolidDrawListShape implements GLShape {
    final FloatBuffer mVertexBuffer;
    final ShortBuffer mDrawListBuffer;
    final float[] mColor;

    private SolidDrawListShape(
            FloatBuffer mVertexBuffer, ShortBuffer mDrawListBuffer,
            float[] mColor) {

        this.mVertexBuffer = mVertexBuffer;
        this.mDrawListBuffer = mDrawListBuffer;
        this.mColor = mColor;
    }

    @Override
    public void draw(float[] mvpMatrix, AbstractProgram program) {
        if (program instanceof SolidProgram) {
            SolidProgram solidProgram = (SolidProgram) program;
            solidProgram.useProgram();

            // set vertex data
            GLES20.glEnableVertexAttribArray(
                    solidProgram.getPositionHandle());
            GLES20.glVertexAttribPointer(
                    solidProgram.getPositionHandle(),
                    VERTEX_DIMENS, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            // set transform matrix
            GLES20.glUniformMatrix4fv(
                    solidProgram.getMVPMatrixHandle(),
                    1, false, mvpMatrix, 0);

            // set color
            GLES20.glUniform4fv(solidProgram.getColorHandle(), 1, mColor, 0);

            // draw triangles
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    mDrawListBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

            // clean up
            GLES20.glDisableVertexAttribArray(solidProgram.getPositionHandle());
        }
    }

    @Override
    public boolean isTextured() {
        return false;
    }

    public static class Builder {

        FloatBuffer mVertexBuffer;
        float[] mColor;
        ShortBuffer mDrawListBuffer;
        int[] mDrawListArray;
        Collection<PointF> mVertexCollection;

        public Builder setVertexBuffer(FloatBuffer buffer) {
            mVertexBuffer = buffer;
            return this;
        }

        public Builder putVertices(PointF... vertices) {
            if (null == mVertexCollection) {
                mVertexCollection = new ArrayList<>(Arrays.asList(vertices));
            } else {
                mVertexCollection.addAll(Arrays.asList(vertices));
            }
            return this;
        }

        public Builder setColor(GLColor color) {
            mColor = color.rgbaArray();
            return this;
        }

        public Builder setDrawListBuffer(ShortBuffer buffer) {
            mDrawListBuffer = buffer;
            return this;
        }

        public Builder setDrawList(int... vertexIndices) {
            mDrawListArray = vertexIndices;
            return this;
        }

        public SolidDrawListShape build() {
            if (null == mDrawListBuffer && null != mDrawListArray) {
                ByteBuffer dlb = ByteBuffer.allocateDirect(mDrawListArray.length * BYTES_PER_SHORT);
                dlb.order(ByteOrder.nativeOrder());
                ShortBuffer drawListBuffer = dlb.asShortBuffer();

                for (int index : mDrawListArray) {
                    drawListBuffer.put((short) index);
                }

                drawListBuffer.position(0);
                mDrawListBuffer = drawListBuffer;
            }

            if (null == mVertexBuffer && null != mVertexCollection) {
                ByteBuffer bb = ByteBuffer.allocateDirect(
                        VERTEX_DIMENS * mVertexCollection.size() * Constants.BYTES_PER_FLOAT);
                bb.order(ByteOrder.nativeOrder());
                FloatBuffer vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.position(0);

                for (PointF v : mVertexCollection) {
                    vertexBuffer.put(v.x);
                    vertexBuffer.put(v.y);
                    vertexBuffer.put(0);
                }
                vertexBuffer.position(0);

                mVertexBuffer = vertexBuffer;
            }

            return new SolidDrawListShape(mVertexBuffer, mDrawListBuffer, mColor);
        }

    }
}

