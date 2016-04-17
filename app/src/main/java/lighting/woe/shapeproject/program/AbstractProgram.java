package lighting.woe.shapeproject.program;

import android.content.Context;
import android.opengl.GLES20;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

abstract public class AbstractProgram {
    private final int mHandle;

    public AbstractProgram(int programHandle) {
        mHandle = programHandle;
    }

    public void useProgram() {
        GLES20.glUseProgram(mHandle);
    }

    protected int getAttributeHandle(String attribute) {
        return GLES20.glGetAttribLocation(mHandle, attribute);
    }

    protected int getUniformHanle(String uniform) {
        return GLES20.glGetUniformLocation(mHandle, uniform);
    }

    protected static int loadProgram(Context ctx, int vertexShader, int fragmentShader) throws IOException {
        InputStream is = ctx.getResources().openRawResource(vertexShader);
        String vertexSource = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
        is = ctx.getResources().openRawResource(fragmentShader);
        String fragmentSource = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);

        return loadProgram(vertexSource, fragmentSource);
    }

    protected static int loadProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        return program;
    }

    protected static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // return the shader
        return shader;
    }

}
