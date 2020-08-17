package com.kiatech.kia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

public class ChatbotActivity extends AppCompatActivity {

    TextView Result;

    TextToSpeech textToSpeech = null;
    SpeechRecognizer speechRecognizer;
    Intent SpeechIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        Result = (TextView)findViewById(R.id.tvresult);

        if ((ContextCompat.checkSelfPermission(ChatbotActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(ChatbotActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
        }
        else{
            setup();
        }

        /*if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val speechRecognizer = speech.setRecognitionListener(this)
            speechRecognizer.setRecognitionListener(this)
        } else {
            // Handle error
        }*/
    }

    void setup(){
        SpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        SpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ChatbotActivity.this.getPackageName());
        SpeechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SpeechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        }

        speechRecognizer.startListening(SpeechIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED && grantResults[2]==PackageManager.PERMISSION_GRANTED){

            }
        }
    }
}