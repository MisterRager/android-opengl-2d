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
public class SolidDrawListShapeTest {
    private static final float FLOAT_EPSILON = 0.005f;

    private static final int
            RED = 200,
            GREEN = 69,
            BLUE = 166,
            ALPHA = 255;

    private static final GLColor COLORS = new GLColor(
            RED / 255f,
            GREEN / 255f,
            BLUE / 255f,
            ALPHA / 255f
    );

    private PointF[] mVertices;
    private SolidDrawListShape.Builder mBuilder;
    private int[] mDrawList;

    @Mock
    private ShortBuffer mDrawListBuffer;

    @Mock
    private FloatBuffer mVertexBuffer;

    @Before
    public void setup() {
        mBuilder = new SolidDrawListShape.Builder();

        mVertices = new PointF[]{
                new PointF(0f, 0f),
                new PointF(1f, 0f),
                new PointF(0f, 1f),
                new PointF(1f, 1f),
        };

        mDrawList = new int[]{0, 1, 3,};
    }

    @Test
    public void testBuilderBuildVertices() {
        mBuilder.putVertices(mVertices);
        SolidDrawListShape shape = mBuilder.build();

        FloatBuffer buffer = mBuilder.mVertexBuffer;
        buffer.position(0);

        for (PointF vertex : mVertices) {
            Assert.assertEquals(vertex.x, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.y, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(0f, buffer.get(), FLOAT_EPSILON);
        }

        buffer = shape.mVertexBuffer;
        buffer.position(0);

        for (PointF vertex : mVertices) {
            Assert.assertEquals(vertex.x, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.y, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(0f, buffer.get(), FLOAT_EPSILON);
        }
    }

    @Test
    public void testBuildDrawList() {
        mBuilder.setDrawList(mDrawList);
        SolidDrawListShape shape = mBuilder.build();

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
    public void testBuilderSetColor() {
        mBuilder.setColor(COLORS);
        Assert.assertArrayEquals(COLORS.rgbaArray(), mBuilder.mColor, FLOAT_EPSILON);
    }

    @Test
    public void testBuilderBuild() {
        SolidDrawListShape shape = mBuilder.setDrawListBuffer(mDrawListBuffer)
                .setVertexBuffer(mVertexBuffer)
                .setColor(COLORS)
                .build();

        Assert.assertEquals(mDrawListBuffer, shape.mDrawListBuffer);
        Assert.assertEquals(mVertexBuffer, shape.mVertexBuffer);
        Assert.assertArrayEquals(COLORS.rgbaArray(), shape.mColor, FLOAT_EPSILON);
    }

    @Test
    public void testIsNotTextured() {
        SolidDrawListShape shape = new SolidDrawListShape.Builder().build();
        Assert.assertFalse(shape.isTextured());
    }
}