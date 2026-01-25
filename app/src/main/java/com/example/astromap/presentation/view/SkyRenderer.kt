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

    var explorationModeEnabled = false

    var starClickEnabled = false

    // --- OpenGL handles ---
    private var program = 0

    private var starProgram = 0
    private var lineProgram = 0

    private var positionHandle = 0
    private var magnitudeHandle = 0
    private var mvpMatrixHandle = 0

    private var linePositionHandle = 0
    private var lineMvpMatrixHandle = 0

    private var colorHandle = 0
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var magnitudeBuffer: FloatBuffer

    private lateinit var colorBuffer: FloatBuffer

    // --- Matrices ---
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private var rotationMatrix = FloatArray(16)

    // --- Precomputed star positions ---
    private val starCoords = FloatArray(stars.size * 3)
    private val starMagnitudes = FloatArray(stars.size)

    private val starColors = FloatArray(stars.size * 3)

    init {
        for (i in stars.indices) {
            val xyz = raDecToXYZ(stars[i].ra, stars[i].dec)
            starCoords[i * 3] = xyz[0]
            starCoords[i * 3 + 1] = xyz[1]
            starCoords[i * 3 + 2] = xyz[2]
            starMagnitudes[i] = stars[i].mag.toFloat()

            val bv = stars[i].bval ?: 0.65   // Słońce jako fallback
            val temp = bvToTemperature(bv)
            val rgb = temperatureToRGB(temp)
            starColors[i * 3] = rgb[0]
            starColors[i * 3 + 1] = rgb[1]
            starColors[i * 3 + 2] = rgb[2]
        }
        Matrix.setIdentityM(rotationMatrix, 0)
    }

    fun updateRotation(rotationMatrix: FloatArray) {
        this.rotationMatrix = getMatrixWithProperControls(rotationMatrix)
    }

    fun rotateWithTouch(dx: Float, dy: Float) {
        val invertedRotation = FloatArray(16)
        Matrix.setIdentityM(invertedRotation, 0)
        Matrix.rotateM(invertedRotation, 0, dx, 0f, 1f, 0f)
        Matrix.rotateM(invertedRotation, 0, -dy, 1f, 0f, 0f)

        val newRotation = FloatArray(16)
        Matrix.multiplyMM(newRotation, 0, invertedRotation, 0, rotationMatrix, 0)
        rotationMatrix = newRotation
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        vertexBuffer = createFloatBuffer(starCoords)
        magnitudeBuffer = createFloatBuffer(starMagnitudes)
        colorBuffer = createFloatBuffer(starColors)

        // ===== STARS =====
        val starVertexShader = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            attribute float aMagnitude;
            attribute vec3 aColor;
    
            varying vec3 vColor;
    
            void main() {
                gl_Position = uMVPMatrix * vPosition;
    
                float size = 12.0 * pow(2.0, -0.25 * aMagnitude);
                size = clamp(size, 6.0, 24.0);
                gl_PointSize = size;
    
                vColor = aColor;
            }
        """

        val starFragmentShader = """
            precision mediump float;
            varying vec3 vColor;
    
            void main() {
                float d = distance(gl_PointCoord, vec2(0.5));
                if (d > 0.5) discard;
                gl_FragColor = vec4(vColor, 1.0);
            }
        """

        starProgram = ShaderUtils.createProgram(starVertexShader, starFragmentShader)

        positionHandle = GLES20.glGetAttribLocation(starProgram, "vPosition")
        magnitudeHandle = GLES20.glGetAttribLocation(starProgram, "aMagnitude")
        colorHandle = GLES20.glGetAttribLocation(starProgram, "aColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(starProgram, "uMVPMatrix")

        // ===== CONSTELLATION LINES =====
        val lineVertexShader = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
    
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """

        val lineFragmentShader = """
            precision mediump float;
            void main() {
                gl_FragColor = vec4(1.0);
            }
        """

        lineProgram = ShaderUtils.createProgram(lineVertexShader, lineFragmentShader)

        linePositionHandle = GLES20.glGetAttribLocation(lineProgram, "vPosition")
        lineMvpMatrixHandle = GLES20.glGetUniformLocation(lineProgram, "uMVPMatrix")
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, ratio, -ratio, -1f, 1f, 1f, 10f)
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

        val finalMatrix = FloatArray(16)
        Matrix.multiplyMM(finalMatrix, 0, viewMatrix, 0, rotationMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, finalMatrix, 0)

        GLES20.glUseProgram(starProgram)
        drawStars()

        GLES20.glUseProgram(lineProgram)
        drawConstellationLines()
    }

    private fun drawStars() {
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(magnitudeHandle)
        GLES20.glVertexAttribPointer(magnitudeHandle, 1, GLES20.GL_FLOAT, false, 0, magnitudeBuffer)

        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_FLOAT, false, 0, colorBuffer)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, stars.size)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(magnitudeHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)



    }

    private fun drawConstellationLines() {
        GLES20.glLineWidth(3.0f)
        for (constellation in constellations) {
            for (line in constellation.lines) {
                val startXYZ = raDecToXYZ(line.first.ra, line.first.dec)
                val endXYZ = raDecToXYZ(line.second.ra, line.second.dec)
                val arcPoints = generateGreatCirclePoints(startXYZ, endXYZ)
                val arcBuffer = createFloatBuffer(arcPoints)

                GLES20.glEnableVertexAttribArray(positionHandle)
                GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, arcBuffer)
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

    fun projectStarToScreen(ra: Double, dec: Double, screenWidth: Int, screenHeight: Int): Pair<Float, Float>? {
        val xyz = raDecToXYZ(ra, dec)
        val vec = floatArrayOf(xyz[0], xyz[1], xyz[2], 1f)

        val result = FloatArray(4)
        Matrix.multiplyMV(result, 0, mvpMatrix, 0, vec, 0)

        if (result[3] <= 0f) return null // gwiazda za kamerą

        val ndcX = result[0] / result[3]
        val ndcY = result[1] / result[3]

        val x = (ndcX * 0.5f + 0.5f) * screenWidth
        val y = (1f - (ndcY * 0.5f + 0.5f)) * screenHeight

        return Pair(x, y)
    }

    fun isInitialized(): Boolean {
        // sprawdza czy MVP matrix został obliczony
        return mvpMatrix.any { it != 0f }
    }
}

private fun columnToOpposite(matrix: FloatArray, columnIndex: Int) {
    for (i in columnIndex until matrix.size step 4)
        matrix[i] = -matrix[i]
}

/**
 * This function needs to be applied, because the sphere is viewed from the inside,
 * which causes the navigation to be inverse in the left-right axis by the default.
 * By applying this function the navigation is brought back to normal.
 */
private fun getMatrixWithProperControls(rotationMatrix: FloatArray): FloatArray {
    val flipMatrix = FloatArray(16)
    Matrix.setIdentityM(flipMatrix, 0)
    flipMatrix[0] = -1f
    val flipped = FloatArray(16)
    Matrix.multiplyMM(flipped, 0, rotationMatrix, 0, flipMatrix, 0)
    columnToOpposite(flipped, 0)
    return flipped
}

fun bvToTemperature(bv: Double): Double {
    return 4600.0 * (
            1.0 / (0.92 * bv + 1.7) +
                    1.0 / (0.92 * bv + 0.62)
            )
}

fun temperatureToRGB(temp: Double): FloatArray {
    val t = (temp / 100.0).coerceIn(10.0, 400.0)

    var r: Double
    var g: Double
    var b: Double

    // RED
    r = if (t <= 66) {
        255.0
    } else {
        329.698727446 * Math.pow(t - 60, -0.1332047592)
    }

    // GREEN
    g = if (t <= 66) {
        99.4708025861 * Math.log(t) - 161.1195681661
    } else {
        288.1221695283 * Math.pow(t - 60, -0.0755148492)
    }

    // BLUE
    b = if (t >= 66) {
        255.0
    } else if (t <= 19) {
        0.0
    } else {
        138.5177312231 * Math.log(t - 10) - 305.0447927307
    }

    return floatArrayOf(
        (r / 255.0).coerceIn(0.0, 1.0).toFloat(),
        (g / 255.0).coerceIn(0.0, 1.0).toFloat(),
        (b / 255.0).coerceIn(0.0, 1.0).toFloat()
    )
}

