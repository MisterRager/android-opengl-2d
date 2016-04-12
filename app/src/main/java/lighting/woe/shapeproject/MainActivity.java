package lighting.woe.shapeproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GLSurfaceView mGlSurfaceView;
    private ShapeRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        mGlSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Doing shape adding task");

                mRenderer.addShape(new Quad(
                        new PointF(0, virtualHeight),
                        new PointF(0, 0),
                        new PointF(virtualWidth, virtualHeight),
                        new PointF(virtualWidth, 0),
                        Color.RED));

                mRenderer.addShape(new Triangle(
                        new PointF(0, 0),
                        new PointF(virtualWidth / 2, virtualHeight),
                        new PointF(virtualWidth, 0),
                        Color.argb(255, 200, 150, 0)));
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
