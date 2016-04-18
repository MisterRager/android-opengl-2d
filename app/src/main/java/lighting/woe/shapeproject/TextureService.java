package lighting.woe.shapeproject;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextureService extends Service {
    private static final String TAG = TextureService.class.getSimpleName();

    private static final int BITMAP_CACHE_SIZE = 100;

    private ExecutorService mExecutor;
    private LruCache<String, Bitmap> mBitmapCache;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "going down");
        mBitmapCache.evictAll();
        if(null != mExecutor){
            mExecutor.shutdownNow();
        }
    }

    public class Binder extends android.os.Binder {
        public void uploadTexture(
                final String name, final int id, final GLSurfaceView surface,
                final GLTextureLoader loader) {

            Log.v(TAG, "Got command to upload texture " + name);
            getExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    loadTexture(name, id, surface, loader);
                }
            });
        }
    }

    public void loadTexture(final String name, int id, GLSurfaceView surface, final GLTextureLoader loader) {
        Bitmap cached = getBitmapCache().get(name);
        final Bitmap bmp;

        if (null == cached) {
            Log.d(TAG, "Loading bitmap with name " + name);
            bmp = BitmapFactory.decodeResource(getResources(), id);
            getBitmapCache().put(name, bmp);
        } else {
            Log.v(TAG, "Using cached bitmap for " + name);
            bmp = cached;
        }

        surface.queueEvent(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "uploading texture to loader: " + name);
                    loader.loadTexture(bmp, name);
                }catch (Throwable t){
                    Log.e(TAG, "problem:" + t.getMessage(), t);
                }
            }
        });
    }

    ExecutorService getExecutor() {
        if (null == mExecutor || mExecutor.isShutdown() || mExecutor.isTerminated()) {
            mExecutor = Executors.newCachedThreadPool();
        }
        return mExecutor;
    }

    LruCache<String, Bitmap> getBitmapCache() {
        if (null == mBitmapCache) {
            mBitmapCache = new LruCache<String, Bitmap>(BITMAP_CACHE_SIZE) {
                @Override
                protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                    oldValue.recycle();
                }
            };
        }
        return mBitmapCache;
    }

    public interface GLTextureLoader {
        int loadTexture(Bitmap bmp, String name);
    }
}
