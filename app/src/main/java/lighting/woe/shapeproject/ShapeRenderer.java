package lighting.woe.shapeproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.util.SparseArray;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import lighting.woe.shapeproject.program.SolidProgram;

public class ShapeRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = ShapeRenderer.class.getSimpleName();
    private final Context mContext;
    private final float mRenderHeight;
    private final float mRenderWidth;

    private final Collection<GLShape> mShapes = new CopyOnWriteArrayList<>();
    private SolidProgram mSolidProgram;


    private long mLastTime;

    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    //private int mTexturedProgram;
    private final SparseArray<Integer> mTextureLoc = new SparseArray<>();

    public ShapeRenderer(Context ctx, float height, float width) {
        mContext = ctx;

        mRenderHeight = height;
        mRenderWidth = width;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        try {
            mSolidProgram = new SolidProgram(loadProgram(R.raw.solid_vertex, R.raw.solid_fragment));
            //mTexturedProgram = loadProgram(R.raw.texture_vertex, R.raw.texture_fragment);
            mTextureLoc.put(0, loadTexture(R.drawable.insanitywelf, 0));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // We need to know the current width and height.

        float widthRatio = mRenderWidth / width,
                heightRatio = mRenderHeight / height;

        float x1, y1, x2, y2;
        if (widthRatio > heightRatio) {
            //scale = widthRatio;
            x1 = 0;
            x2 = mRenderWidth;

            float extraHeight = (height * widthRatio) - mRenderHeight;
            Log.d(TAG, String.format("expected height: %.3f", mRenderHeight));
            y1 = -(extraHeight / 2);
            y2 = (height * widthRatio) + y1;
        } else {
            //scale = heightRatio;
            y1 = 0;
            y2 = mRenderHeight;

            float extraWidth = (width * heightRatio) - mRenderWidth;
            Log.d(TAG, String.format("expected width: %.3f", mRenderWidth));
            x1 = -(extraWidth / 2);
            x2 = (width * heightRatio) + x1;
        }

        Log.d(TAG, String.format("x1 %.3f, x2 %.3f, y1 %.3f, y2 %.4f", x1, x2, y1, y2));

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, width, height);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.orthoM(
                mProjectionMatrix, 0,
                x1, x2,
                y1, y2,
                Float.MIN_VALUE, 10f);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(
                mViewMatrix, 0,
                0f, 0f, 10f,
                0f, 0f, 0f,
                0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        long now = System.currentTimeMillis();
        if (now < mLastTime) {
            // Not sane
            return;
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        for (GLShape shape : mShapes) {
            if (shape.isTextured()) {
                //shape.draw(mMVPMatrix, mTexturedProgram);
            } else {
                shape.draw(mMVPMatrix, mSolidProgram);
            }
        }

        mLastTime = now;
    }

    private int loadProgram(int vertexShader, int fragmentShader) throws IOException {
        InputStream is = mContext.getResources().openRawResource(vertexShader);
        String vertexSource = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
        is = mContext.getResources().openRawResource(fragmentShader);
        String fragmentSource = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);

        return loadProgram(vertexSource, fragmentSource);
    }

    private int loadProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        return program;
    }

    private int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // return the shader
        return shader;
    }

    private int loadTexture(int resourceId, int textureSlot) {
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), resourceId);
            return loadTexture(bmp, textureSlot);
        } finally {
            if (null != bmp && !bmp.isRecycled()) {
                bmp.recycle();
            }
        }
    }

    private int loadTexture(Bitmap bmp, int textureSlot) {
        final int[] textureLoc = new int[1];
        GLES20.glGenTextures(1, textureLoc, textureSlot);

        if (0 == textureLoc[0]) {
            return 0;
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureLoc[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        return textureLoc[0];
    }

    public void onPause() {

    }

    public void onResume() {
        mLastTime = System.currentTimeMillis();
    }

    public ShapeRenderer addShape(GLShape shape) {
        mShapes.add(shape);
        return this;
    }

    public int getTextureLocation(int slotNumber) {
        return mTextureLoc.get(slotNumber);
    }
}
