package com.example.astromap.presentation.view

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class SkyRenderer(
    private val stars: List<Star>,
    private val constellations: List<Constellation>
) : GLSurfaceView.Renderer {

    // --- OpenGL handles ---
    private var program = 0
    private var positionHandle = 0
    private var mvpMatrixHandle = 0
    private lateinit var vertexBuffer: FloatBuffer

    // --- Matrices ---
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // --- Camera rotation ---
    var angleX = 0f
    var angleY = 0f

    // --- Precomputed star positions ---
    private val starCoords = stars.flatMap {
        val xyz = raDecToXYZ(it.ra, it.dec)
        listOf(xyz[0], xyz[1], xyz[2])
    }.toFloatArray()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        vertexBuffer = createFloatBuffer(starCoords)

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

        program = ShaderUtils.createProgram(vertexShaderCode, fragmentShaderCode)
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

        // View + rotation
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 1f,   // camera position
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

        val temp = FloatArray(16)
        Matrix.multiplyMM(temp, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, temp, 0)

        drawStars()
        drawConstellationLines()
    }

    private fun drawStars() {
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, stars.size)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun drawConstellationLines() {
        for (constellation in constellations) {
            for (line in constellation.lines) {
                val startXYZ = raDecToXYZ(line.first.ra, line.first.dec)
                val endXYZ = raDecToXYZ(line.second.ra, line.second.dec)
                val arcPoints = generateGreatCirclePoints(startXYZ, endXYZ)
                val arcBuffer = createFloatBuffer(arcPoints)

                GLES20.glEnableVertexAttribArray(positionHandle)
                GLES20.glVertexAttribPointer(
                    positionHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    arcBuffer
                )
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, arcPoints.size / 3)
                GLES20.glDisableVertexAttribArray(positionHandle)
            }
        }
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(data.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(data)
                position(0)
            }
        }
    }

    private fun raDecToXYZ(ra: Double, dec: Double): FloatArray {
        val radRA = Math.toRadians(ra)
        val radDec = Math.toRadians(dec)
        val x = cos(radDec) * cos(radRA)
        val y = sin(radDec)
        val z = cos(radDec) * sin(radRA)
        return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    private fun generateGreatCirclePoints(v1: FloatArray, v2: FloatArray, segments: Int = 30): FloatArray {
        val points = FloatArray((segments + 1) * 3)
        val dot = (v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2]).coerceIn(-1f, 1f)
        val theta = acos(dot)
        if (theta < 1e-5) {
            for (i in 0..segments) {
                points[i*3] = v1[0]
                points[i*3 + 1] = v1[1]
                points[i*3 + 2] = v1[2]
            }
            return points
        }
        val sinTheta = sin(theta)
        for (i in 0..segments) {
            val t = i.toFloat() / segments
            val s1 = sin((1 - t) * theta) / sinTheta
            val s2 = sin(t * theta) / sinTheta
            points[i*3] = s1*v1[0] + s2*v2[0]
            points[i*3 + 1] = s1*v1[1] + s2*v2[1]
            points[i*3 + 2] = s1*v1[2] + s2*v2[2]
        }
        return points
    }
}