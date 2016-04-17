package lighting.woe.shapeproject.shapes;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.TextureProgram;

import static lighting.woe.shapeproject.Constants.*;
import static lighting.woe.shapeproject.Constants.BYTES_PER_SHORT;

public class TextureDrawListShape implements GLShape {
    final FloatBuffer mVertexBuffer, mUVBuffer;
    final ShortBuffer mDrawListBuffer;
    final String mTextureName;

    public TextureDrawListShape(
            FloatBuffer mVertexBuffer, ShortBuffer mDrawListBuffer, FloatBuffer mUVBuffer,
            String textureName) {

        this.mVertexBuffer = mVertexBuffer;
        this.mDrawListBuffer = mDrawListBuffer;
        this.mUVBuffer = mUVBuffer;
        this.mTextureName = textureName;
    }

    @Override
    public void draw(float[] mvpMatrix, AbstractProgram program) {
        if (program instanceof TextureProgram) {
            TextureProgram textureProgram = (TextureProgram) program;

            textureProgram.useProgram();

            // vertex data
            GLES20.glEnableVertexAttribArray(textureProgram.getPositionHandle());
            GLES20.glVertexAttribPointer(
                    textureProgram.getPositionHandle(), VERTEX_DIMENS,
                    GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            // texture coords
            GLES20.glEnableVertexAttribArray(textureProgram.getTexCoordHandle());
            GLES20.glVertexAttribPointer(
                    textureProgram.getTexCoordHandle(), UV_DIMENS,
                    GLES20.GL_FLOAT, false, 0, mUVBuffer);

            // texture
            GLES20.glUniform1i(
                    textureProgram.getTexHandle(), textureProgram.getTextureNumber(mTextureName));

            // set transform matrix
            GLES20.glUniformMatrix4fv(
                    textureProgram.getMVPMatrixHandle(),
                    1, false, mvpMatrix, 0);
            // draw triangles

            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    mDrawListBuffer.limit(), GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

            // clean up
            GLES20.glDisableVertexAttribArray(textureProgram.getPositionHandle());
            GLES20.glDisableVertexAttribArray(textureProgram.getTexCoordHandle());
        }
    }

    @Override
    public boolean isTextured() {
        return true;
    }

    public static class Builder {
        FloatBuffer mVertexBuffer, mUVBuffer;
        ShortBuffer mDrawListBuffer;
        private TexturePointF[] mVertexArray;
        private int[] mDrawListArray;
        private String mTextureName;

        public Builder setVertices(TexturePointF... vertices) {
            mVertexArray = vertices;
            return this;
        }

        public Builder setVertexBuffer(FloatBuffer mVertexBuffer) {
            this.mVertexBuffer = mVertexBuffer;
            return this;
        }

        public Builder setUVBuffer(FloatBuffer mUVBuffer) {
            this.mUVBuffer = mUVBuffer;
            return this;
        }

        public Builder setDrawListBuffer(ShortBuffer mDrawListBuffer) {
            this.mDrawListBuffer = mDrawListBuffer;
            return this;
        }

        public Builder setDrawIndices(int... indices) {
            mDrawListArray = indices;
            return this;
        }

        public Builder setTextureName(String textureName) {
            this.mTextureName = textureName;
            return this;
        }

        public TextureDrawListShape build() {
            if ((null == mVertexBuffer || null == mUVBuffer) && null != mVertexArray) {
                ByteBuffer bb = ByteBuffer.allocateDirect(
                        VERTEX_DIMENS * mVertexArray.length * BYTES_PER_FLOAT);
                bb.order(ByteOrder.nativeOrder());
                FloatBuffer vertexBuffer = bb.asFloatBuffer();
                vertexBuffer.position(0);

                ByteBuffer ub = ByteBuffer.allocateDirect(
                        UV_DIMENS * mVertexArray.length * BYTES_PER_FLOAT);
                ub.order(ByteOrder.nativeOrder());
                FloatBuffer uvBuffer = ub.asFloatBuffer();
                uvBuffer.position(0);

                for (TexturePointF v : mVertexArray) {
                    vertexBuffer.put(v.mPointF.x);
                    vertexBuffer.put(v.mPointF.y);
                    vertexBuffer.put(0);
                    uvBuffer.put(v.mTextureU);
                    uvBuffer.put(v.mTextureV);
                }
                vertexBuffer.position(0);
                uvBuffer.position(0);

                mVertexBuffer = vertexBuffer;
                mUVBuffer = uvBuffer;
            }

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

            return new TextureDrawListShape(mVertexBuffer, mDrawListBuffer, mUVBuffer, mTextureName);
        }
    }
}
