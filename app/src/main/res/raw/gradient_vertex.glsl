uniform mat4 uMVPMatrix;

attribute vec4 vPosition;
attribute vec4 aColor;

varying vec4 vColor;

void main() {
  gl_Position = uMVPMatrix * vPosition;
  vColor = aColor;
}