package rohan27.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class LevelsActivity extends AppCompatActivity {
    boolean isFirsSelection = true;

    private Spinner spinner1;
    private Button btnSubmit;

    public void single_start(View view){

             Log.i("Level ", (String)spinner1.getSelectedItem());

        Intent i = new Intent(this, GyroActivity.class);
        i.putExtra("Level",(String)spinner1.getSelectedItem());
        startActivity(i);
    }
    public void back(View view){
        onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        //set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF,
                WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);
        spinner1 = (Spinner) findViewById(R.id.spinner1);


        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // PROBLEM - Initially it remains to the left,
                //comes to center only after something is selected first
                if(isFirsSelection == true){
                    isFirsSelection = false;}
                else{
                    ((TextView) adapterView.getChildAt(0)).setGravity(Gravity.CENTER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //.setGravity(Gravity.CENTER);
                return;
            }
        });

    }
    public void onBackPressed() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
