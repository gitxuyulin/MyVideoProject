package com.example.xuyulin.myvideoproject.step11;

/**
 * Create by 徐玉林.
 * Create on 2019-12-16.
 * Describe: =>
 */
public class DrawTexture {

    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 vPosition;\n" +
            "attribute vec2 vCoordinate;\n" +
            "uniform mat4 vMatrix;\n" +
            "varying vec2 aCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "gl_Position = vMatrix*vPosition;\n" +
            "aCoordinate=vCoordinate;\n" +
            "}";

    private static final String CAMERA_INPUT_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "vuniform samplerExternalOES vTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   gl_FragColor = texture2D(vTexture, textureCoordinate);\n" +
            "}";

}
