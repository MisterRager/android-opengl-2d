package lighting.woe.shapeproject.program;

import android.content.Context;

import java.io.IOException;

public class SolidProgram extends AbstractProgram{
    private final int mPositionHandle;
    private final int mMVPMatrixHandle;
    private final int mColorHandle;

    public SolidProgram(int programHandle) {
        super(programHandle);
        mPositionHandle = getAttributeHandle("vPosition");
        mMVPMatrixHandle = getUniformHanle("uMVPMatrix");
        mColorHandle = getUniformHanle("vColor");
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

    public static SolidProgram buildShader(Context ctx, int vertexShaderId, int fragmentShaderId)
            throws IOException {
        return new SolidProgram(loadProgram(ctx, vertexShaderId, fragmentShaderId));
    }
}
