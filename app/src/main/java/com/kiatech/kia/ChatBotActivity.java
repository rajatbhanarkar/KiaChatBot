package com.kiatech.kia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.telecom.Call;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatBotActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    ListView Chats;
    ImageView GoogleMic, Logo, Chart;
    ChatCustomAdapter chatCustomadapter;

    TextToSpeech textToSpeech = null;
    SpeechRecognizer speechRecognizer;
    Intent SpeechIntent;
    boolean isListening = false;
    boolean loaded = true;
    boolean isFullScreen = true;

    ArrayList<String> Messages = new ArrayList<>();
    String res;
    String userName = "";

    int count = 0;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.darkBG));

        sharedPreferences = getSharedPreferences("KiaSharedPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Chats = (ListView)findViewById(R.id.lvchats);
        GoogleMic = (ImageView)findViewById(R.id.ivgooglemic);
        Logo = (ImageView)findViewById(R.id.ivlogo);
        Chart = (ImageView)findViewById(R.id.ivchart);
        relativeLayout = (RelativeLayout)findViewById(R.id.rlmain);

        GoogleMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((ContextCompat.checkSelfPermission(ChatBotActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(ChatBotActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(ChatBotActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)){
                    ActivityCompat.requestPermissions(ChatBotActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, 1000);
                }
                if (!isListening){
                    if (speechRecognizer == null){
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ChatBotActivity.this);

                        SpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        SpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        SpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ChatBotActivity.this.getPackageName());

                        SpeechRecognitionListener speechRecognitionListener = new SpeechRecognitionListener();
                        speechRecognizer.setRecognitionListener(speechRecognitionListener);
                    }
                    speechRecognizer.startListening(SpeechIntent);
                }
            }
        });

        GoogleMic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ChatBotActivity.this, "Stopped Listening", Toast.LENGTH_SHORT).show();
                if (speechRecognizer != null){
                    speechRecognizer.destroy();
                }
                return true;
            }
        });

        Logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RunModelActivity.class);
                startActivity(intent);
            }
        });

        Chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HealthStatusActivity.class);
                startActivity(intent);
            }
        });

        //------------------------------------ User Intro ------------------------------------

        chatCustomadapter = new ChatCustomAdapter();
        Chats.setAdapter(chatCustomadapter);

        if (!sharedPreferences.contains("FirstTime")){
            RegisterMessage("Hello! I'm Kia, your assistant. May I know your name?");
        }
        else{
            RegisterMessage("Welcome back "+sharedPreferences.getString("UserName", "Rajat")+"! So happy to see you again! What can I do for you?");
        }

        chatCustomadapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                int currentCount = chatCustomadapter.getCount();
            }
        });
    }

    class SpeechRecognitionListener implements RecognitionListener{

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
        public void onResults(Bundle bundle) {
            ArrayList<String> result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            res = result.get(0);
            res = ""+res.substring(0, 1).toUpperCase()+res.substring(1);

            Messages.add(res);
            chatCustomadapter.notifyDataSetChanged();

            if (!sharedPreferences.contains("FirstTime")){
                if (count == 0){
                    res = res.replace("My name is ", "");
                    res = ""+res.substring(0, 1).toUpperCase()+res.substring(1);
                    userName = res;
                    editor.putString("UserName", userName); editor.apply(); editor.commit();

                    RegisterMessage("Hi "+userName+"! So happy to have you here! I would really love to know you, and for that, you need to answer some questions. So are you ready?");
                    //RegisterMessage("Hello "+userName+"! These are the things I can do for you:\n\nTo open an app say, Open, APPNAME\n\nTo open a website say, Connect, WEBSITE\n\nTo call a contact say, Call, CONTACT NAME");
                }
                else if (count == 1){
                    RegisterMessage("Great! So, are you a Man or a Woman?");
                }
                else if (count == 2){
                    res = res.replace("I am a", "");
                    res = ""+res.substring(0, 1).toUpperCase()+res.substring(1);
                    editor.putString("UserGender", res); editor.apply(); editor.commit();

                    RegisterMessage("Cool! How old are you?");
                }
                else if (count == 3){
                    res = res.replace("I am ", "");
                    res = res.replace("years old", "");
                    res = ""+res.substring(0, 1).toUpperCase()+res.substring(1);
                    editor.putString("UserAge", res); editor.apply(); editor.commit();

                    RegisterMessage("Amazing! What's your favourite hobby?");
                }
                else if (count == 4){
                    editor.putString("UserHobby", res); editor.apply(); editor.commit();

                    RegisterMessage("That's interesting! So you're "+sharedPreferences.getString("UserName", "Rajat")+", a "+sharedPreferences.getString("UserAge", "21")+" year old "+sharedPreferences.getString("UserGender", "Male")+" who loves "+sharedPreferences.getString("UserHobby", "Coding")+"!\n\nShould we proceed?");
                }
                else if (count == 5){
                    Messages.add("Awesome! I have been learning to do quite a few things, and this is what I have learnt:\n\nOPEN an App\n(Ex: OPEN WhatsApp)\n\nCONNECT to a website\n(Ex: CONNECT google.com)\n\nCALL a person\n(Ex: CALL Rajat)\n\nWhat would you like me to do for you?");
                    Speak("Awesome! I have been learning to do quite a few things, and this is what I have learnt:\n\nOPEN an App\n\nCONNECT to a website\n\nCALL a person\n\nWhat would you like me to do for you?");
                    chatCustomadapter.notifyDataSetChanged();
                }
                else{
                    if (!sharedPreferences.contains("FirstTime")){
                        editor.putString("FirstTime", "No"); editor.apply(); editor.commit();
                    }
                    doTask(res);
                }
            }
            else{
                doTask(res);
            }

            count++;
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }

    class ChatCustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return Messages.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (i%2 != 0){
                view = getLayoutInflater().inflate(R.layout.send_layout, null);
                TextView SentText = (TextView)view.findViewById(R.id.tvsend);
                SentText.setText(""+Messages.get(i));
            }
            else{
                view = getLayoutInflater().inflate(R.layout.recieve_layout, null);
                TextView RecievedText = (TextView)view.findViewById(R.id.tvrecieve);
                RecievedText.setText(""+Messages.get(i));
            }
            return view;
        }
    }

    void Speak(final String stringToSpeak){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {

                        }

                        @Override
                        public void onDone(String s) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isListening){
                                        GoogleMic.performClick();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(String s) {

                        }
                    });

                    Bundle params = new Bundle();
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");

                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.speak(""+stringToSpeak, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
                    //textToSpeech.speak(""+stringToSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    void doTask(String instruction){
        String[] ss = instruction.split(" ");

        if (ss[0].equalsIgnoreCase("Open")){
            OpenApp(instruction);
        }
        else if (ss[0].equalsIgnoreCase("Connect")){
            ConnectSite(instruction);
        }
        else if (ss[0].equalsIgnoreCase("Call")){
            CallSomeone(instruction);
        }
        else{
            RegisterMessage(instruction);
        }
    }









    //------------------------------- Feature Tasks ------------------------------------------------

    void OpenApp(String instruction){
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        boolean found = false;
        instruction = instruction.replace("Open ", "");
        for (ApplicationInfo packageInfo : packages){
            String packageName = packageInfo.packageName;
            String AppName = ""+packageManager.getApplicationLabel(packageInfo);

            if (AppName.toLowerCase().contains(instruction.toLowerCase())){
                found = true;
                final Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent == null){
                    RegisterMessage("Sorry, App not found!");
                }
                else{
                    RegisterMessage("Opening "+instruction);
                    startActivity(launchIntent);
                    break;
                }
            }
        }
        if (!found){
            RegisterMessage("Sorry, App not found!");
        }
    }

    void ConnectSite(String instruction){
        instruction = instruction.replace("Connect ","");
        final String url = "http://www."+instruction;

        RegisterMessage("Connecting to "+instruction);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    void CallSomeone(String instruction){
        instruction = instruction.replace("Call ","");
        if (instruction.matches("[0-9]+")){
            instruction.replaceAll("\\s+", "");
            RegisterMessage("Calling "+instruction);

            final String phone = instruction;
            callPhone(phone);
        }
        else{
            final String phone = getPhoneNumber(instruction);

            if (phone != null){
                RegisterMessage("Calling "+instruction);
                callPhone(phone);
            }
            else{
                RegisterMessage("No such contact found!");
            }
        }
    }

    void SMSSomeone(String instruction){
        instruction = instruction.replace("Call ","");
        if (instruction.matches("[0-9]+")){
            instruction.replaceAll("\\s+", "");
            RegisterMessage("Calling "+instruction);

            final String phone = instruction;
            callPhone(phone);
        }
        else{
            final String phone = getPhoneNumber(instruction);

            if (phone != null){
                RegisterMessage("Calling "+instruction);
                callPhone(phone);
            }
            else{
                RegisterMessage("No such contact found!");
            }
        }
    }

    void EmailSomeone(String instruction){

    }

    void GoogleSearch(String instruction){
        instruction = instruction.replace("Search ","");
        final String url = "http://www."+instruction;

        RegisterMessage("Connecting to "+instruction);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    void CovidStatus(String instruction){

    }

    void CricketScores(String instruction){

    }

    void FootBallScores(String instruction){

    }

    void GoldRate(String instruction){

    }

    void StockPrices(String instruction){

    }


    void RegisterMessage(String message){
        Speak(""+message);
        Messages.add(""+message);
        chatCustomadapter.notifyDataSetChanged();
    }

    String getPhoneNumber(String name){
        String phone = null;

        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%"+name+"%'";
        String[] projection = new String[]{ ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, null);
        if (cursor.moveToFirst()){
            phone = cursor.getString(0);
        }
        cursor.close();

        return phone;
    }

    @SuppressLint("MissingPermission")
    void callPhone(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number));
        startActivity(callIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!loaded){
           GoogleMic.performClick();
        }

        loaded = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        speechRecognizer.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null){
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null){
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED && grantResults[2]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "All Permissions Granted!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == 3000){
            Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
        }*/
    }
}