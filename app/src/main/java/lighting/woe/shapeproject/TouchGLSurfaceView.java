package lighting.woe.shapeproject;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class TouchGLSurfaceView extends GLSurfaceView {
    private static final String TAG = TouchGLSurfaceView.class.getSimpleName();
    private float mLastDownX, mLastDownY, mLastX, mLastY;
    private boolean mIsDragging;
    private ShapeRenderer mRenderer;

    public TouchGLSurfaceView(Context ctx) {
        super(ctx);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX(),
                y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownX = x;
                mLastDownY = y;
                mIsDragging = true;
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, String.format("Motion event: [%.4f, %.4f], [%.4f, %.4f]",
                        x, y, mLastDownX, mLastDownY));
                mIsDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    final float dx = x - mLastX, dy = y - mLastY;
                    Log.v(TAG, String.format("Motion distance: [%.9f, %.9f]", dx, dy));

                    post(new Runnable() {
                        @Override
                        public void run() {
                            mRenderer.moveCamera(dx, -dy);
                            requestRender();
                        }
                    });
                }
                break;
        }

        mLastX = x;
        mLastY = y;
        return true;
    }

    @Override
    public void setRenderer(Renderer renderer) {
        if (!(renderer instanceof ShapeRenderer)) {
            throw new IllegalArgumentException("renderer must be a ShapeRenderer");
        }
        mRenderer = (ShapeRenderer) renderer;
        super.setRenderer(renderer);
    }
}
