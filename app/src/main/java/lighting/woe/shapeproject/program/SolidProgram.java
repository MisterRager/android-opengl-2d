package lighting.woe.shapeproject.program;

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
}
