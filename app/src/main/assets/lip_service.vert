//position
attribute vec4 position;
uniform mat4 FMatrix;

//camera transform and texture
uniform mat4 camTextureTransform;
attribute vec4 camTexCoordinate;

//tex coords
varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;
varying vec2 v_FaceCoordinate;
uniform float imgAspectRatio;

uniform mat4 uPMatrix;


void main() {
    //camera texcoord needs to be manipulated by the transform given back
    //from the system
    v_CamTexCoordinate = (camTextureTransform * camTexCoordinate * uPMatrix).xy;

    //normal coordinate is upside down so reverse it
    v_TexCoordinate = camTexCoordinate.xy * vec2(1.0, -1.0);

    v_FaceCoordinate = (camTexCoordinate).xy * vec2(1.0, -1.0);

    gl_Position =  FMatrix * position;
}