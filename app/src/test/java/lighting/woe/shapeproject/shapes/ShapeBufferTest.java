package lighting.woe.shapeproject.shapes;

import android.graphics.RectF;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ShapeBufferTest {
    private static final float HEIGHT = 50;
    private static final float WIDTH = 100;
    private static final float FLOAT_EPSILON = 0.0001f;

    private GLColor mColor;
    private ShapeBuffer mBuffer;
    private RectF mRectangle;

    @Before
    public void setup() {
        mColor = new GLColor(0.1f, 0.75f, 0.25f, 1f);
        mBuffer = new ShapeBuffer();
        mRectangle = new RectF(0, HEIGHT, WIDTH, 0);
    }

    @Test
    public void testBuildRectangle() {
        Assert.assertNotEquals(0f, mRectangle.width(), FLOAT_EPSILON);
        Assert.assertNotEquals(0f, mRectangle.height(), FLOAT_EPSILON);
        mBuffer.addRectangle(mRectangle, mColor);
        ArrayList<SolidDrawListShape> shapes = mBuffer.getShapes();

        SolidDrawListShape shape = shapes.get(0);
        Assert.assertEquals(1, shapes.size());

        Assert.assertArrayEquals(mColor.rgbaArray(), shape.mColor, FLOAT_EPSILON);
        Assert.assertEquals(6, shape.mDrawListBuffer.limit());

        shape.mDrawListBuffer.position(0);
        short minIndex = shape.mDrawListBuffer.get(),
                maxIndex = minIndex;

        while(shape.mDrawListBuffer.limit() > shape.mDrawListBuffer.position()){
            short index = shape.mDrawListBuffer.get();
            minIndex = (short) Math.min(index, minIndex);
            maxIndex = (short) Math.max(index, maxIndex);
        }

        Assert.assertNotNull(maxIndex);
        Assert.assertNotNull(minIndex);

        shape.mVertexBuffer.position(minIndex);
        Float minX = shape.mVertexBuffer.get(),
                maxX = minX,
                minY = shape.mVertexBuffer.get(),
                maxY = minY;

        // get rid of z, for now
        shape.mVertexBuffer.get();

        for (int k = minIndex; k < maxIndex; k++) {
            float x = shape.mVertexBuffer.get(),
                    y = shape.mVertexBuffer.get(),
                    z = shape.mVertexBuffer.get();

            Assert.assertEquals(0, z, FLOAT_EPSILON);
            maxX = Math.max(x, maxX);
            minX = Math.min(x, minX);
            maxY = Math.max(y, maxY);
            minY = Math.min(y, minY);
        }

        Assert.assertNotNull(minX);
        Assert.assertNotNull(minY);
        Assert.assertNotNull(maxX);
        Assert.assertNotNull(maxY);

        Assert.assertEquals(mRectangle.left, minX, FLOAT_EPSILON);
        Assert.assertEquals(mRectangle.right, maxX, FLOAT_EPSILON);
        Assert.assertEquals(mRectangle.bottom, minY, FLOAT_EPSILON);
        Assert.assertEquals(mRectangle.top, maxY, FLOAT_EPSILON);
    }
}