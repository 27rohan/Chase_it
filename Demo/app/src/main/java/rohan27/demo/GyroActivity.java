package rohan27.demo;

//import java.util.DisplayMetrics;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//import rohan27.demo.pkg.R;

public class GyroActivity extends AppCompatActivity implements SensorEventListener {
    private Context ctx = this;
    private long total = 60000;
    boolean b = false;

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            text.setText("done!");
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),LoseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            total = millisUntilFinished;
            text.setText("Countdown:" + millisUntilFinished / 1000);
        }

    }//class MyCountDownTimer

    //Countdown shit
    private CountDownTimer countDownTimer,countDownScore;
    public TextView text, display;
    private final long startTime = 60 * 1000;//1 minute for the game
    private final long interval = 1 * 1000;

    //Game shit
    float score=0;

    BallView mBallView = null;
    BallView monsterBallView = null;
    float monsterspeed_x;
    float monsterspeed_y;
    double distance;
    int level = 1;

    float mBallRadius=0;

    Handler RedrawHandler_2 = new Handler(); //so redraw occurs in main thread
    Timer mTmr = null;
    TimerTask mTsk = null;
    int mScrWidth, mScrHeight;
    android.graphics.PointF mBallPos, mBallSpd;

    final String TAG = "Motion";
    Sensor mSensor;
    SensorManager mSensorManager;
    Random RandomGenerator = new Random();



    public void save(String filename, double score,
                            Context ctx) {
        FileInputStream fis;
        double temp=0;
        try {

            fis = ctx.openFileInput(filename);
            temp = fis.read();
            fis.close();
           // b = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (score > temp) {

            FileOutputStream fos;
            try {
                fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);

                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(score);
                oos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//save function

    @Override
    public void onCreate(Bundle savedInstanceState) {

        b = false;

        level = Integer.parseInt(getIntent().getStringExtra("Level"));
        score = level*60;
        monsterspeed_x = level + RandomGenerator.nextInt(level+1);
        monsterspeed_y = -(level + RandomGenerator.nextInt(level+1));
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        //set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF,
                LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gyro);

        text = (TextView) findViewById(R.id.textView1);
        display = (TextView) findViewById(R.id.textView5);
        countDownTimer = new MyCountDownTimer(startTime, interval);
        //text.setText(String.valueOf(startTime / 1000));
        countDownTimer.start();
        //countDownScore = new MyCountDownTimer();
        //countDownScore.start();

        //create pointer to main screen
        final FrameLayout mainView_2 =
                (android.widget.FrameLayout)findViewById(R.id.main_view_2);
        //get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        mScrWidth = display.getWidth();
        mScrHeight = display.getHeight();

        mBallRadius = mScrHeight/50;

        mBallPos = new android.graphics.PointF();
        mBallSpd = new android.graphics.PointF();
        //create variables for ball position and speed
        mBallPos.x = mScrWidth/2;
        mBallPos.y = mScrHeight-mBallRadius;
        mBallSpd.x = 0;
        mBallSpd.y = 0;
        //create initial ball
        mBallView = new BallView(this, mBallPos.x, mBallPos.y,mBallRadius,0xFF00FF00);
        monsterBallView = new BallView(this,mScrWidth/2, mScrHeight/2,mBallRadius,0xffff0000);
        //mainView_2.addView(monster);
        mainView_2.addView(monsterBallView);
        mainView_2.addView(mBallView); //add ball to main screen
        mBallView.invalidate(); //call onDraw in BallView

        monsterBallView.invalidate();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
        else{
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); //otherwise use the magnetometer-based one
        }

        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);

        Log.i(TAG, "Sensors Available: ");
        for(Sensor s : mSensorManager.getSensorList(Sensor.TYPE_ALL))
            Log.i(TAG, s.toString());


        if(mSensor == null) { //we don't have a relevant sensor
            Log.v(TAG, "Switching to accelerometer");
            ((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
                    new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            //set ball speed based on phone tilt (ignore Z axis)
                            mBallSpd.y = event.values[0]*level/2;


                            mBallSpd.x = event.values[1]*level/2;
                            //timer event will redraw ball
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        } //ignore
                    },
                    ((SensorManager) getSystemService(Context.SENSOR_SERVICE))
                            .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
                    SensorManager.SENSOR_DELAY_NORMAL);

            //finish();

        }//Accelerometer

    } //OnCreate


    //@Override - gyrometer
    public void onSensorChanged(SensorEvent event) {
        Log.v(TAG, "Raw: "+ Arrays.toString(event.values));
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        SensorManager.getOrientation(rotationMatrix, orientation);
        //don't take any sine or cosine, take the angles themselves
        //ignore z rotation

        mBallSpd.x = (float) Math.toDegrees(orientation[2])/22*level;
        mBallSpd.y = (float) Math.toDegrees(orientation[1])/22*level;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //leave blank for now
    }

    //listener for menu button on phone
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add("Exit"); //only one menu item
        return super.onCreateOptionsMenu(menu);
    }

    //listener for menu item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        if (item.getTitle() == "Exit") //user clicked Exit
            finish(); //will call onPause
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onPause() //app moved to background, stop background threads
    {
        countDownTimer.cancel();
        //android.util.Log.d("Stopping countdown timer");
        mTmr.cancel(); //kill\release timer (our only background thread)
        mTmr = null;
        mTsk = null;
        super.onPause();
    }
    @Override
    public void onStop(){
        //countDownTimer.cancel();

    super.onStop();
    }

    @Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {

        //create timer to move ball to new position
        mTmr = new Timer();

            mTsk = new TimerTask() {
                public void run() {

                    score -= 0.01 * level;
                    if (monsterBallView.x <= mBallRadius || monsterBallView.x >= (mScrWidth - mBallRadius)) {
                        monsterspeed_x *= -1;
                    }
                    if (monsterBallView.y <= mBallRadius || monsterBallView.y >= (mScrHeight - mBallRadius)) {
                        monsterspeed_y *= -1;
                    }
                    monsterBallView.x += monsterspeed_x;
                    monsterBallView.y += monsterspeed_y;


                    //  if debugging with external device,
                    //  a log cat viewer will be needed on the device
                    android.util.Log.d("TiltBall","Timer Hit - " + mBallPos.x + ":" + mBallPos.y);
                    //considering radius
                    if (mBallView.x > mBallRadius && mBallView.x < (mScrWidth - mBallRadius)) {
                        mBallPos.x += mBallSpd.x;
                    }
                    else if (mBallView.y > mBallRadius && mBallView.y < (mScrHeight - mBallRadius)) {
                        mBallPos.y += mBallSpd.y;
                    }

                    else {//corners
                        if (Math.abs(mBallSpd.x) > Math.abs(mBallSpd.y)) {
                            mBallPos.x += mBallSpd.x;

                        } else {
                            mBallPos.y += mBallSpd.y;

                        }
                    }

                    //do not let the ball go off the screen
                    if (mBallPos.x > mScrWidth - mBallRadius) mBallPos.x = mScrWidth - mBallRadius;
                    if (mBallPos.y > mScrHeight - mBallRadius)
                        mBallPos.y = mScrHeight - mBallRadius;
                    if (mBallPos.x < mBallRadius) mBallPos.x = mBallRadius;
                    if (mBallPos.y < mBallRadius) mBallPos.y = mBallRadius;

                    mBallView.x = mBallPos.x;
                    mBallView.y = mBallPos.y;

                    //redraw ball. Must run in background thread to prevent thread lock.
                    RedrawHandler_2.post(new Runnable() {
                        public void run() {

                            display.setText("Score:" + score);
                            mBallView.invalidate();
                            monsterBallView.invalidate();
                            //mBallView2.invalidate();
                            distance = Math.sqrt(Math.pow((mBallView.x - monsterBallView.x), 2)
                                    + Math.pow((mBallView.y - monsterBallView.y), 2));
                            if (distance <= (2 * mBallRadius)) {

                                countDownTimer.cancel();

                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), WinActivity.class);
                                intent.putExtra("Score", String.valueOf(score));
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                if(!b) {
                                    b = true;


                                    SharedPreferences score_pref = getApplicationContext().
                                            getSharedPreferences("Score_Pref", 0);

                                    SharedPreferences level_pref = getApplicationContext().
                                            getSharedPreferences("Level_Pref", 0);

                                    SharedPreferences date_pref = getApplicationContext().
                                            getSharedPreferences("Date_Pref", 0);

                                    SharedPreferences time_pref = getApplicationContext().
                                            getSharedPreferences("Time_Pref", 0);

                                    SharedPreferences.Editor score_editor = score_pref.edit();
                                    score_editor.putString("score_key", score_pref.getString("score_key", "") +
                                            String.valueOf(score) + ",");
                                    //DO NOT FORGET THE NEXT STEP!!!!
                                    score_editor.commit();

                                    SharedPreferences.Editor level_editor = level_pref.edit();
                                    level_editor.putString("level_key", level_pref.getString("level_key", "") +
                                            String.valueOf(level) + ",");
                                    //DO NOT FORGET THE NEXT STEP!!!!
                                    level_editor.commit();

                                    DateFormat date_format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                                    Date dateobj = new Date();

                                    String date = date_format.format(dateobj).substring(0, 8);

                                    android.util.Log.d("Date ",date_pref.getString("date_key", "") +
                                            date + ",");
                                    String time = date_format.format(dateobj).substring(9);
                                    android.util.Log.d("Time ",time_pref.getString("time_key", "") +
                                            time + ",");

                                    SharedPreferences.Editor date_editor = date_pref.edit();
                                    date_editor.putString("date_key", date_pref.getString("date_key", "") +
                                            date + ",");

                                    //DO NOT FORGET THE NEXT STEP!!!!
                                    date_editor.commit();

                                    SharedPreferences.Editor time_editor = time_pref.edit();
                                    time_editor.putString("time_key", time_pref.getString("time_key", "") +
                                            time + ",");

                                    //DO NOT FORGET THE NEXT STEP!!!!
                                    time_editor.commit();

                                    score_pref = getApplicationContext().
                                            getSharedPreferences("Score_Pref", 0);

                                    String top = score_pref.getString("score_key", "");
                                    float high = Float.parseFloat(top.split(",")[0]);
                                    if (top == null || top == "" || score > high) {
                                        NotificationCompat.Builder mBuilder =
                                                (NotificationCompat.Builder) new NotificationCompat.
                                                        Builder(getApplicationContext())
                                                        .setSmallIcon(R.drawable.notification_icon)
                                                        .setContentTitle(getString(R.string.app_name))
                                                        .setContentText("New high score - " +
                                                                String.valueOf((int)score));
                                        // Creates an explicit intent for an Activity in your app
                                        Intent resultIntent = new Intent(getApplicationContext(),
                                                HighScoreActivity.class);

                                        // The stack builder object will contain an artificial back stack for the
                                        // started Activity.
                                        // This ensures that navigating backward from the Activity leads out of
                                        // your application to the Home screen.
                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                                        // Adds the back stack for the Intent (but not the Intent itself)
                                        stackBuilder.addParentStack(HighScoreActivity.class);
                                        // Adds the Intent that starts the Activity to the top of the stack
                                        stackBuilder.addNextIntent(resultIntent);
                                        PendingIntent resultPendingIntent =
                                                stackBuilder.getPendingIntent(
                                                        0,
                                                        PendingIntent.FLAG_UPDATE_CURRENT
                                                );
                                        mBuilder.setContentIntent(resultPendingIntent);
                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        // Id (1) allows you to update the notification later on.
                                        mNotificationManager.notify(1, mBuilder.build());

                                    }//new high score
                                }//only once



                            }//victory

                        }//run
                    }
                    );
                    //}//if

                }
            }; // TimerTask


            mTmr.schedule(mTsk, 10, 10); //start timer
            super.onResume();




    } // onResume
    @Override
    public void onDestroy() //main thread stopped
    {
        super.onDestroy();
        //wait for threads to exit before clearing app
        System.runFinalizersOnExit(true);
        //remove app from memory
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //listener for config change.
    //This is called when user tilts phone enough to trigger landscape view
    //we want our app to stay in portrait view, so bypass event
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    public void onBackPressed() {
        onPause();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ctx);

        // set title
        alertDialogBuilder.setTitle("Confirm exit");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to exit your current game?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        onDestroy();

                        Intent i = new Intent(ctx, HomeActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);

                        //finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        onResume();
                        countDownTimer = new MyCountDownTimer(total, interval);
                        countDownTimer.start();

                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });



        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

} //GyroActivity