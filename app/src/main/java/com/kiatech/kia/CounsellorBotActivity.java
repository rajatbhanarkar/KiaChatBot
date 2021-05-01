package com.kiatech.kia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CounsellorBotActivity extends AppCompatActivity implements BotReply{

    // Main UI Components

    RelativeLayout relativeLayout;
    ListView Chats;
    ImageView GoogleMic, Chart, Menu;
    TextView Logo;
    ChatCustomAdapter chatCustomadapter;

    // Text-to-speech and Speech Recognizer for the voice controlled app

    TextToSpeech textToSpeech = null;
    SpeechRecognizer speechRecognizer;
    Intent SpeechIntent;
    boolean isListening = false;
    boolean loaded = true;

    // Storing and Displaying chat messages

    ArrayList<String> Messages = new ArrayList<>();
    String res;
    String userName = "";
    int count = 0;

    // Local storage for storing user data

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    // Dialogflow connections to the cloud server

    SessionsClient sessionsClient;
    SessionName sessionName;
    String uuid = UUID.randomUUID().toString();

    ArrayList<String> toBeSaidList = new ArrayList<>();
    String counsgender = "female";
    String encodedmsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counsellor_bot);

        // UI Stuff, making nav bar and status bar color same as background

        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

        // Initialize shared Preferences

        sharedPreferences = getSharedPreferences("KiaSharedPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Ask for permissions

        if ((ContextCompat.checkSelfPermission(CounsellorBotActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(CounsellorBotActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(CounsellorBotActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(CounsellorBotActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, 1000);
        }

        // Declaring all IDs and linking to XML

        Chats = (ListView)findViewById(R.id.lvchats);
        GoogleMic = (ImageView)findViewById(R.id.ivgooglemic);
        Logo = (TextView)findViewById(R.id.ivlogo);
        Chart = (ImageView)findViewById(R.id.ivchart);
        Menu = (ImageView)findViewById(R.id.chatbotmenu);
        relativeLayout = (RelativeLayout)findViewById(R.id.rlmain);

        Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(CounsellorBotActivity.this, R.style.myDialog);
                dialog.setContentView(R.layout.chatbot_settings_layout);
                dialog.show();
            }
        });

        // Init Speech Recognizer onClick Mic Icon

        GoogleMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Permission Check
                if ((ContextCompat.checkSelfPermission(CounsellorBotActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(CounsellorBotActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(CounsellorBotActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)){
                    ActivityCompat.requestPermissions(CounsellorBotActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, 1000);
                }
                else if (!isListening){ // If already listening, no need for init
                    if (speechRecognizer == null){ //If null, then init
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(CounsellorBotActivity.this);

                        SpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        SpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        SpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, CounsellorBotActivity.this.getPackageName());
                        SpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000000);
                        //SpeechIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000000);

                        SpeechRecognitionListener speechRecognitionListener = new SpeechRecognitionListener();
                        speechRecognizer.setRecognitionListener(speechRecognitionListener);
                    }
                    speechRecognizer.startListening(SpeechIntent); //Start listening to user commands
                }
            }
        });

        // Long press mic icon to stop listening

        GoogleMic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(CounsellorBotActivity.this, "Stopped Listening", Toast.LENGTH_SHORT).show();
                if(speechRecognizer != null){
                    speechRecognizer.destroy();
                }
                return true;
            }
        });

        // Direct user to health status activity

        Chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HealthStatusActivity.class);
                startActivity(intent);
            }
        });

        // initialize chat display adapter

        chatCustomadapter = new ChatCustomAdapter();
        Chats.setAdapter(chatCustomadapter);

        // Init DialogFlow ChatBot, connect to agent at cloud server

        setUpBot();
    }

    // -------------------------------- Speech Recognition Listener --------------------------------

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
            res = result.get(0); // Getting top result
            res = ""+res.substring(0, 1).toUpperCase()+res.substring(1);

            Messages.add("u"+res);
            chatCustomadapter.notifyDataSetChanged();

            // sending message to the dialogflow agent
            sendMsgToBot(res);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }

    // ------------- Chat Adapter to display messages for user side and bot side -------------------

    class ChatCustomAdapter extends BaseAdapter {

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
            String message = Messages.get(i);

            if (message.charAt(0) == 'u'){ //User side message UI
                view = getLayoutInflater().inflate(R.layout.send_layout, null);
                TextView SentText = (TextView)view.findViewById(R.id.tvsend);
                SentText.setText(""+message.substring(1));
            }
            else if (message.charAt(0) == 'c'){ //ChatBot side message UI
                view = getLayoutInflater().inflate(R.layout.recieve_layout, null);
                TextView RecievedText = (TextView)view.findViewById(R.id.tvrecieve);
                ImageView CounsPic = (ImageView)view.findViewById(R.id.ivcounsellorpic);
                CounsPic.setVisibility(View.INVISIBLE);
                RecievedText.setText(""+message.substring(1));
            }
            else{ //ChatBot side message UI
                view = getLayoutInflater().inflate(R.layout.recieve_layout, null);
                TextView RecievedText = (TextView)view.findViewById(R.id.tvrecieve);
                ImageView CounsPic = (ImageView)view.findViewById(R.id.ivcounsellorpic);
                if (counsgender.equals("male")){
                    CounsPic.setImageResource(R.drawable.malecounsellor);
                }
                RecievedText.setText(""+message.substring(1));
            }
            return view;
        }
    }

    // -------------------------- Bot speaks the required message ----------------------------------

    void Speak(final String stringToSpeak){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) { // When successfully retrieved user speech
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            // when speaking by bot starts
                        }

                        @Override
                        public void onDone(String s) { // when speaking by bot done, click mic button to make app hands-free
                            runOnUiThread(new Runnable() { // accessing UI thread from background
                                @Override
                                public void run() {
                                    if (!isListening){
                                        if (toBeSaidList.isEmpty()){
                                            GoogleMic.performClick();
                                        }
                                        else{
                                            String mm = toBeSaidList.get(0);
                                            toBeSaidList.remove(0);
                                            RegisterMessage(mm, 1);
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(String s) {
                            Toast.makeText(CounsellorBotActivity.this, "TTS Error", Toast.LENGTH_SHORT).show();
                        }
                    });


                    // Putting TTS Engine ID in bundle params for speech

                    Bundle params = new Bundle();
                    params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");

                    // Setting TTS Engine

                    textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.setSpeechRate(1f);

                    if (counsgender.equals("male")){
                        textToSpeech.setVoice(new Voice("en-us-x-sfg#male_3-local", new Locale("en", "US"), 400, 200, true, null));
                        textToSpeech.setSpeechRate(1.2f);
                    }

                    // Finally, bot speaks the required text

                    textToSpeech.speak(""+stringToSpeak, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
                }
            }
        }, "com.google.android.tts");
    }

    void RegisterMessage(String message, int mode){
        if (counsgender.equals("male")){
            message = message.replace("Alexa", "Alex");
        }
        Speak(""+message); // bot speaks the message
        if (mode==0){
            Messages.add("b"+message); // adding message to arraylist of all messages
        }
        else{
            Messages.add("c"+message); // adding message to arraylist of all messages
        }
        chatCustomadapter.notifyDataSetChanged(); // notifying adapter that new message has been added
    }

    //-------------------------------------- Bot Stuff ---------------------------------------------

    // setup bot and connection to dialogflow agent

    void setUpBot(){
        try{
            // Getting project credentials and details

            InputStream inputStream = getResources().openRawResource(R.raw.lovelifeagent);
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream); //.createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId = ((ServiceAccountCredentials)googleCredentials).getProjectId();

            // Load latest chatbot settings from server

            SessionsSettings.Builder builder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = builder.setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials)).build();

            // Initialize settings and apply
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);

            //sending problem msg to dialogflow agent
            sendMsgToBot("I had a break-up recently");
        }
        catch (Exception e){
            Toast.makeText(this, "There was an error, please try again later", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // send user command to server in background

    void sendMsgToBot(String msg){

        if (!encodedmsg.equals("")){
            msg = encodedmsg;
            encodedmsg = "";
        }

        QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("en-US")).build();

        Log.d("SessionClient", sessionsClient.toString());
        Log.d("SessionName", sessionName.toString());

        new SendMessageInBg(CounsellorBotActivity.this, sessionName, sessionsClient, queryInput).execute();
    }

    // getting most appropriate response from dialogflow agent

    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if(returnResponse != null){
            String botReply = returnResponse.getQueryResult().getFulfillmentText();
            if(!botReply.isEmpty()){
                String[] sssss = botReply.split("\n\n\n\n");

                if (sssss.length>1){
                    encodedmsg = sssss[1];
                }

                String[] sss = sssss[0].split("\n\n");
                for(int i=1 ; i<sss.length ; i++){
                    toBeSaidList.add(sss[i]);
                }
                RegisterMessage(sss[0], 0);
            }
            else{
                RegisterMessage("Sorry, something went wrong!", 0);
            }
        }
        else{
            RegisterMessage("Connection Failed!", 0);
        }
    }

    //------------------------------- Feature Tasks ------------------------------------------------

    // Do some basic tasks as per user commands, Not in our main aim

    /*void doTask(String instruction){

        if (instruction.contains("Call")){
            instruction = instruction.substring(5);
            CallSomeone(instruction);
        }
        else if (instruction.contains("SMS")){
            instruction = instruction.substring(8);
            SMSSomeone(instruction);
        }
        else if (instruction.contains("Open")){
            instruction = instruction.substring(5);
            OpenApp(instruction);
        }
        else{
            GoogleSearchAPI(instruction);
        }
    }

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

    void GoogleSearchAPI(String instruction){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://www.googleapis.com/customsearch/v1?key=AIzaSyCNnc0Bp2hEp7pXs8DlrzYToNvqWPb0Jwk&cx=5bdc2e9ff2c8164b8&q="+instruction;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject object = response.getJSONArray("items").getJSONObject(0);
                    String result = object.getString("snippet");
                    result = result.replaceAll("\n", " ");
                    result = result.replace("...", ".");
                    RegisterMessage(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("x-rapidapi-host", "google-search3.p.rapidapi.com");
                //headers.put("x-rapidapi-key", "3658c53c9bmshb0d41e3e1a8001bp15f6c7jsn733b479da7ab");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
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
    }*/

    // ----------------------------- Activity Life Cycle Stuff -------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

        if(!loaded){
            if(speechRecognizer == null){
                GoogleMic.performClick();
            }
        }

        loaded = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(speechRecognizer != null){
            speechRecognizer.stopListening();
        }
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

        // Permission granting and checking

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
    }
}

// Previously hardcoded stuff, will now be replaced by chatbot actions

/**/

/*if (!sharedPreferences.contains("FirstTime")){
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
count++;*/

/*if (!sharedPreferences.contains("FirstTime")){
    RegisterMessage("Hello! I'm Kia, your assistant. May I know your name?");
}
else{
    RegisterMessage("Welcome back "+sharedPreferences.getString("UserName", "Rajat")+"! So happy to see you again! What can I do for you?");
}*/

/*chatCustomadapter.registerDataSetObserver(new DataSetObserver() {
    @Override
    public void onChanged() {
        super.onChanged();

        int currentCount = chatCustomadapter.getCount();
    }
});*/

