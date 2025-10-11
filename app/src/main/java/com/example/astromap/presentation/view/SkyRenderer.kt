package com.example.astromap.presentation.view

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.astromap.domain.model.Star
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class SkyRenderer(private val stars: List<Star>) : GLSurfaceView.Renderer {

    private var program = 0
    private var positionHandle = 0
    private var mvpMatrixHandle = 0
    private lateinit var vertexBuffer: FloatBuffer

    // projection matrix
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    var angleX = 0f
    var angleY = 0f


    // TODO: should these coordinates be calculated by the SkyRenderer?
    private val starCoords = FloatArray(stars.size * 3).apply {
        var i = 0
        for ((_, ra, dec, _) in stars) {
            val xyz = raDecToXYZ(ra, dec)
            this[i++] = xyz[0]
            this[i++] = xyz[1]
            this[i++] = xyz[2]
        }
    }

    private val lineCoords = floatArrayOf(
        starCoords[0], starCoords[1], starCoords[2],  // first star
        starCoords[3], starCoords[4], starCoords[5],  // second star
        starCoords[2], starCoords[3], starCoords[7],
        starCoords[1], starCoords[9], starCoords[5],
        starCoords[3], starCoords[0], starCoords[6],
        starCoords[0], starCoords[3], starCoords[4],
    )

    private lateinit var lineBuffer: FloatBuffer

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Background color
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        // prepare lines buffer
        val lb = ByteBuffer.allocateDirect(lineCoords.size * 4)
        lb.order(ByteOrder.nativeOrder())
        lineBuffer = lb.asFloatBuffer()
        lineBuffer.put(lineCoords)
        lineBuffer.position(0)

        // Prepare vertex buffer
        val bb = ByteBuffer.allocateDirect(starCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(starCoords)
        vertexBuffer.position(0)

        // --- Simple vertex and fragment shaders ---
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                gl_PointSize = 8.0;
            }
        """


        val fragmentShaderCode = """
            precision mediump float;
            void main() {
                gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
            }
        """

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Create and link program
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 10f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        // --- Camera setup ---
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 1f,   // camera position // change this to "0f, 0f, 2f" if you want to see the sphere from "outside"
            0f, 0f, -1f,   // look at
            0f, 1f, 0f    // up direction
        )

        // --- Apply user-controlled rotation (touch input) ---
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)

        // X rotation (up/down)
        Matrix.rotateM(modelMatrix, 0, -angleX, 1f, 0f, 0f)
        // Y rotation (left/right)
        Matrix.rotateM(modelMatrix, 0, -angleY, 0f, 1f, 0f)

//        // --- Rotation around Y-axis ---
//        // uncomment to rotate the sphere without touching
//        Matrix.setRotateM(modelMatrix, 0, -angle, 0f, 1f, 0f)
//        angle = (angle + 0.5f) % 360  // increase slowly every frame

        // --- Combine matrices ---
        val temp = FloatArray(16)
        Matrix.multiplyMM(temp, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, temp, 0)

        // --- Draw stars ---
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, starCoords.size / 3)
        GLES20.glDisableVertexAttribArray(positionHandle)

        // --- Draw line ---
        val star1 = floatArrayOf(starCoords[0], starCoords[1], starCoords[2])
        val star2 = floatArrayOf(starCoords[3], starCoords[4], starCoords[5])
        val arcPoints = generateGreatCirclePoints(star1, star2, 20)

        val bb = ByteBuffer.allocateDirect(arcPoints.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val arcBuffer = bb.asFloatBuffer()
        arcBuffer.put(arcPoints)
        arcBuffer.position(0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, arcBuffer)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, arcPoints.size/3)
        GLES20.glDisableVertexAttribArray(positionHandle)

    }


    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compile failed: $error")
        }
        return shader
    }

    private fun raDecToXYZ(ra: Double, dec: Double): FloatArray {
        val radRA = Math.toRadians(ra)
        val radDec = Math.toRadians(dec)
        val x = cos(radDec) * cos(radRA)
        val y = sin(radDec)
        val z = cos(radDec) * sin(radRA)
        return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun generateGreatCirclePoints(v1: FloatArray, v2: FloatArray, segments: Int = 20): FloatArray {
        val p = FloatArray((segments + 1) * 3)
        val dot = (v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2]).coerceIn(-1f, 1f)
        val theta = acos(dot)
        if (theta < 1e-5) {
            // Points are too close; just return v1
            for (i in 0..segments) {
                p[i*3] = v1[0]
                p[i*3 + 1] = v1[1]
                p[i*3 + 2] = v1[2]
            }
            return p
        }
        for (i in 0..segments) {
            val t = i.toFloat() / segments
            val sinTheta = sin(theta)
            val s1 = sin((1-t)*theta) / sinTheta
            val s2 = sin(t*theta) / sinTheta
            p[i*3]     = s1*v1[0] + s2*v2[0]
            p[i*3 + 1] = s1*v1[1] + s2*v2[1]
            p[i*3 + 2] = s1*v1[2] + s2*v2[2]
        }
        return p
    }

}
