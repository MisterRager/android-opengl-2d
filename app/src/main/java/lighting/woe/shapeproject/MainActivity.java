package lighting.woe.shapeproject;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lighting.woe.shapeproject.shapes.GLColor;
import lighting.woe.shapeproject.shapes.GLShape;
import lighting.woe.shapeproject.shapes.SolidShapeBuffer;
import lighting.woe.shapeproject.shapes.TextureShapeBuffer;

import static android.graphics.Color.MAGENTA;


public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GLSurfaceView mGlSurfaceView;
    private ShapeRenderer mRenderer;
    private BroadcastReceiver mRendererReadyReceiver;
    private BroadcastReceiver mTextureReadyReceiver;
    private TextureService.Binder mTextureBinder;
    private ArrayList<GLShape> mShapes = new ArrayList<>();
    private final Map<String, Integer> mPendingTextures = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyDialog()
                .detectAll()
                .build()
        );

        mGlSurfaceView = new TouchGLSurfaceView(this);

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
                        new GLColor(Color.BLUE));
        mShapes.addAll(gradientShapeBuffer.getShapes());

        final TextureShapeBuffer textureShapeBuffer = new TextureShapeBuffer();

        mRendererReadyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "Done loading renderer");
                mGlSurfaceView.requestRender();
            }
        };

        mRenderer.setShapes(mShapes);

        mTextureReadyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String texName = intent.getStringExtra(ShapeRenderer.DATA_TEXTURE);
                Log.v(TAG, "done loading texture " + texName);

                switch (texName) {
                    case Constants.TEX_WELF:
                        textureShapeBuffer
                                .addTile(Constants.TEX_WELF,
                                        new RectF(
                                                virtualWidth / 8, virtualHeight * 3 / 8,
                                                virtualWidth * 3 / 8, virtualHeight / 8));
                        break;
                    case Constants.TEX_AWESUM:
                        textureShapeBuffer
                                .addTile(Constants.TEX_AWESUM,
                                        new RectF(
                                                virtualWidth * 5 / 8, virtualHeight * 3 / 8,
                                                virtualWidth * 7 / 8, virtualHeight / 8));
                        break;
                }

                mShapes = Lists.newArrayList(
                        Iterables.concat(
                                gradientShapeBuffer.getShapes(), textureShapeBuffer.getShapes()));

                mRenderer.setShapes(mShapes);
                mGlSurfaceView.requestRender();
            }
        };

        Intent textureIntent = new Intent(this, TextureService.class);
        startService(textureIntent);
        bindService(textureIntent, this, Service.BIND_AUTO_CREATE);
        uploadTexture(R.drawable.awesome, Constants.TEX_AWESUM);
        uploadTexture(R.drawable.insanitywelf, Constants.TEX_WELF);
    }

    private void uploadTexture(int id, String name) {
        synchronized (mPendingTextures) {
            if (null == mTextureBinder) {
                mPendingTextures.put(name, id);
            } else {
                mTextureBinder.uploadTexture(name, id, mGlSurfaceView, mRenderer);
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (TextureService.class.getName().equals(name.getClassName())) {
            synchronized (mPendingTextures) {
                Log.v(TAG, "Bound TextureService");
                mTextureBinder = (TextureService.Binder) service;

                if (!mPendingTextures.isEmpty()) {
                    Log.v(TAG, "Uploading textures");
                    for (Map.Entry<String, Integer> pendingTexture : mPendingTextures.entrySet()) {
                        mTextureBinder.uploadTexture(
                                pendingTexture.getKey(), pendingTexture.getValue(),
                                mGlSurfaceView, mRenderer);
                    }
                    mPendingTextures.clear();
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (TextureService.class.getName().equals(name.getClassName())) {
            mTextureBinder = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
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

