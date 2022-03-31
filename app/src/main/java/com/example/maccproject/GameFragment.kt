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
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.withMatrix
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

    //center of the wheel in the image
    val wheelx=160f
    val wheely=390f

    //diagonal of the cannon bitmap
    var diagonal = 0f

    val viewPortMatrix = Matrix()
    val bitmapMatrix = Matrix()

    var screenHeight=0f
    var screenWidth=0f
    var screenAspectRatio=1f

    val biasAngle = 39f //angle of the gun when the bitmap is horizontal

    //isFiring
    var firing = false

    //Position of the ball over time
    var ballx=0f
    var bally=0f
    //Velocity of the ball over time
    var vx =0f
    var vy= 0f

    //Initial velocity of the ball
    var px=0f
    var py=0f

    //Scaling parameter for the initial velocity
    val mpp = 0.05f //

    val realtime=1f //ratio between simulation time and real time

    var mass = 0.28f
    var gravity = mass*9.82f //2.82f //m^2/s
    var past=0L //previous time slot

    var touchX=-10f //Hide touch
    var touchY=-10f //Hide touch

    var initialTilt = 0f

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

    lateinit var cannon : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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

        cannon  =
            BitmapFactory.decodeStream(
                getContext()?.assets?.open("cannon2.png")
            )

        diagonal = (cannon.width* Math.sqrt(2.0)).toFloat()

        gameView.setOnTouchListener(this)
        viewPortMatrix.apply {
            setScale(1f,-1f)
            postTranslate(0f,screenHeight)
        }
        bitmapMatrix.apply {
            setScale(0.5f,0.5f)
            postTranslate(0f,screenHeight-cannon.height/2f)
        }

        val displayMetrics = DisplayMetrics()
        this.activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
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

                    //Set the new initial velocity components
                    vx=touchX
                    vy=screenHeight-touchY
                    vy*=mpp
                    vx*=mpp
                    px=vx
                    py=vy
                    gameView.invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                if (firing) return true
                firing = true
                //Calculate the initial position of the ball
                val angle=(atan2(py,px))
                ballx=diagonal* cos(angle) /2f
                bally=diagonal* sin(angle) /2f

                past=System.currentTimeMillis()
                gameView.invalidate()
            }
        }
        return true
    }

    inner class GameView(context: Context?) : View(context) {

        val cannon  =
            BitmapFactory.decodeStream(
                getContext().assets.open("cannon2.png")
            )

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            var tilt = 0f
            //if(initialTilt == 0f) initialTilt = orientation[1]
            if(orientation[1]<initialTilt-0.1f) tilt = initialTilt-0.1f
            else if(orientation[1]>initialTilt+0.1f) tilt = initialTilt+0.1f
            else tilt = orientation[1]

            with(canvas){
                drawText(""+orientation[0], 10f, 50f,textPaint)
                drawText(""+orientation[1], 10f, 100f,textPaint)//questo
                drawText(""+orientation[2], 10f, 150f,textPaint)
                drawText(""+tilt, 10f, 200f,textPaint)
                drawText(""+initialTilt, 10f, 250f,textPaint)



                withMatrix(viewPortMatrix) {
                    drawLine(0f,0f,vx,vy,cannonPaint)
                }

                withMatrix(bitmapMatrix) {
                    withMatrix(Matrix().apply {
                        setRotate(biasAngle-(180/ Math.PI* atan2(py,px)).toFloat(),wheelx,wheely)
                    })
                    {
                        drawBitmap(cannon,0f,0f,null)
                    }
                }

                if (firing){
                    val now= System.currentTimeMillis() //Current time
                    val dt = now-past //Time elapsed from the last onDraw()
                    ballx+=vx*dt/(realtime*1000)
                    bally+=vy*dt/(realtime*1000)
                    vy-=gravity*dt/(realtime*1000) //vertical component decreases over time..
                    withMatrix(viewPortMatrix) {
                        drawCircle(ballx,bally,20f,cannonPaint)
                        drawLine(ballx,bally,ballx+3*vx,bally,testPaint)
                        drawLine(ballx,bally,ballx,bally+3*vy,testPaint)
                    }
                    if (
                        (ballx>screenWidth) or
                        (bally>screenHeight) or
                        (ballx<0) or
                        (bally<0)
                    ) {
                        firing = false
                    }
                }
                invalidate()
            }
        }
    }
}