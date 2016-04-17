package lighting.woe.shapeproject.shapes;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import lighting.woe.shapeproject.Constants;
import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.GradientProgram;

import static lighting.woe.shapeproject.Constants.BYTES_PER_SHORT;
import static lighting.woe.shapeproject.Constants.COLOR_DIMENS;
import static lighting.woe.shapeproject.Constants.COORDS_PER_VERTEX;

public class GradientDrawListShape implements GLShape {
    final FloatBuffer mVertexBuffer;
    final ShortBuffer mDrawListBuffer;

    private GradientDrawListShape(
            FloatBuffer mVertexBuffer, ShortBuffer mDrawListBuffer) {


        this.mVertexBuffer = mVertexBuffer;
        this.mDrawListBuffer = mDrawListBuffer;
    }

    @Override
    public void draw(float[] mvpMatrix, AbstractProgram program) {
        if (program instanceof GradientProgram) {
            GradientProgram gradientProgram = (GradientProgram) program;
            gradientProgram.useProgram();

            // set vertex data
            GLES20.glEnableVertexAttribArray(
                    gradientProgram.getPositionHandle());
            GLES20.glVertexAttribPointer(
                    gradientProgram.getPositionHandle(),
                    COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                    COLOR_DIMENS, mVertexBuffer);

            // set transform matrix
            GLES20.glUniformMatrix4fv(
                    gradientProgram.getMVPMatrixHandle(),
                    1, false, mvpMatrix, 0);

            // set color
            GLES20.glEnableVertexAttribArray(gradientProgram.getColorHandle());
            GLES20.glVertexAttribPointer(
                    gradientProgram.getColorHandle(),
                    COLOR_DIMENS, GLES20.GL_FLOAT, false,
                    COORDS_PER_VERTEX, COORDS_PER_VERTEX);

            // draw triangles
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    mDrawListBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

            // clean up
            GLES20.glDisableVertexAttribArray(gradientProgram.getPositionHandle());
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
        ArrayList<ColorPointF> mVertexCollection;

        public Builder setVertexBuffer(FloatBuffer buffer) {
            mVertexBuffer = buffer;
            return this;
        }

        /*
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
        */
        public Builder putVertices(ColorPointF... vertices) {
            if (null == mVertexCollection) {
                mVertexCollection = new ArrayList<>(Arrays.asList(vertices));
            } else {
                mVertexCollection.addAll(Arrays.asList(vertices));
            }

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

        public GradientDrawListShape build() {
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
                        (COORDS_PER_VERTEX + COLOR_DIMENS)
                                * mVertexCollection.size() * Constants.BYTES_PER_FLOAT);
                bb.order(ByteOrder.nativeOrder());
                FloatBuffer vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.position(0);

                for (ColorPointF v : mVertexCollection) {
                    vertexBuffer.put(v.point.x);
                    vertexBuffer.put(v.point.y);
                    vertexBuffer.put(0);
                    vertexBuffer.put(v.color.red);
                    vertexBuffer.put(v.color.green);
                    vertexBuffer.put(v.color.blue);
                    vertexBuffer.put(v.color.alpha);
                }
                vertexBuffer.position(0);

                mVertexBuffer = vertexBuffer;
            }

            return new GradientDrawListShape(mVertexBuffer, mDrawListBuffer);
        }

    }
}

