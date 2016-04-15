package lighting.woe.shapeproject.program;

import android.content.Context;

import java.io.IOException;

public class GradientProgram extends AbstractProgram{
    private final int mPositionHandle;
    private final int mMVPMatrixHandle;
    private final int mColorHandle;

    public GradientProgram(int programHandle) {
        super(programHandle);
        mPositionHandle = getAttributeHandle("vPosition");
        mMVPMatrixHandle = getUniformHanle("uMVPMatrix");
        mColorHandle = getAttributeHandle("aColor");
    }

    public int getPositionHandle() {
        return mPositionHandle;
    }

    public int getMVPMatrixHandle() {
        return mMVPMatrixHandle;
    }

    public int getColorHandle() {
        return mColorHandle;
    }

    public static GradientProgram buildShader(Context ctx, int vertexShaderId, int fragmentShaderId)
            throws IOException {
        return new GradientProgram(loadProgram(ctx, vertexShaderId, fragmentShaderId));
    }
}
