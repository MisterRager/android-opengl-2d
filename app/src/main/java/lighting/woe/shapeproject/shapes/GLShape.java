package lighting.woe.shapeproject.shapes;

import lighting.woe.shapeproject.program.AbstractProgram;

public interface GLShape {
    void draw(float[] mvpMatrix, AbstractProgram program);
    boolean isTextured();
}
