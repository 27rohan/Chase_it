package rohan27.Chase_It;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import static rohan27.Chase_It.Constants.FIRST_COLUMN;
import static rohan27.Chase_It.Constants.FOURTH_COLUMN;
import static rohan27.Chase_It.Constants.SECOND_COLUMN;
import static rohan27.Chase_It.Constants.THIRD_COLUMN;

//  vvv imp - do not extend listactivity here. Extend activity
// and then set the adapters within the class like you did
public class HighScoreActivity extends Activity {

    private ArrayList<HashMap<String, String>> list;

    private Context ctx = this;
    ArrayList<Node> items = new ArrayList<Node>();

    private TextView text;

    class Node{
        Node next;
        float score;
        int level;
        String date="";
        String time="";
        public Node(float s, int l, String d, String t){
            this.score = s;
            this.level = l;
            this.date = d;
            this.time = t;

        }
    }//Node class


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        //set app to full screen and keep screen on
        getWindow().setFlags(0xFFFFFFFF,
                WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        //Error here
        setContentView(R.layout.listview);


        ListView listView=(ListView)findViewById(R.id.list);
        list=new ArrayList<HashMap<String,String>>();
        HashMap<String,String> temp;

        SharedPreferences score_pref =
                getApplicationContext().getSharedPreferences("Score_Pref",
                        Context.MODE_PRIVATE);

        SharedPreferences level_pref = getApplicationContext().
                getSharedPreferences("Level_Pref",0);

        SharedPreferences date_pref = getApplicationContext().
                getSharedPreferences("Date_Pref",0);

        SharedPreferences time_pref = getApplicationContext().
                getSharedPreferences("Time_Pref",0);


        String scores[] = score_pref.getString("score_key","").split(",");
        String levels[] = level_pref.getString("level_key","").split(",");
        String dates[] = date_pref.getString("date_key", "").split(",");
        String times[] = time_pref.getString("time_key", "").split(",");

        if(scores==null || (scores.length==1 && scores[0]=="")){

        }

        else {
            Node head = new Node(Float.parseFloat(scores[0]), Integer.parseInt(levels[0]),
                    dates[0], times[0]);

            for(int i=1;i<scores.length;i++){
                Node ptr = new Node(Float.parseFloat(scores[i]), Integer.parseInt(levels[i]),
                        dates[i], times[i]);

                //Inserting before head
                if(ptr.score>head.score){
                ptr.next = head;
                head = ptr;
                }
                else {

                    Node traverse = head;
                    Node prev = null;

                    while (traverse != null && traverse.score > ptr.score) {
                        prev = traverse;
                        traverse = traverse.next;

                    }


                    prev.next = ptr;
                    ptr.next = traverse;
                }//else
            }//for on i

            SharedPreferences.Editor score_editor = score_pref.edit();
            SharedPreferences.Editor level_editor = level_pref.edit();
            SharedPreferences.Editor date_editor = date_pref.edit();
            SharedPreferences.Editor time_editor = time_pref.edit();


            String score_sorted="";
            String level_sorted="";
            String date_sorted="";
            String time_sorted="";

            /*
            DecimalFormat two = new DecimalFormat("#.00");
            two.setRoundingMode(RoundingMode.CEILING);
            */

            int count=0;
            Node q = head;
            String display="";
            while(q!=null) {
                //Displaying only top 10
                if(count<10) {
                    temp=new HashMap<String, String>();
                    temp.put(FIRST_COLUMN, String.valueOf((int)q.score));
                    temp.put(SECOND_COLUMN, String.valueOf(q.level));
                    temp.put(THIRD_COLUMN, q.date);
                    temp.put(FOURTH_COLUMN, am_pm(q.time));
                    list.add(temp);


                    /*
                    listValues.add(String.valueOf(two.format(q.score)) + "\t" + String.valueOf(q.level)
                            + "\t" + q.date + "\t" + q.time);
                            */
                    /*listValues.add("Score -"+String.valueOf(q.score) + ", Level -" + String.valueOf(q.level) + ", Date -"
                            + q.date + ", Time -" + q.time);
                            */
                }
                score_sorted += String.valueOf(q.score)+",";
                level_sorted += String.valueOf(q.level)+",";
                date_sorted += q.date+",";
                time_sorted += q.time+",";
                //output[count] = score_sorted+level_sorted+date_sorted+time_sorted;


                ++count;

                q = q.next;

            }
            //NOT WORKING!
            if(count==0) {
                temp = new HashMap<String, String>();
                temp.put(FIRST_COLUMN, "No");
                temp.put(SECOND_COLUMN, "High-score");
                temp.put(THIRD_COLUMN, "to");
                temp.put(FOURTH_COLUMN, "show");
                list.add(temp);
            }

            ListViewAdapter adapter=new ListViewAdapter(this, list);
            listView.setAdapter(adapter);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
                {
                    int pos=position+1;
                    Toast.makeText(HighScoreActivity.this,
                            Integer.toString(pos)+" Clicked", Toast.LENGTH_SHORT).show();
                }

            });


