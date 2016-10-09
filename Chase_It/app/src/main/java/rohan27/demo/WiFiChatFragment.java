package rohan27.Chase_It;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This fragment handles chat related UI which includes a list view for messages
 * and a message entry field with send button.
 */

/*Do not keep anything here - send and receive will be used to transfer co-ordinates of both players
    and all this class should do is draw those balls on the screen and end when one wins
    */
public class WiFiChatFragment extends Fragment implements SensorEventListener {


    Handler RedrawHandler_2 = new Handler(); //so redraw occurs in main thread
    BallView mBallView = null;
    //BallView opponentBallView = null;
    BallView monsterBallView = null;
    Timer mTmr = null;
    TimerTask mTsk = null;
    int mScrWidth, mScrHeight;
    android.graphics.PointF mBallPos, mBallSpd, opponent, opponentPos;
    float mBallRadius=0;
    final String TAG = "Motion";
    int level = 5;
    double distance, opp_distance;
    float monsterspeed_x;
    float monsterspeed_y;
    Sensor mSensor;
    SensorManager mSensorManager;
    Random RandomGenerator = new Random();
    public void onSensorChanged(SensorEvent event) {
        Log.v(TAG, "Raw: "+ Arrays.toString(event.values));
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        SensorManager.getOrientation(rotationMatrix, orientation);
        //don't take any sine or cosine, take the angles themselves
        //ignore z rotation

        mBallSpd.x = (float) Math.toDegrees(orientation[2])/22*level;
        //-(float) Math.acos(Math.toRadians(orientation[0])));//about -X and -Z axes
        mBallSpd.y = (float) Math.toDegrees(orientation[1])/22*level;
        //- (float) Math.acos(Math.toRadians(orientation[0])));//about Y and -Z axes
        //mBallPos.z= (float)Math.toDegrees(orientation[0]))+"\u00B0");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //leave blank for now
    }




    private View view;
    private ChatManager chatManager;
    private TextView chatLine;
    private ListView listView;
    ChatMessageAdapter adapter = null;
    private List<String> items = new ArrayList<String>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.multi, container, false);

        //view = inflater.inflate(R.layout.fragment_chat, container, false);

        final FrameLayout mainView_2 =
                (android.widget.FrameLayout)view.findViewById(R.id.main_view_2);


        mBallRadius = mScrHeight/50;
        mBallPos = new android.graphics.PointF();
        mBallSpd = new android.graphics.PointF();
        opponent = new android.graphics.PointF();
        opponentPos = new android.graphics.PointF();

        //create variables for ball position and speed
        mBallPos.x = mScrWidth/2;
        mBallPos.y = mScrHeight - mBallRadius;

        mBallSpd.x = 0;
        mBallSpd.y = 0;
        opponent.x = 0;
        opponent.y = 0;

        opponentPos.x = mScrWidth/2;
        opponentPos.y = mScrHeight - mBallRadius;

        //create initial ball - PS instead of this we use getactivity
        mBallView = new BallView(getActivity(), mBallPos.x, mBallPos.y,mBallRadius,0xFF00FF00);
        //opponentBallView = new BallView(getActivity(), opponentPos.x, opponentPos.y,mBallRadius,0xff0000ff);
        monsterBallView = new BallView(getActivity(),mScrWidth/2, mScrHeight/2,mBallRadius,0xffff0000);
        //mainView_2.addView(opponentBallView);
        mainView_2.addView(monsterBallView);
        mainView_2.addView(mBallView); //add ball to main screen
        mBallView.invalidate(); //call onDraw in BallView
        //opponentBallView.invalidate();
        monsterBallView.invalidate();

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

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
            ((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE)).registerListener(
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
                    ((SensorManager)  getActivity().getSystemService(Context.SENSOR_SERVICE))
                            .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0),
                    SensorManager.SENSOR_DELAY_NORMAL);

            //finish();

        }//Accelerometer


        /*
        chatLine = (TextView) view.findViewById(R.id.txtChatLine);
        listView = (ListView) view.findViewById(android.R.id.list);
        adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1,
                items);
        listView.setAdapter(adapter);
        view.findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (gameManager != null) {
                            gameManager.write(chatLine.getText().toString()
                                    .getBytes());
                            //displays self message on screen
                            pushMessage("Me: " + chatLine.getText().toString());
                            chatLine.setText("");
                            chatLine.clearFocus();
                        }
                    }
                });
        */
        return view;

    }//onCreateView
    public interface MessageTarget {
        public Handler getHandler();
    }
    public void setChatManager(ChatManager obj) {
        chatManager = obj;
    }
    //message to be shown on the screen from buddy

    public void pushMessage(String message) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), LoseActivity.class);
        //intent.putExtra("Status",(String));

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        /*
        //adds the message to end of array
        String[] input = message.split(",");
        opponent.x = Float.parseFloat(input[0]);
        opponent.y = Float.parseFloat(input[1]);
        //opponentBallView.invalidate(); //call onDraw in BallView
        */



       /*
        adapter.add(readMessage);
        adapter.notifyDataSetChanged();
        */
    }
    /**
     * ArrayAdapter to manage chat messages.
     */
    public class ChatMessageAdapter extends ArrayAdapter<String> {
        List<String> messages = null;
        public ChatMessageAdapter(Context context, int textViewResourceId,
                                  List<String> items) {
            super(context, textViewResourceId, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            //Message to display
            String message = items.get(position);
            if (message != null && !message.isEmpty()) {

                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);
                if (nameText != null) {
                    nameText.setText(message);
                    //message being sent
                    if (message.startsWith("Me: ")) {
                        nameText.setTextAppearance(getActivity(),
                                R.style.normalText);
                    } else {
                        //message being received
                        nameText.setTextAppearance(getActivity(),
                                R.style.boldText);
                    }
                }
            }
            return v;
        }//getView()
    }//chatMesssageAdapter class

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScrHeight = displaymetrics.heightPixels;
        mScrWidth = displaymetrics.widthPixels;
