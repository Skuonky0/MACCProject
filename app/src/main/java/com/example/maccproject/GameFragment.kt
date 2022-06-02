package com.example.maccproject

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.graphics.withMatrix
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class GameFragment : Fragment(), SensorEventListener, View.OnTouchListener{

    lateinit var sensorManager : SensorManager
    lateinit var gameView: View
    lateinit var canvas : Canvas

    var mRotMatrix = floatArrayOf(
        1f,0f,0f,
        0f,0.5f,-0.5f,
        0f,0.5f,0.5f)
    var orientation = FloatArray(3)
    var gameVector = FloatArray(3)

    val viewPortMatrix = Matrix()

    var screenHeight=0f
    var screenWidth=0f
    var screenAspectRatio=1f

    //isFiring
    var firing = false

    //Position of the ball over time
    var ballx=500f
    var bally=500f
    //Velocity of the ball over time
    var vx =0f
    var vy= 5f

    val realtime=1f //ratio between simulation time and real time

    var mass = 0.08f
    var gravity = mass*9.82f //2.82f //m^2/s

    var touchX=-10f //Hide touch
    var touchY=-10f //Hide touch

    //Objects
    lateinit var slabs : Array<Slabs>

    //paint
    val textPaint = Paint().apply {
        color = Color.parseColor("#AAFF0000")
        strokeWidth = 30f
        textSize=40f
    }
    val cannonPaint = Paint().apply {
        color = Color.parseColor("#AAFFFF00")
        strokeWidth = 4f
        strokeCap= Paint.Cap.ROUND
    }
    val testPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        style= Paint.Style.STROKE
    }

    val slabPaint = Paint().apply {
        color = Color.rgb(200,100,100)
        strokeWidth = 20f
        style= Paint.Style.STROKE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slabs = Array(25){Slabs(0f,0f,0f,0f)}

        gameView = GameView(this.context)
        gameView.keepScreenOn = true

        val point = Point()
        this.activity?.windowManager?.defaultDisplay?.getSize(point)
        screenWidth=point.x.toFloat()
        screenHeight=point.y.toFloat()
        screenAspectRatio=screenWidth/screenHeight

        val bitmap = Bitmap.createBitmap(screenWidth.toInt(), screenHeight.toInt(), Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)

        return gameView
    }

    override fun onStart() {
        super.onStart()
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameView.setOnTouchListener(this)
        viewPortMatrix.apply {
            setScale(1f,-1f)
            postTranslate(0f,screenHeight)
        }

        val displayMetrics = DisplayMetrics()
        this.activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        this.activity?.findViewById<Button>(R.id.calibrate)?.setOnClickListener {
            initialTilt = orientation[1]
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type== Sensor.TYPE_GAME_ROTATION_VECTOR){
            gameVector = event.values.clone()
        }

        orientation = floatArrayOf(
            mRotMatrix[0]*gameVector[0]+mRotMatrix[1]*gameVector[1]+mRotMatrix[2]*gameVector[2],
            mRotMatrix[3]*gameVector[0]+mRotMatrix[4]*gameVector[1]+mRotMatrix[5]*gameVector[2],
            mRotMatrix[6]*gameVector[0]+mRotMatrix[7]*gameVector[1]+mRotMatrix[8]*gameVector[2])
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                if (!firing) { //Cannon is not firing..
                    touchX = event.x
                    touchY = event.y //coordinate of the touch event
                    gameView.invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                ballx = touchX
                bally = touchY
                gameView.invalidate()
            }
        }
        return true
    }

    inner class GameView(context: Context?) : View(context) {

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            var tilt = 0f
            if(initialTilt == 0f) initialTilt = orientation[1]
            if(orientation[1]<initialTilt-0.1f) tilt = initialTilt-0.1f
            else if(orientation[1]>initialTilt+0.1f) tilt = initialTilt+0.1f
            else tilt = orientation[1]
            var displ = asin(tilt-initialTilt)
            if(displ>1) displ = 1f
            else if(displ<-1) displ = -1f
            vx = displ*100f

            with(canvas){
                //drawText(""+orientation[0], 10f, 50f,textPaint)
                drawText(""+orientation[1], 10f, 100f,textPaint)
                //drawText(""+orientation[2], 10f, 150f,textPaint)
                drawText(""+(displ), 10f, 200f,textPaint)
                drawText(""+initialTilt, 10f, 250f,textPaint)
                drawText(""+ballx, 10f, 350f,textPaint)
                drawText(""+bally, 10f, 400f,textPaint)
                drawText(""+vx, 10f, 450f,textPaint)
                drawText(""+vy, 10f, 500f,textPaint)


                withMatrix(viewPortMatrix) {
                    drawCircle(ballx,bally,20f,cannonPaint)
                    drawLine(ballx,bally,ballx+3*vx,bally,testPaint)
                    drawLine(ballx,bally,ballx,bally+3*vy,testPaint)
                }

                withMatrix(viewPortMatrix) {
                    drawLine(200f,250f,400f,250f,slabPaint)
                }

                gravity=-0.2f
                vy+=gravity
                if(vy > 20f) vy = 20f
                if(vy < -20f) vy = -20f
                ballx+=vx
                bally+=vy

                if(ballx>screenWidth) ballx = screenWidth
                else if(ballx<0) ballx = 0f
                //if(bally>screenHeight) bally = screenHeight
                if(bally<0){
                    vy = 20f
                    bally = 1f
                }
                //collision detection
                collisionDetection()
                invalidate()
            }
        }

        fun collisionDetection(){

            if(bally<260f && bally>240f && ballx>200f && ballx<400f && vy<0){
                vy = 20f
                bally+=2f
            }
        }
    }
}