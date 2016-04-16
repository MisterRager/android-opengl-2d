package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import lighting.woe.shapeproject.Constants;
import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.SolidProgram;

import static lighting.woe.shapeproject.Constants.BYTES_PER_SHORT;

public class SolidDrawListShape implements GLShape {
    final FloatBuffer mVertexBuffer;
    final ShortBuffer mDrawListBuffer;
    final float[] mColor;
    final int mVertexCount;

    private SolidDrawListShape(
            FloatBuffer mVertexBuffer, ShortBuffer mDrawListBuffer,
            float[] mColor, int mVertexCount) {

        this.mVertexBuffer = mVertexBuffer;
        this.mDrawListBuffer = mDrawListBuffer;
        this.mColor = mColor;
        this.mVertexCount = mVertexCount;
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
                    3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            // set transform matrix
            GLES20.glUniformMatrix4fv(
                    solidProgram.getMVPMatrixHandle(),
                    1, false, mvpMatrix, 0);

            // set color
            GLES20.glUniform4fv(solidProgram.getColorHandle(), 1, mColor, 0);

            // draw triangles
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    mVertexCount, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

            // clean up
            GLES20.glDisableVertexAttribArray(solidProgram.getPositionHandle());
        }
    }

    @Override
    public boolean isTextured() {
        return false;
    }

    public static class Builder {

        private FloatBuffer mVertexBuffer;
        private float[] mColor;
        private int mVertexCount;
        private ShortBuffer mDrawListBuffer;

        public Builder setVertexBuffer(FloatBuffer buffer) {
            mVertexBuffer = buffer;
            return this;
        }

        public Builder putVertices(PointF... vertices) {
            float vertexArray[] = new float[vertices.length * 3];
            int i = 0;
            for (PointF v : vertices) {
                vertexArray[i++] = v.x;
                vertexArray[i++] = v.y;
                vertexArray[i++] = 0;
            }

            ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length * Constants.BYTES_PER_FLOAT);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(vertexArray);
            vertexBuffer.position(0);

            return setVertexBuffer(vertexBuffer);
        }

        public Builder setColor(int r, int g, int b, int a){
            return setColor(r / 255f, g / 255f, b / 255f, a / 255f);
        }

        public Builder setColor(float r, float g, float b, float a) {
            mColor = new float[]{r, g, b, a};
            return this;
        }

        public Builder setDrawListBuffer(ShortBuffer buffer, int drawListLength){
            mDrawListBuffer = buffer;
            mVertexCount = drawListLength;
            return this;
        }

        public Builder setDrawList(short... vertexIndices) {
            ByteBuffer dlb = ByteBuffer.allocateDirect(vertexIndices.length * BYTES_PER_SHORT);
            dlb.order(ByteOrder.nativeOrder());
            ShortBuffer drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(vertexIndices);
            drawListBuffer.position(0);

            return setDrawListBuffer(drawListBuffer, vertexIndices.length);
        }

        public SolidDrawListShape build() {
            return new SolidDrawListShape(mVertexBuffer, mDrawListBuffer, mColor, mVertexCount);
        }

        public FloatBuffer getVertexBuffer() {
            return mVertexBuffer;
        }

        public float[] getColor() {
            return mColor;
        }

        public int getVertexCount() {
            return mVertexCount;
        }

        public ShortBuffer getDrawListBuffer() {
            return mDrawListBuffer;
        }

    }
}