            score_editor.putString("score_key",score_sorted);
            //DO NOT FORGET THE NEXT STEP!!!!
            score_editor.commit();
            level_editor.putString("level_key",level_sorted);
            level_editor.commit();

            date_editor.putString("date_key",date_sorted);
            date_editor.commit();
            time_editor.putString("time_key",time_sorted);
            time_editor.commit();
        }//else


    }//on_create


    public void back(View view){
    onBackPressed();
    }
    public void reset(View view){

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ctx);

        // set title
        alertDialogBuilder.setTitle("Confirm reset");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to reset your high score?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity

                        SharedPreferences score_pref =
                                getApplicationContext().getSharedPreferences("Score_Pref",
                                        Context.MODE_PRIVATE);
                        SharedPreferences level_pref = getApplicationContext().
                                getSharedPreferences("Level_Pref",0);

                        SharedPreferences date_pref = getApplicationContext().
                                getSharedPreferences("Date_Pref",0);

                        SharedPreferences time_pref = getApplicationContext().
                                getSharedPreferences("Time_Pref",0);


                        SharedPreferences.Editor score_editor = score_pref.edit();
                        SharedPreferences.Editor level_editor = level_pref.edit();
                        SharedPreferences.Editor date_editor = date_pref.edit();
                        SharedPreferences.Editor time_editor = time_pref.edit();

                        //DO NOT FORGET THE NEXT STEP!!!!
                        time_editor.commit();

                        //DO NOT FORGET THE NEXT STEP!!!!
                        date_editor.commit();

                        score_editor.putString("score_key", "");
                        //DO NOT FORGET THE NEXT STEP!!!!
                        score_editor.commit();
                        level_editor.putString("level_key", "");
                        level_editor.commit();

                        date_editor.putString("date_key", "");
                        date_editor.commit();
                        time_editor.putString("time_key","");
                        time_editor.commit();

                        Intent i = new Intent(ctx, HomeActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });



        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }//reset
    public void onBackPressed() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

     public class ListViewAdapter extends BaseAdapter {

        public ArrayList<HashMap<String, String>> list;
        Activity activity;
        TextView txtFirst;
        TextView txtSecond;
        TextView txtThird;
        TextView txtFourth;
        public ListViewAdapter(Activity activity,ArrayList<HashMap<String, String>> list){
            super();
            this.activity=activity;
            this.list=list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub



            LayoutInflater inflater=activity.getLayoutInflater();

            if(convertView == null){

                convertView=inflater.inflate(R.layout.row_layout, null);

                txtFirst=(TextView) convertView.findViewById(R.id.score);
                txtSecond=(TextView) convertView.findViewById(R.id.level);
                txtThird=(TextView) convertView.findViewById(R.id.date);
                txtFourth=(TextView) convertView.findViewById(R.id.time);

            }

            HashMap<String, String> map=list.get(position);
            txtFirst.setText(map.get(FIRST_COLUMN));
            txtSecond.setText(map.get(SECOND_COLUMN));
            txtThird.setText(map.get(THIRD_COLUMN));
            txtFourth.setText(map.get(FOURTH_COLUMN));

            return convertView;
        }

    }//class Adapter

    public static String am_pm(String s){
        int hour = Integer.parseInt(s.substring(0,2));
        String output;
        if(hour>11) {
            if(hour>12){
            hour-=12;
            }
            output = String.valueOf(hour);
            output += s.substring(2, 5) + " PM";
        }
        else{
            if(hour==0){
                hour+=12;
            }
            output = String.valueOf(hour);
            output += s.substring(2, 5) + " AM";
        }
        return output;
    }



    }//class

