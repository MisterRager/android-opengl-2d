package lighting.woe.shapeproject;

public interface GLShape {
    void draw(float[] mvpMatrix, int program);
    boolean isTextured();
}
