package com.kiatech.kia;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.EmotionOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Activity to init and run the TFLite models
// Work is in progress here

public class RunModelActivity extends AppCompatActivity {

    Interpreter tflite;

    EditText Input;
    TextView Output;
    Button Convert;

    String textEmo = "";

    String totalUserSpeech = "";

    Hashtable<String, Integer> facialEmotionMap = new Hashtable<>();

    private static final String MODEL_PATH = "newimagemodel2.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 48;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    TextView DetectFacialEmotion;
    ImageView btnToggleCamera, PlayPause;
    private CameraView cameraView;

    boolean isPlaying = false;

    Handler handler = new Handler();

    SpeechRecognizer speechRecognizer;
    Intent SpeechIntent;
    boolean isListening = false;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_model);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

        cameraView = findViewById(R.id.cameraView);
        textViewResult = findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnToggleCamera = findViewById(R.id.ivbtnflip);
        DetectFacialEmotion = findViewById(R.id.tvdetectemotion);
        PlayPause = findViewById(R.id.ivplaypause);

        sharedPreferences = getSharedPreferences("KiaSharedPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        cameraView.setSoundEffectsEnabled(false);
        cameraView.toggleFacing();
        cameraView.setFocusable(true);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = cropImage(bitmap);

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                bitmap = toGrayscale(bitmap);

                String emo = classifier.recognizeImage(bitmap);
                facialEmotionMap.put(emo, facialEmotionMap.getOrDefault(emo, 0)+1);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        });

        DetectFacialEmotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

        PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying){
                    isPlaying = true;

                    PlayPause.setImageResource(R.drawable.ic_pause);
                    int delay = 5000;

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DetectFacialEmotion.performClick();
                            handler.postDelayed(this, delay);
                        }
                    }, delay);

                    if ((ContextCompat.checkSelfPermission(RunModelActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(RunModelActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(RunModelActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)){
                        ActivityCompat.requestPermissions(RunModelActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, 1000);
                    }
                    else if (!isListening){ // If already listening, no need for init
                        if (speechRecognizer == null){ //If null, then init
                            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(RunModelActivity.this);

                            SpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            SpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            SpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, RunModelActivity.this.getPackageName());

                            SpeechRecognitionListener speechRecognitionListener = new SpeechRecognitionListener();
                            speechRecognizer.setRecognitionListener(speechRecognitionListener);
                        }
                        speechRecognizer.startListening(SpeechIntent); //Start listening to user commands
                    }
                }
                else{
                    isPlaying = false;
                    PlayPause.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacksAndMessages(null);

                    speechRecognizer.stopListening();
                    speechRecognizer.destroy();
                    isListening = false;

                    Log.d("Final user text", totalUserSpeech);

                    getTextEmotion(totalUserSpeech);
                }
            }
        });

        initTensorFlowAndLoadModel();
    }

    class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.d("Status", "ReadyForSpeech");
        }

        @Override
        public void onBeginningOfSpeech() {
            isListening = true;
            Log.d("Status", "BeginningOfSpeech");
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            isListening = false;
            Log.d("Status", "EndOfSpeech");
        }

        @Override
        public void onError(int i) {
            Log.d("Status", "Error:"+i);
            speechRecognizer.startListening(SpeechIntent);
        }

        @Override
        public void onResults(Bundle bundle) { // When speech input of user received
            Log.d("---- Bundle ----", bundle.toString());
            ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            totalUserSpeech += result.get(0);
            isListening = true;
            speechRecognizer.startListening(SpeechIntent);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }

    String getImageEmo(){
        ArrayList<String> emos = new ArrayList<>(facialEmotionMap.keySet());
        ArrayList<Integer> freqs = new ArrayList<>(facialEmotionMap.values());
        return emos.get(freqs.indexOf(Collections.max(freqs)));
    }

    public void getTextEmotion(String msg)
    {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
            /*{
                "apikey": "dcmI9Bcsrcem5IMRn3RFZNv7uhP0hUli3QyNCSVPI9qL",
              "iam_apikey_description": "Auto-generated for key d1e93c75-4f0c-40ab-ac49-e6cfa43a1f84",
              "iam_apikey_name": "Auto-generated service credentials",
              "iam_role_crn": "crn:v1:bluemix:public:iam::::serviceRole:Manager",
              "iam_serviceid_crn": "crn:v1:bluemix:public:iam-identity::a/b5cb3cb6b44d408cbd1b6dcac51d2d18::serviceid:ServiceId-825db978-fdfb-4125-9121-44a4a3a9cd73",
              "url": "https://api.us-east.natural-language-understanding.watson.cloud.ibm.com/instances/35a3c571-6c4b-48fa-9ecf-            c34fe4a4350a"
            }*/

            IamAuthenticator authenticator = new IamAuthenticator("dcmI9Bcsrcem5IMRn3RFZNv7uhP0hUli3QyNCSVPI9qL");
            NaturalLanguageUnderstanding naturalLanguageUnderstanding = new NaturalLanguageUnderstanding("2020-08-01", authenticator);
            naturalLanguageUnderstanding.setServiceUrl("https://api.us-east.natural-language-understanding.watson.cloud.ibm.com/instances/35a3c571-6c4b-48fa-9ecf-c34fe4a4350a");

            EmotionOptions emotion = new EmotionOptions.Builder().build();

            Features features = new Features.Builder()
                    .emotion(emotion)
                    .build();

            com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions parameters = new com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions.Builder()
                    .features(features)
                    .text(msg)
                    .build();

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    AnalysisResults response = naturalLanguageUnderstanding
                            .analyze(parameters)
                            .execute()
                            .getResult();

                    try {
                        JSONObject jsonObject = new JSONObject(response.getEmotion().toString()).getJSONObject("document").getJSONObject("emotion");
                        ArrayList<Double> list = new ArrayList<>(Arrays.asList(jsonObject.getDouble("anger"), jsonObject.getDouble("disgust"), jsonObject.getDouble("fear"), jsonObject.getDouble("joy"), jsonObject.getDouble("sadness")));
                        double max = Collections.max(list);
                        String[] cats = new String[]{"Sad", "Sad", "Sad", "Happy", "Sad"};

                        textEmo = cats[list.indexOf(max)];

                        String temo = textEmo;
                        String iemo = getImageEmo();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (temo.equals("Happy")){
                                    textViewResult.setText("Glad to know that you're happy!");
                                }
                                else{
                                    textViewResult.setText("Sorry to know that you're sad!");
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    Bitmap cropImage(Bitmap bitmap){
        return Bitmap.createBitmap(bitmap, 0, bitmap.getHeight()/2-bitmap.getWidth()/2, bitmap.getWidth(), bitmap.getWidth());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(getAssets(), MODEL_PATH, LABEL_PATH, INPUT_SIZE, QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
