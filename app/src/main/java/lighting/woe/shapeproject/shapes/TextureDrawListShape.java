package lighting.woe.shapeproject.shapes;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import lighting.woe.shapeproject.Constants;
import lighting.woe.shapeproject.program.AbstractProgram;
import lighting.woe.shapeproject.program.TextureProgram;

public class TextureDrawListShape implements GLShape {
    final FloatBuffer mVertexBuffer;
    final ShortBuffer mDrawListBuffer;
    final FloatBuffer mUVBuffer;
    final int mTextureNumber;

    public TextureDrawListShape(
            FloatBuffer mVertexBuffer, ShortBuffer mDrawListBuffer, FloatBuffer mUVBuffer,
            int textureNumber) {

        this.mVertexBuffer = mVertexBuffer;
        this.mDrawListBuffer = mDrawListBuffer;
        this.mUVBuffer = mUVBuffer;
        this.mTextureNumber = textureNumber;
    }

    @Override
    public void draw(float[] mvpMatrix, AbstractProgram program) {
        if (program instanceof TextureProgram) {
            TextureProgram textureProgram = (TextureProgram) program;

            textureProgram.useProgram();

            // vertex data
            GLES20.glEnableVertexAttribArray(textureProgram.getPositionHandle());
            GLES20.glVertexAttribPointer(
                    textureProgram.getPositionHandle(), Constants.VERTEX_DIMENS,
                    GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            // texture coords
            GLES20.glEnableVertexAttribArray(textureProgram.getTexCoordHandle());
            GLES20.glVertexAttribPointer(
                    textureProgram.getTexCoordHandle(), Constants.UV_DIMENS,
                    GLES20.GL_FLOAT, false, 0, mUVBuffer);

            // texture
            GLES20.glUniform1i(textureProgram.getTexHandle(), mTextureNumber);

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
}
