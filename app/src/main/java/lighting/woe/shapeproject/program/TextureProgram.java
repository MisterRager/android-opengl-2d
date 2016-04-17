package lighting.woe.shapeproject.program;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextureProgram extends AbstractProgram {
    private final int mPositionHandle;
    private final int mMVPMatrixHandle;
    private final int mTexCoordHandle;
    private final int mTexHandle;

    private final Map<String, Integer> mTextureHandles = new LinkedHashMap<>();

    public TextureProgram(int programHandle) {
        super(programHandle);
        mPositionHandle = getAttributeHandle("vPosition");
        mMVPMatrixHandle = getUniformHanle("uMVPMatrix");
        mTexCoordHandle = getAttributeHandle("a_texCoord");
        mTexHandle = getUniformHanle("s_texture");
    }

    public int uploadTexture(String name, Bitmap bmp) {
        synchronized (mTextureHandles) {
            if (!mTextureHandles.containsKey(name)) {
                int[] textureHandle = new int[1];
                GLES20.glGenTextures(1, textureHandle, 0);

                // Bind texture to texturename
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + mTextureHandles.size());
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

                // Set filtering
                GLES20.glTexParameteri(
                        GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(
                        GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

                // Load the bitmap into the bound texture.
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

                mTextureHandles.put(name, textureHandle[0]);
            }
            return mTextureHandles.get(name);
        }
    }

    public int getPositionHandle() {
        return mPositionHandle;
    }

    public int getMVPMatrixHandle() {
        return mMVPMatrixHandle;
    }

    public int getTexCoordHandle() {
        return mTexCoordHandle;
    }

    public int getTexHandle() {
        return mTexHandle;
    }

    public int getTextureHandle(@NonNull String name) {
        synchronized (mTextureHandles) {
            if (mTextureHandles.containsKey(name)) {
                return mTextureHandles.get(name);
            }
            return 0;
        }
    }

    public int getTextureNumber(@NonNull String name) {
        synchronized (mTextureHandles) {
            int k = 0;
            for (String texName : mTextureHandles.keySet()) {
                if (name.equals(texName)) {
                    return k;
                }
                k++;
            }
            return 0;
        }
    }

    public static TextureProgram buildShader(Context ctx, int vertexShaderId, int fragmentShaderId)
            throws IOException {
        return new TextureProgram(loadProgram(ctx, vertexShaderId, fragmentShaderId));
    }
}
