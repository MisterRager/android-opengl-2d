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

import static lighting.woe.shapeproject.Constants.BYTES_PER_FLOAT;
import static lighting.woe.shapeproject.Constants.COLOR_DIMENS;
import static lighting.woe.shapeproject.Constants.VERTEX_DIMENS;

public class GradientShapeBuffer {

    final Collection<ColorPointF> mPoints = new ArrayList<>();
    final Collection<GradientDrawListShape> mShapes = new ArrayList<>();
    final Map<GradientDrawListShape.Builder, List<Short>> mShapeDrawLists = new LinkedHashMap<>();

    FloatBuffer mVertexBuffer;

    final AtomicBoolean mDirty = new AtomicBoolean(false);

    public GradientShapeBuffer addRectangle(RectF rect, GLColor color) {
        ColorPointF[] vertices = new ColorPointF[]{
                new ColorPointF(new PointF(rect.left, rect.bottom), color),
                new ColorPointF(new PointF(rect.left, rect.top), color),
                new ColorPointF(new PointF(rect.right, rect.bottom), color),
                new ColorPointF(new PointF(rect.right, rect.top), color)};

        GradientDrawListShape.Builder builder = new GradientDrawListShape.Builder();

        synchronized (mDirty) {
            mPoints.addAll(Arrays.asList(vertices));
            mShapeDrawLists.put(builder, Arrays.asList(new Short[]{1, 0, 3, 3, 1, 2}));
            mDirty.set(true);
        }

        return this;
    }


    public GradientShapeBuffer addTriangle(ColorPointF v1, ColorPointF v2, ColorPointF v3, GLColor glColor) {
        ColorPointF[] vertices = new ColorPointF[]{v1, v2, v3};
        GradientDrawListShape.Builder builder = new GradientDrawListShape.Builder();

        synchronized (mDirty) {
            mPoints.addAll(Arrays.asList(vertices));
            mShapeDrawLists.put(builder, Arrays.asList(new Short[]{0, 1, 2}));
            mDirty.set(true);
        }

        return this;
    }

    public ArrayList<GradientDrawListShape> getShapes() {
        synchronized (mDirty) {
            if (mDirty.getAndSet(false)) {
                build();
            }
        }

        return new ArrayList<>(mShapes);
    }

    void build() {
        float vertices[] = new float[mPoints.size() * (VERTEX_DIMENS + COLOR_DIMENS)];
        int i = 0;
        for (ColorPointF v : mPoints) {
            vertices[i++] = v.point.x;
            vertices[i++] = v.point.y;
            vertices[i++] = 0;
            vertices[i++] = v.color.red;
            vertices[i++] = v.color.green;
            vertices[i++] = v.color.blue;
            vertices[i++] = v.color.alpha;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);

        vertexBuffer.position(0);
        mVertexBuffer = vertexBuffer;

        mShapes.clear();
        int offset = 0;

        for (Map.Entry<GradientDrawListShape.Builder, List<Short>> entry
                : mShapeDrawLists.entrySet()) {

            GradientDrawListShape.Builder b = entry.getKey();
            List<Short> dla = entry.getValue();

            b.setVertexBuffer(mVertexBuffer);

            int indexList[] = new int[dla.size()];
            int k = 0;
            for (Short s : dla) {
                indexList[k++] = s + offset;
            }
            b.setDrawList(indexList);
            mShapes.add(b.build());
            offset += new HashSet<>(dla).size();
        }
    }
}
