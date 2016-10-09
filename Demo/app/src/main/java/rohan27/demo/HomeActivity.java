package rohan27.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class HomeActivity extends AppCompatActivity {
private Context ctx = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar


        //set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF,
        WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    public void show_levels(View view){
        Intent i = new Intent(this, LevelsActivity.class);
        startActivity(i);

    }

    public void gyro_start(View view){
        Intent i = new Intent(this, GyroActivity.class);
        startActivity(i);

    }
    public void p2p_start(View view){
        Intent i = new Intent(this, WiFiServiceDiscoveryActivity.class);
        startActivity(i);

    }

    public void high_score(View view){
      Intent i = new Intent(this, HighScoreActivity.class);
        startActivity(i);
    }

    public void exit(View view){
        onBackPressed();
    }

    public void onBackPressed(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ctx);

        // set title
        alertDialogBuilder.setTitle("Confirm exit");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        System.exit(1);
                        /*
                        Intent i = new Intent(ctx, HomeActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        */
                        //finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        /*onResume();
                        countDownTimer = new MyCountDownTimer(total, interval);
                        countDownTimer.start();
*/
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
}//class HomeActivity
