#extension GL_OES_EGL_image_external : require

//necessary
precision mediump float;
uniform samplerExternalOES camTexture;
uniform float aspectRatio;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

//THIS NEEDS TO MATCH STRING IN LipServiceRenderer
uniform sampler2D faceTexture;
uniform sampler2D paintTexture;

uniform float imgAspectRatio;
uniform float gamma;
uniform float hue;

/* yo gamma gamma! */
vec4 setGamma(vec4 color, float gamma)
{
    float gam = 1.0 / (gamma);
    return pow(color, vec4(gam, gam, gam, gam));
}

void main ()
{
    vec4 cameraColor = texture2D(camTexture, v_CamTexCoordinate);// * vec2(aspectRatio, 1.0));

    float halfAspectOffset = (1. - imgAspectRatio) / 2.;
    vec4 face;

    //if(imgAspectRatio < 1.)
        face = texture2D(faceTexture, (v_TexCoordinate + vec2(halfAspectOffset, 0.)) * vec2(imgAspectRatio, 1.));
    //else
      //  face = texture2D(faceTexture, (v_TexCoordinate + vec2(halfAspectOffset, 0.)));

    vec4 paint = texture2D(paintTexture, v_TexCoordinate);

    /* hue adjustment */
    float angle = hue * 3.14159265;
    float s = sin(angle), c = cos(angle);
    vec3 weights = (vec3(2.0 * c, -sqrt(3.0) * s - c, sqrt(3.0) * s - c) + 1.0) / 3.0;
    float len = length(cameraColor.rgb);
    cameraColor.rgb = vec3(
        dot(cameraColor.rgb, weights.xyz),
        dot(cameraColor.rgb, weights.zxy),
        dot(cameraColor.rgb, weights.yzx)
    );

    /* gamma adjustment */
    cameraColor = setGamma(cameraColor, gamma);

    if(paint.a > 0.0)
    {
        //if image is part of alpha fade of the faceTexture, mix the camera and the face
        //pixels together and then add a bit extra from the face to make sure its still solid
        gl_FragColor = mix(cameraColor, face, clamp(1. - paint.a, 0., 1.) );
    }
    else
    {
        gl_FragColor = face;
    }
}