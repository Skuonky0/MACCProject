package com.example.maccproject

import android.content.Context
import android.graphics.*
import android.graphics.BitmapFactory.decodeStream
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import androidx.navigation.fragment.NavHostFragment
import kotlin.math.asin
import kotlin.random.Random


//class GameFragment : Fragment(), SensorEventListener, View.OnTouchListener{
class GameFragment : Fragment(), SensorEventListener{

    lateinit var sensorManager : SensorManager
    lateinit var gameView: View
    lateinit var canvas : Canvas
    lateinit var frgmt: GameFragment

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

    //Position of the ball over time
    var ballx=500f
    var bally=300f
    //Velocity of the ball over time
    var vx =0f
    var vy= 0f

    var mass = 0.08f
    var gravity = mass*9.82f //2.82f //m^2/s

    private val INV_TIME = 500
    private val SPAWN_TIME = 600

    var touchX=-10f //Hide touch
    var touchY=-10f //Hide touch

    var enemyx = arrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
    var enemyy = arrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
    var on_screen = arrayOf(-1, -1, -1, -1, -1, -1, -1)

    var points = 0
    var lives = 3
    var invTime = INV_TIME
    var spawnTime = SPAWN_TIME
    var enSpeed = -9f
    var theshold = 2000
    var p = 0                   //parallax
    var p1 = 0

    lateinit var spaceship: Bitmap
    lateinit var asteroid: Bitmap
    lateinit var sky: Bitmap
    lateinit var sky1: Bitmap

    //Objects
    lateinit var slabs : Array<Slabs>

    //paint
    val textPaint = Paint().apply {
        color = Color.parseColor("#AAFF0000")
        strokeWidth = 30f
        textSize=40f
    }
    val pointsPaint = Paint().apply {
        color = Color.parseColor("#AAFFFF00")
        strokeWidth = 40f
        textSize=60f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = this.activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        frgmt = this
        spaceship = decodeStream(context?.assets?.open("spaceship.png"))
        spaceship = Bitmap.createScaledBitmap(spaceship, (spaceship.width*0.21).toInt(), (spaceship.height*0.21).toInt(), false)

        asteroid = decodeStream(context?.assets?.open("asteroid.png"))
        asteroid = Bitmap.createScaledBitmap(asteroid, (asteroid.width*0.57).toInt(), (asteroid.height*0.57).toInt(), false)

        sky = decodeStream(context?.assets?.open("sky.png"))
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

        sky = Bitmap.createScaledBitmap(sky, (screenWidth).toInt(), (screenHeight*2).toInt(), false)
        sky1 = rotateBitmap(sky, 180f)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //gameView.setOnTouchListener(this)
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

    override fun onDestroy() {
        super.onDestroy()

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

    /*override fun onTouch(v: View?, event: MotionEvent?): Boolean {
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
    }*/

    inner class GameView(context: Context?) : View(context) {

        override fun onDraw(canvas: Canvas) {
            points += 1
            super.onDraw(canvas)
            var tilt = 0f
            if(initialTilt == 0f) initialTilt = orientation[1]
            if(orientation[1]<initialTilt-0.1f) tilt = initialTilt-0.1f
            else if(orientation[1]>initialTilt+0.1f) tilt = initialTilt+0.1f
            else tilt = orientation[1]
            var displ = asin(tilt-initialTilt)
            if(displ>1) displ = 1f
            else if(displ<-1) displ = -1f
            vx = displ*120f

            with(canvas){
                drawRGB(0,0,0)
                withTranslation(0f){
                    drawBitmap(sky, 0f, -screenHeight+p, null)
                }
                p = ((p+3)%screenHeight).toInt()

                withTranslation(0f){
                    drawBitmap(sky1, 0f, -screenHeight+p1, null)
                }
                p1 = ((p1+6)%screenHeight).toInt()

                drawText(""+points, 10f, 100f,pointsPaint)
                drawText(""+lives, 10f, 200f,textPaint)
                drawText(""+enSpeed, 10f, 250f,textPaint)

                withMatrix(viewPortMatrix) {
                    if(invTime != INV_TIME){
                        if((invTime < 500 && invTime > 336) || (invTime < 172 && invTime >8)){
                            drawBitmap(spaceship,ballx-(spaceship.width/2),bally-(spaceship.height/2),pointsPaint)
                        }
                        else{
                            drawBitmap(spaceship,ballx-(spaceship.width/2),bally-(spaceship.height/2),null)
                        }
                    }
                    else{
                        drawBitmap(spaceship,ballx-(spaceship.width/2),bally-(spaceship.height/2),null)
                    }
                }

                if(vy > 20f) vy = 20f
                if(vy < -20f) vy = -20f
                ballx+=vx

                var rnd_pos = 0
                for(e in enemyy.indices){
                    if(on_screen[enemyy.size-1] == -1) spawnTime -= 1
                    if(on_screen[e] == -1 && spawnTime < 0){
                        //spawn temporizzato dei nemici
                        on_screen[e] = 0
                        spawnTime = SPAWN_TIME
                    }
                    if(on_screen[e] == 0){
                        rnd_pos = Random.nextInt(6)
                        enemyx[e] = rnd_pos*180f + Random.nextInt(50)
                        enemyy[e] = 2000f + Random.nextInt(50)
                        on_screen[e] = 1
                    }
                    withMatrix(viewPortMatrix) {
                        drawBitmap(asteroid,enemyx[e]-(asteroid.width/2),enemyy[e]-(asteroid.height/2),null)
                    }
                    enemyy[e] += enSpeed
                    if(enemyy[e]<-100f && on_screen[e] == 1) on_screen[e] = 0
                }
                if(points==1000) enSpeed-=1
                if(points==2000) enSpeed-=1
                if(points==3000) enSpeed-=1
                if(points==4000) enSpeed-=1
                if(points==5000) enSpeed-=1
                if(points==6000) enSpeed-=1
                if(points==7000) enSpeed-=1
                if(points==8000) enSpeed-=1

                if(ballx>screenWidth) ballx = screenWidth
                else if(ballx<0) ballx = 0f
                //collision detection
                collisionDetection()
                invalidate()
            }
        }

        private fun collisionDetection(){
            for(e in enemyy.indices){
                if(invTime != INV_TIME) invTime -= 1
                if(invTime == 0) invTime = INV_TIME
                if(bally<enemyy[e]+110f && bally>enemyy[e]-110f && ballx>enemyx[e]-110f && ballx<enemyx[e]+110f && invTime == INV_TIME){
                    lives -= 1
                    invTime -= 1
                }
                if(lives == 0) {
                    //passare il punteggio alla schermata di fine
                    val bundle = Bundle()
                    bundle.putInt("points", points)
                    NavHostFragment.findNavController(frgmt).navigate(R.id.endFragment, bundle)
                }
            }
        }
    }
    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
    }
}