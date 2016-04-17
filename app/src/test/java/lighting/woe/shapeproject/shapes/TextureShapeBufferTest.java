package lighting.woe.shapeproject.shapes;

import android.graphics.PointF;
import android.graphics.RectF;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

public class TextureShapeBufferTest {
    private static final float FLOAT_EPSILON = 0.0001f;
    private TextureShapeBuffer mBuffer;
    private String mTexture;
    private TexturePointF[] mTriangleVertex;
    private RectF mRectF;

    @Before
    public void setup() {
        mBuffer = new TextureShapeBuffer();
        mTexture = "WOOOO";
        mTriangleVertex = new TexturePointF[]{
                new TexturePointF(new PointF(0, 0), 0, 1),
                new TexturePointF(new PointF(10, 0), 1, 1),
                new TexturePointF(new PointF(10, 20), 1, 0)};
        mRectF = new RectF(0, 10, 20, 5);
    }

    @Test
    public void testBuildTriangle() {
        mBuffer.addTriangle(mTexture, mTriangleVertex[0], mTriangleVertex[1], mTriangleVertex[2]);
        List<TextureDrawListShape> shapes = mBuffer.getShapes();

        Assert.assertEquals(1, shapes.size());

        TextureDrawListShape shape = shapes.get(0);
        Assert.assertTrue(shape.isTextured());
        Assert.assertEquals(mTexture, shape.mTextureName);

        FloatBuffer vBuff = shape.mVertexBuffer;
        vBuff.position(0);
        FloatBuffer uvBuff = shape.mUVBuffer;
        uvBuff.position(0);
        ShortBuffer drawListBuffer = shape.mDrawListBuffer;
        drawListBuffer.position(0);

        for (int k = 0; k < 3; k++) {
            TexturePointF point = mTriangleVertex[k];

            Assert.assertEquals(point.mPointF.x, vBuff.get(), FLOAT_EPSILON);

            Assert.assertEquals(point.mPointF.y, vBuff.get(), FLOAT_EPSILON);
            Assert.assertEquals(0f, vBuff.get(), FLOAT_EPSILON);

            Assert.assertEquals(point.mTextureU, uvBuff.get(), FLOAT_EPSILON);
            Assert.assertEquals(point.mTextureV, uvBuff.get(), FLOAT_EPSILON);

            Assert.assertEquals(k, drawListBuffer.get());
        }
    }

    @Test
    public void testBuildTile() {
        mBuffer.addTile(mTexture, mRectF);
        List<TextureDrawListShape> shapes = mBuffer.getShapes();

        Assert.assertEquals(1, shapes.size());

        TextureDrawListShape shape = shapes.get(0);
        Assert.assertTrue(shape.isTextured());
        Assert.assertEquals(mTexture, shape.mTextureName);

        FloatBuffer vBuff = shape.mVertexBuffer;
        vBuff.position(0);
        FloatBuffer uvBuff = shape.mUVBuffer;
        uvBuff.position(0);
        shape.mDrawListBuffer.position(0);
        short minIndex = shape.mDrawListBuffer.get(),
                maxIndex = minIndex;

        while (shape.mDrawListBuffer.limit() > shape.mDrawListBuffer.position()) {
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

        Assert.assertEquals(mRectF.left, minX, FLOAT_EPSILON);
        Assert.assertEquals(mRectF.right, maxX, FLOAT_EPSILON);
        Assert.assertEquals(mRectF.bottom, minY, FLOAT_EPSILON);
        Assert.assertEquals(mRectF.top, maxY, FLOAT_EPSILON);
    }
}