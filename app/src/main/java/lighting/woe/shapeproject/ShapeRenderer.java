package lighting.woe.shapeproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import lighting.woe.shapeproject.program.SolidProgram;
import lighting.woe.shapeproject.program.TextureProgram;
import lighting.woe.shapeproject.shapes.GLShape;

public class ShapeRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = ShapeRenderer.class.getSimpleName();

    public static final String ACTION_RENDER_READY = "action.rendererIsReady";
    public static final String ACTION_TEXTURE_READY = "action.textureUploadComplete";
    public static final String DATA_TEXTURE = "data.textureName";

    private final Context mContext;
    private final float mRenderHeight;
    private final float mRenderWidth;

    private final Collection<GLShape> mShapes = new CopyOnWriteArrayList<>();
    private final Handler mUiHandler;
    private SolidProgram mSolidProgram;

    private long mLastTime;

    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private TextureProgram mTextureProgram;
    private final Map<String, Bitmap> mPendingTextures = new LinkedHashMap<>();

    public ShapeRenderer(Context ctx, float height, float width) {
        mContext = ctx;

        mRenderHeight = height;
        mRenderWidth = width;
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        try {
            mSolidProgram = SolidProgram.buildShader(
                    mContext, R.raw.solid_vertex, R.raw.solid_fragment);
            mTextureProgram = TextureProgram.buildShader(
                    mContext, R.raw.texture_vertex, R.raw.texture_fragment);

            synchronized (mPendingTextures) {
                for (Map.Entry<String, Bitmap> e : mPendingTextures.entrySet()) {
                    loadTexture(e.getValue(), e.getKey());
                }
                mPendingTextures.clear();
            }

            // Broadcast renderer done setting up
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    LocalBroadcastManager.getInstance(mContext)
                            .sendBroadcast(new Intent(ACTION_RENDER_READY));
                }
            });
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

        for (int k = 0; k < 16; k++) {
            mMVPMatrix[k] = mViewMatrix[k] = mProjectionMatrix[k] = 0;
        }

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
                shape.draw(mMVPMatrix, mTextureProgram);
            } else {
                shape.draw(mMVPMatrix, mSolidProgram);
            }
        }

        mLastTime = now;
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

    public ShapeRenderer addShapes(Collection<? extends GLShape> shapes) {
        mShapes.addAll(shapes);
        return this;
    }

    public int loadTexture(Bitmap bmp, final String textureName) {
        synchronized (mPendingTextures) {
            if (null != mTextureProgram) {
                int result = mTextureProgram.uploadTexture(textureName, bmp);
                if(result != 0){
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent broadcast = new Intent(ACTION_TEXTURE_READY);
                            broadcast.putExtra(DATA_TEXTURE, textureName);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcast);
                        }
                    });
                }
            } else {
                mPendingTextures.put(textureName, bmp);
            }
            return 0;
        }
    }
}
