package lighting.woe.shapeproject;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;

import lighting.woe.shapeproject.shapes.GLColor;
import lighting.woe.shapeproject.shapes.SolidShapeBuffer;
import lighting.woe.shapeproject.shapes.TextureShapeBuffer;

import static android.graphics.Color.MAGENTA;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GLSurfaceView mGlSurfaceView;
    private ShapeRenderer mRenderer;
    private BroadcastReceiver mRendererReadyReceiver;
    private BroadcastReceiver mTextureReadyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyDialog()
                .detectAll()
                .build()
        );

        mGlSurfaceView = new GLSurfaceView(this);

        final boolean supportsEs2 = isEs2Supported();

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGlSurfaceView.setEGLContextClientVersion(2);

        } else {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
            throw new RuntimeException("No support for ES2");
        }

        final float virtualWidth = getFloatResource(R.dimen.GL_VIRTUAL_WIDTH);
        final float virtualHeight = getFloatResource(R.dimen.GL_VIRTUAL_HEIGHT);
        mRenderer = new ShapeRenderer(this, virtualHeight, virtualWidth);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        setContentView(mGlSurfaceView);
        final SolidShapeBuffer gradientShapeBuffer = new SolidShapeBuffer();

        gradientShapeBuffer
                .addRectangle(
                        new RectF(0, virtualHeight, virtualWidth, 0),
                        new GLColor(MAGENTA))
                .addTriangle(
                        new PointF(0, 0),
                        new PointF(virtualWidth / 2, virtualHeight),
                        new PointF(virtualWidth, 0),
                        new GLColor(Color.GRAY));

        final TextureShapeBuffer textureShapeBuffer = new TextureShapeBuffer()
                .addTile(Constants.TEX_WELF,
                        new RectF(
                                virtualWidth / 8, virtualHeight * 3 / 8,
                                virtualWidth * 3 / 8, virtualHeight / 8))
                .addTile(Constants.TEX_AWESUM,
                        new RectF(
                                virtualWidth * 5 / 8, virtualHeight * 3 / 8,
                                virtualWidth * 7 / 8, virtualHeight / 8));

        final Bitmap wolf = BitmapFactory.decodeResource(getResources(), R.drawable.insanitywelf);
        final Bitmap awe = BitmapFactory.decodeResource(getResources(), R.drawable.awesome);

        mGlSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Doing shape adding task");
                mRenderer.loadTexture(wolf, Constants.TEX_WELF);
                mRenderer.loadTexture(awe, Constants.TEX_AWESUM);
                mRenderer.addShapes(gradientShapeBuffer.getShapes());
                mRenderer.addShapes(textureShapeBuffer.getShapes());
                Log.v(TAG, "done adding shapes");
            }
        });

        mRendererReadyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "Done loading renderer");
                mGlSurfaceView.requestRender();
            }
        };

        mTextureReadyReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String texName = intent.getStringExtra(ShapeRenderer.DATA_TEXTURE);
                Log.v(TAG, "done loading texture " + texName);
                mGlSurfaceView.requestRender();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mGlSurfaceView) {
            mGlSurfaceView.onResume();
        }
        if (null != mRenderer) {
            mRenderer.onResume();
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

        lbm.registerReceiver(
                mRendererReadyReceiver, new IntentFilter(ShapeRenderer.ACTION_RENDER_READY));
        lbm.registerReceiver(
                mTextureReadyReceiver, new IntentFilter(ShapeRenderer.ACTION_TEXTURE_READY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mGlSurfaceView) {
            mGlSurfaceView.onPause();
        }
        if (null != mRenderer) {
            mRenderer.onPause();
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.unregisterReceiver(mRendererReadyReceiver);
        lbm.unregisterReceiver(mTextureReadyReceiver);
    }

    private float getFloatResource(int id) {
        TypedValue tv = new TypedValue();
        getResources().getValue(id, tv, true);
        return tv.getFloat();
    }

    private boolean isEs2Supported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                // Or... say "yes" if it's the VM.
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))) {
            return true;
        }

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        return configurationInfo.reqGlEsVersion >= 0x20000;
    }
}

