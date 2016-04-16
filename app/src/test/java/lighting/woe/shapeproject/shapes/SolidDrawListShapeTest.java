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
    private final int VERTEX_COUNT = 69;

    private static final int
            RED = 200,
            GREEN = 69,
            BLUE = 166,
            ALPHA = 255;

    private static final float COLORS[] = new float[]{
            RED / 255f,
            GREEN / 255f,
            BLUE / 255f,
            1f
    };

    private PointF[] mVertices;
    private SolidDrawListShape.Builder mBuilder;
    private short[] mDrawList;

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

        mDrawList = new short[]{0, 1, 3,};
    }

    @Test
    public void testBuilderPutVertices() {
        mBuilder.putVertices(mVertices);

        FloatBuffer buffer = mBuilder.getVertexBuffer();

        for (PointF vertex : mVertices) {
            Assert.assertEquals(vertex.x, buffer.get(), FLOAT_EPSILON);
            Assert.assertEquals(vertex.y, buffer.get(), FLOAT_EPSILON);
        }
    }

    @Test
    public void testBuilderPutDrawList() {
        mBuilder.setDrawList(mDrawList);

        Assert.assertEquals(mDrawList.length, mBuilder.getVertexCount());

        ShortBuffer buffer = mBuilder.getDrawListBuffer();

        for (short idx : mDrawList) {
            Assert.assertEquals(idx, buffer.get());
        }
    }

    @Test
    public void testBuilderSetColor() {
        mBuilder.setColor(RED, GREEN, BLUE, ALPHA);

        float[] color = mBuilder.getColor();

        for(int k = 0; k < 4; k++){
            Assert.assertEquals(COLORS[k], color[k], FLOAT_EPSILON);
        }
    }

    @Test
    public void testBuilderBuild(){
        SolidDrawListShape shape = mBuilder.setDrawListBuffer(mDrawListBuffer, VERTEX_COUNT)
                .setVertexBuffer(mVertexBuffer)
                .setColor(COLORS[0], COLORS[1], COLORS[2], COLORS[3])
                .build();

        Assert.assertEquals(mDrawListBuffer, shape.mDrawListBuffer);
        Assert.assertEquals(VERTEX_COUNT, shape.mVertexCount);
        Assert.assertEquals(mVertexBuffer, shape.mVertexBuffer);
        Assert.assertArrayEquals(COLORS, shape.mColor, FLOAT_EPSILON);
    }

}