/*
        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        getActivity().getWindow().setFlags(0xFFFFFFFF,
                WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/
        monsterspeed_x = level*2*(float)mScrHeight/mScrWidth;
        ///(mScrWidth+mScrHeight);
                //+ RandomGenerator.nextInt(level+1);
        monsterspeed_y = -level/2*(float)mScrWidth/mScrHeight;
                //*(float)Math.sqrt(Math.pow(mScrHeight,2)+Math.pow(mScrHeight,2));;
                //+ RandomGenerator.nextInt(level+1));
        super.onCreate(savedInstanceState);
    }//onCreate

    @Override
    public void onPause() //app moved to background, stop background threads
    {
        //android.util.Log.d("Stopping countdown timer");
        mTmr.cancel(); //kill\release timer (our only background thread)
        mTmr = null;
        mTsk = null;
        super.onPause();
    }

    @Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {

        //create timer to move ball to new position
        mTmr = new Timer();
        //String co_ordinates = null;
        mTsk = new TimerTask() {
            public void run() {
                String co_ordinates = Float.toString(mBallSpd.x) + ','
                        +          Float.toString(mBallSpd.y);
/*
        String a[] = co_ordinates.split(",");

        1 - New string that consists of
            x, y
        2 - string to byte array (as usual)
        3 - send the byte array as usual
         */


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
                android.util.Log.d("TiltBall", "Timer Hit - " + mBallPos.x + ":" + mBallPos.y);
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
                //mBallView.mX = mBallPos.x;
                //mBallView.mY = mBallPos.y;

                mBallView.x = mBallPos.x;
                mBallView.y = mBallPos.y;

/*
                if (opponentBallView.x > mBallRadius && opponentBallView.x < mScrWidth - mBallRadius) {
                    opponentPos.x += opponent.x;
                } else if (opponentBallView.y > mBallRadius && opponentBallView.y < mScrHeight - mBallRadius) {
                    opponentPos.y += opponent.y;
                } else {//corners
                    if (Math.abs(opponent.x) > Math.abs(opponent.y)) {
                        opponentPos.x += opponent.x;

                    } else {
                        opponentPos.y += opponent.y;

                    }
                }

                //do not let the ball go off the screen
                if (opponentPos.x > mScrWidth - mBallRadius) opponentPos.x = mScrWidth - mBallRadius;
                if (opponentPos.y > mScrHeight - mBallRadius)
                    opponentPos.y = mScrHeight - mBallRadius;
                if (opponentPos.x < mBallRadius) opponentPos.x = mBallRadius;
                if (opponentPos.y < mBallRadius) opponentPos.y = mBallRadius;
                //mBallView.mX = mBallPos.x;
                //mBallView.mY = mBallPos.y;

                opponentBallView.x = opponentPos.x;
                opponentBallView.y = opponentPos.y;


*/
                //THIS HANDLER IS THE ONLY PROBLEM - SOLVE IT AND YOU'RE DONE!!!
                //redraw ball. Must run in background thread to prevent thread lock.
                RedrawHandler_2.post(new Runnable() {
                                         public void run() {
                                             //opponentBallView.invalidate();
                                             mBallView.invalidate();
                                             monsterBallView.invalidate();
                                             //mBallView2.invalidate();
                                             distance = Math.sqrt(Math.pow((mBallView.x - monsterBallView.x), 2)
                                                     + Math.pow((mBallView.y - monsterBallView.y), 2));
                                             /*
                                             opp_distance = Math.sqrt(Math.pow((opponentBallView.x -
                                                     monsterBallView.x), 2)
                                                     + Math.pow((opponentBallView.y - monsterBallView.y), 2));
                                                     */
                                             if (distance <= (2 * mBallRadius)) {

                                                 if (chatManager != null) {
                                                     //send co-ordinates instead
                                                     //WORKING - LOG SEEN
                                                     Log.d(TAG, "End");

                                                     chatManager.write("End".getBytes());
/*          //displays self message on screen
            pushMessage("Me: " + chatLine.getText().toString());
            chatLine.setText("");
            chatLine.clearFocus();*/
                                                 }
                                                 //onPause();
                                                 //save("highscores", score, getApplicationContext());
                                                 Intent intent = new Intent();
                                                 intent.setClass(getActivity(), WinActivity.class);
                                                 //intent.putExtra("Status",(String));

                                                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                 startActivity(intent);
                                             }
                                             /*
                                             if (opp_distance <= (2 * mBallRadius)) {
                                                 //onPause();
                                                 //save("highscores", score, getApplicationContext());
                                                 Intent intent = new Intent();
                                                 intent.setClass(getActivity(), LoseActivity.class);
                                                 //intent.putExtra("Status",(String));

                                                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                 startActivity(intent);
                                             }
*/
                                         }

                                     }
                );
                //}//if

            }
        }; // TimerTask


        mTmr.schedule(mTsk, 10, 10); //start timer



        super.onResume();




    } // onResume
    @Override
    public void onStop(){
        super.onStop();
    }
    @Override
    public void onDestroy() //main thread stopped
    {
        super.onDestroy();
        //wait for threads to exit before clearing app
        System.runFinalizersOnExit(true);
        //remove app from memory
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}