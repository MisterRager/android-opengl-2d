package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;
import android.graphics.RectF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lighting.woe.shapeproject.Constants;

public class SolidShapeBuffer {

    final Collection<PointF> mPoints = new ArrayList<>();
    final List<SolidDrawListShape> mShapes = new ArrayList<>();
    final Map<SolidDrawListShape.Builder, List<Short>> mShapeDrawLists = new LinkedHashMap<>();

    final AtomicBoolean mDirty = new AtomicBoolean(false);

    public SolidShapeBuffer addRectangle(RectF rect, GLColor color) {
        PointF[] vertices = new PointF[]{
                new PointF(rect.left, rect.bottom),
                new PointF(rect.left, rect.top),
                new PointF(rect.right, rect.bottom),
                new PointF(rect.right, rect.top)};

        SolidDrawListShape.Builder builder = new SolidDrawListShape.Builder().putVertices(vertices)
                .setColor(color);

        synchronized (mDirty) {
            mPoints.addAll(Arrays.asList(vertices));
            mShapeDrawLists.put(builder, Arrays.asList(new Short[]{1, 0, 3, 3, 1, 2}));
            mDirty.set(true);
        }

        return this;
    }

    public SolidShapeBuffer addTriangle(PointF v1, PointF v2, PointF v3, GLColor glColor) {
        PointF[] vertices = new PointF[]{v1, v2, v3};
        SolidDrawListShape.Builder builder = new SolidDrawListShape.Builder().putVertices(vertices)
                .setColor(glColor);

        synchronized (mDirty) {
            mPoints.addAll(Arrays.asList(vertices));
            mShapeDrawLists.put(builder, Arrays.asList(new Short[]{0, 1, 2}));
            mDirty.set(true);
        }

        return this;
    }

    public List<SolidDrawListShape> getShapes() {
        synchronized (mDirty) {
            if (mDirty.getAndSet(false)) {
                build();
            }
            return Collections.unmodifiableList(mShapes);
        }

    }

    void build() {
        float vertices[] = new float[mPoints.size() * Constants.VERTEX_DIMENS];
        int i = 0;
        for (PointF v : mPoints) {
            vertices[i++] = v.x;
            vertices[i++] = v.y;
            vertices[i++] = 0;
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * Constants.BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);

        vertexBuffer.position(0);

        mShapes.clear();
        int offset = 0;

        for (Map.Entry<SolidDrawListShape.Builder, List<Short>> entry
                : mShapeDrawLists.entrySet()) {

            SolidDrawListShape.Builder b = entry.getKey();
            List<Short> dla = entry.getValue();

            b.setVertexBuffer(vertexBuffer);

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
