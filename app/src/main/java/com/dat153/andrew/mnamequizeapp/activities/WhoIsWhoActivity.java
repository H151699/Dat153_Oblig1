package com.dat153.andrew.mnamequizeapp.activities;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dat153.andrew.mnamequizeapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class WhoIsWhoActivity extends AppCompatActivity {

    private TextView textViewOfImg, textView_clearText;
    private ImageView mImageView;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private int score = 0;
    private int attempt = 0;
    private TextView textView_score;
    private TextView textView_attempt;
    private TextView textViewCountDown;
    private long timerLeftInMilis = START_TIME_IN_MILLIS;
    private static final long START_TIME_IN_MILLIS = 60000; // countdown 60 seconds

    private Button btnNext, btnSubmit,btnStartPauseStart, btnAddImage, btRestart;
    private EditText editText_userInput;

    // Firebase pointer
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference imagUrl = rootRef.child("uploads");

    List<String> pictureList = new ArrayList<>();
    List<String> pictureNameList = new ArrayList<>();
    Random random = new Random(System.currentTimeMillis());




    /**
     * Load Next random image REAL-TIME from Firbase.
     *
     * Possibility image repeating
     */
    public void loadNext(){
        //randomNumber = random.nextInt(urlCount);
        //Glide.with(WhoIsWhoActivity.this).load(pictureList).into(mImageView);
        int next = random.nextInt(pictureList.size());
        Glide.with(WhoIsWhoActivity.this).load(pictureList.get(next)).into(mImageView);
        textViewOfImg.setText(pictureNameList.get(next));
    }



    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whoiswho);

        btnStartPauseStart = (Button) findViewById(R.id.btnStart_Pause_Start);
        btRestart = (Button) findViewById(R.id.btnRestart);
        btnAddImage = (Button) findViewById(R.id.btnAddImage);
        btnNext = (Button)findViewById(R.id.btnNext);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        mImageView = (ImageView)findViewById(R.id.main_imageView);
        textViewCountDown = (TextView) findViewById(R.id.textViewCDtimer);
        textViewOfImg = (TextView)findViewById(R.id.textView_showImgName);
        textView_clearText =(TextView)findViewById(R.id.textView_clearText);
        textView_score = (TextView) findViewById(R.id.textView_score);
        textView_attempt= (TextView) findViewById(R.id.textView_attempt);
        editText_userInput = (EditText)findViewById(R.id.editText_guessedName);


        /**
         * Current Score and Attempt
         */
        textView_score.setText("Score: " + 0);
        textView_attempt.setText("Attempts:" + 0 );


        /**
         * Initial state of Countdown Timer
         * Disable some btn disabled and un visible
         *
         * Disabled:
         * Submit btn, editText_userInput
         *
         * Invisible:
         * Restart(reSet) btn, textView of correct image_name
         */
        textViewCountDown.setText("00:00");
        btnSubmit.setEnabled(false);
        btRestart.setVisibility(View.INVISIBLE);
        editText_userInput.setEnabled(false);
        textViewOfImg.setVisibility(View.INVISIBLE); // Hide the correct name



        /** Start CountDown
         * btnStartQuize
         */



        /**
         *  Implement main Start_Quiz_Button: Start-Pause-Start
         */
        btnStartPauseStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(timerRunning){
                    pauseTimer();
                }else {
                    startTimer();
                }
            }
        });//btnStartPauseStart


        /**
         * Implement Restart(resetTimer) Button
         */
        btRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetTimer();
            }
        });//btRestart


        /**
         * Implement Pass_Button to get next random image from db
         */
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadNext();
            }

        });//  btnNext

        /**
         *
         * Implement Submit_Button
         *
         * Validating input,
         * Checking if name is matching with the showing image.
         *
         */
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isEmpty()){  /* check if input field is not empty */

                    if(isValidName()){

                        score = score + 1;
                        attempt = attempt + 1;

                        textView_score.setText("Score: " + score);
                        textView_attempt.setText("Attempts: " + attempt);
                        loadNext();
                        Toast.makeText(WhoIsWhoActivity.this, "Yippi!!", Toast.LENGTH_SHORT).show();

                    } else if(!isValidName()) { /* check if input name is matched to the image */

                        attempt++;
                        textView_attempt.setText("Attempts: " + attempt);
                        Toast.makeText(WhoIsWhoActivity.this, "Wrong Name! Please try next image", Toast.LENGTH_LONG).show();
                        textViewOfImg.setVisibility(View.VISIBLE);
                    }

                } else { /* if input field is empty */

                        Toast.makeText(WhoIsWhoActivity.this,"Name required!", Toast.LENGTH_SHORT).show();
                        textViewOfImg.setVisibility(View.INVISIBLE);
                }
            }
        });// Submit_Button


        /**
         *
         * Implement Clear_InputField_Txt
         * textView_clearText
         */
        textView_clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText_userInput.setText("");
            }
        }); //textView_clearText



        /**
         * Implement Add_image_Button, to get add_image Activity
         */
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseFileActivity();
            }
        }); //btnAddImage



        /**
         * Implement ValueEventListener
         *
         * Read all the data inside the reference over DataSnapshot
         */
        ValueEventListener valueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // List<String> urlList = new ArrayList<>();


                for(DataSnapshot ds : dataSnapshot.getChildren()){ /* Gives access to all of the immediate children of this snapshot. */

                    // String url = ds.getKey();
                    HashMap<String, Object> map = (HashMap<String, Object>) ds.getValue();

                    //urlList.add();
                    pictureList.add((String) map.get("imgUrl"));

                    // add image name to list
                    pictureNameList.add((String)map.get("imgName"));
                }

                    loadNext();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        imagUrl.addListenerForSingleValueEvent(valueEventListener);



    } // OnCreate



    /***********************************************************************************************
     *                          Implement methods
     **********************************************************************************************/

    /**
     * Name validation
     *
     * @return
     */
    public boolean isValidName(){
        String name1 = editText_userInput.getText().toString().trim();
        String name2 = textViewOfImg.getText().toString().trim();
       return name1.equalsIgnoreCase(name2);
     }//

    /**
     * Check editText field
     *
     * @return
     *
     */
    public boolean isEmpty() {
        if (editText_userInput.getText().toString().trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Start Timer
     *
     */
    private void startTimer(){
        countDownTimer = new CountDownTimer(timerLeftInMilis, 1000) {

            /**
             * Start Timer
             *
             * @param millisUntilFinished
             */
            @Override
            public void onTick(long millisUntilFinished) {

                timerLeftInMilis = millisUntilFinished;
                updateCountDownText();
                editText_userInput.setEnabled(true);
            }


            /**
             * When Timer's up ...
             */
            @Override
            public void onFinish() {
                btnSubmit.setEnabled(false);
                btnStartPauseStart.setVisibility(View.INVISIBLE);
                btRestart.setVisibility(View.VISIBLE);
                btnAddImage.setEnabled(true);
                btnAddImage.setEnabled(true);
                btnNext.setEnabled(false);
            }

        }.start();

        timerRunning = true;
        btnSubmit.setEnabled(true);
        btnAddImage.setEnabled(false);
        btnStartPauseStart.setText("Pause");
        btRestart.setVisibility(View.INVISIBLE);
        btnAddImage.setEnabled(false);
        editText_userInput.setText("");

    } //

    /**
     * Reset Timer
     */
     public void resetTimer(){
        timerLeftInMilis = START_TIME_IN_MILLIS;
        updateCountDownText();
        btRestart.setVisibility(View.INVISIBLE);
        btnStartPauseStart.setVisibility(View.VISIBLE);
        score = 0;
        attempt = 0;
        btnAddImage.setEnabled(true);
        textView_score.setText("Score: " +score);
        textView_attempt.setText("Attempts: " + attempt);
        editText_userInput.setText("");

    }

    /**
     * Pause Timer
     */
    public void pauseTimer(){
        countDownTimer.cancel();
        timerRunning = false;
        btnStartPauseStart.setText("Start");
        btRestart.setVisibility(View.VISIBLE);
        btnAddImage.setEnabled(false);

    }


    /**
     * Count down calculation
     *
     * updateCountDownText
     */
    private void updateCountDownText(){
        int  minutes =(int) (timerLeftInMilis / 1000) / 60;
        int  seconds = (int)  (timerLeftInMilis / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        textViewCountDown.setText(timeLeftFormatted);
    }


    /**
     * Implement method of Add_image Button
     * Go To ChooseFielActivity
     */
    public void chooseFileActivity(){
        Intent intent = new Intent(this, MultiMediaManagerActivity.class );
        startActivity(intent);

    } //


} // WhoIsWhoActivity Class
