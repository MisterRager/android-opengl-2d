package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

@RunWith(MockitoJUnitRunner.class)
public class GradientDrawListShapeTest {
    private static final float FLOAT_EPSILON = 0.005f;

    private ColorPointF[] mVertices;
    private GradientDrawListShape.Builder mBuilder;
    private int[] mDrawList;

    @Mock
    private ShortBuffer mDrawListBuffer;

    @Mock
    private FloatBuffer mVertexBuffer;

    @Before
    public void setup() {
        mBuilder = new GradientDrawListShape.Builder();

        mVertices = new ColorPointF[]{
                new ColorPointF(new PointF(0f, 0f), new GLColor(0.25f, 0.5f, 0.75f, 1f)),
                new ColorPointF(new PointF(1f, 0f), new GLColor(0.5f, 0.75f, 0.25f, 1f)),
                new ColorPointF(new PointF(0f, 1f), new GLColor(0.75f, 0.25f, 0.5f, 1f)),
        };

        mDrawList = new int[]{0, 1, 2,};
    }

    @Test
    public void testBuilderBuildVertices() {
        mBuilder.putVertices(mVertices);
        GradientDrawListShape shape = mBuilder.build();

        FloatBuffer buffer = mBuilder.mVertexBuffer;
        buffer.position(0);

        for (ColorPointF vertex : mVertices) {
            Assert.assertEquals(vertex.point.x, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.point.y, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(0f, buffer.get(), FLOAT_EPSILON);

            Assert.assertEquals(vertex.color.red, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.color.green, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.color.blue, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.color.alpha, buffer.get(), FLOAT_EPSILON);
        }

        buffer = shape.mVertexBuffer;
        buffer.position(0);

        for (ColorPointF vertex : mVertices) {
            Assert.assertEquals(vertex.point.x, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.point.y, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(0f, buffer.get(), FLOAT_EPSILON);

            Assert.assertEquals(vertex.color.red, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.color.green, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.color.blue, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.color.alpha, buffer.get(), FLOAT_EPSILON);
        }
    }

    @Test
    public void testBuildDrawList() {
        mBuilder.setDrawList(mDrawList);
        GradientDrawListShape shape = mBuilder.build();

        ShortBuffer buffer = mBuilder.mDrawListBuffer;
        buffer.position(0);

        for (int idx : mDrawList) {
            Assert.assertEquals((short) idx, buffer.get());
        }

        buffer = shape.mDrawListBuffer;
        buffer.position(0);

        for (int idx : mDrawList) {
            Assert.assertEquals((short) idx, buffer.get());
        }
    }

    @Test
    public void testBuilderBuild() {
        GradientDrawListShape shape = mBuilder
                .setDrawListBuffer(mDrawListBuffer)
                .setVertexBuffer(mVertexBuffer)
                .build();

        Assert.assertEquals(mDrawListBuffer, shape.mDrawListBuffer);
        Assert.assertEquals(mVertexBuffer, shape.mVertexBuffer);
    }

    @Test
    public void testIsNotTextured() {
        GradientDrawListShape shape = new GradientDrawListShape.Builder().build();
        Assert.assertFalse(shape.isTextured());
    }
}