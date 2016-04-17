package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;
import android.graphics.RectF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lighting.woe.shapeproject.Constants;

import static lighting.woe.shapeproject.Constants.BYTES_PER_FLOAT;

public class TextureShapeBuffer {
    final Map<TextureDrawListShape.Builder, List<Short>> mShapeDrawLists = new LinkedHashMap<>();
    final Collection<TexturePointF> mPoints = new ArrayList<>();
    private final AtomicBoolean mDirty = new AtomicBoolean(false);

    private List<TextureDrawListShape> mShapes = new ArrayList<>();

    public TextureShapeBuffer addTriangle(
            String textureName,
            TexturePointF v1, TexturePointF v2, TexturePointF v3) {

        TextureDrawListShape.Builder builder = new TextureDrawListShape.Builder()
                .setTextureName(textureName);

        synchronized (mDirty) {
            mPoints.add(v1);
            mPoints.add(v2);
            mPoints.add(v3);

            mShapeDrawLists.put(builder, Arrays.asList(new Short[]{0, 1, 2}));
            mDirty.set(true);
        }

        return this;
    }

    public TextureShapeBuffer addTile(
            String textureName,
            RectF rectangle){

        synchronized (mDirty){
            mPoints.addAll(Arrays.asList(
                    new TexturePointF(new PointF(rectangle.left, rectangle.bottom), 0, 1),
                    new TexturePointF(new PointF(rectangle.left, rectangle.top), 0, 0),
                    new TexturePointF(new PointF(rectangle.right, rectangle.bottom), 1, 1),
                    new TexturePointF(new PointF(rectangle.right, rectangle.top), 1, 0)
            ));

            mShapeDrawLists.put(
                    new TextureDrawListShape.Builder().setTextureName(textureName),
                    Arrays.asList(new Short[]{1, 0, 3, 3, 0, 2}));

            mDirty.set(true);
        }

        return this;
    }

    public List<TextureDrawListShape> getShapes() {
        synchronized (mDirty) {
            if (mDirty.getAndSet(false)) {
                build();
            }
        }
        return mShapes;
    }

    void build() {
        ByteBuffer bb = ByteBuffer.allocateDirect(
                mPoints.size() * Constants.VERTEX_DIMENS * BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.position(0);

        ByteBuffer ub = ByteBuffer.allocateDirect(
                mPoints.size() * Constants.UV_DIMENS * BYTES_PER_FLOAT);
        ub.order(ByteOrder.nativeOrder());
        FloatBuffer uvBuffer = ub.asFloatBuffer();
        uvBuffer.position(0);

        for (TexturePointF v : mPoints) {
            vertexBuffer.put(v.mPointF.x);
            vertexBuffer.put(v.mPointF.y);
            vertexBuffer.put(0);
            uvBuffer.put(v.mTextureU);
            uvBuffer.put(v.mTextureV);
        }

        vertexBuffer.position(0);

        uvBuffer.position(0);

        ArrayList<TextureDrawListShape> shapes = new ArrayList<>(mShapeDrawLists.size());
        int offset = 0;

        for (Map.Entry<TextureDrawListShape.Builder, List<Short>> entry
                : mShapeDrawLists.entrySet()) {

            TextureDrawListShape.Builder b = entry.getKey();
            List<Short> dla = entry.getValue();

            b.setVertexBuffer(vertexBuffer);
            b.setUVBuffer(uvBuffer);

            int indexList[] = new int[dla.size()];
            int k = 0;
            for (Short s : dla) {
                indexList[k++] = s + offset;
            }
            b.setDrawIndices(indexList);
            shapes.add(b.build());
            offset += new HashSet<>(dla).size();
        }

        mShapes = shapes;
    }
}
