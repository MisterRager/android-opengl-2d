package lighting.woe.shapeproject.program;

import android.opengl.GLES20;

abstract public class AbstractProgram {
    private final int mHandle;

    public AbstractProgram(int programHandle){
        mHandle = programHandle;
    }

    public void useProgram(){
        GLES20.glUseProgram(mHandle);
    }

    protected int getAttributeHandle(String attribute){
        return GLES20.glGetAttribLocation(mHandle, attribute);
    }

    protected int getUniformHanle(String uniform){
        return GLES20.glGetUniformLocation(mHandle, uniform);
    }
}
