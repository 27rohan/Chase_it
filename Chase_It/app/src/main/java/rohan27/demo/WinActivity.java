package rohan27.Chase_It;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class WinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        //set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF,
                WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);
        TextView text = (TextView) findViewById(R.id.textView4);
        String score = getIntent().getStringExtra("Score");
        if(score!=null) {
            text.setText("VICTORY! \n\n Score = " + score);
        }
        else{
            text.setText("VICTORY!");
        }
    }
    public void restart(View view){
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
    public void onBackPressed() {
        Intent i = new Intent(this, LevelsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
/*
    public void exit(View view){
        System.exit(1);
        finish();

    }
    */
    //listener for menu button on phone

}
