package com.kiatech.kia;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView circleView;
    ImageView PlayPause;
    MediaPlayer mediaPlayer;
    StorageReference storageReference;

    boolean isPlaying = false;

    Handler handler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        circleView = (TextView)findViewById(R.id.circleview);
        PlayPause = (ImageView)findViewById(R.id.ivplaypause);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mediaPlayer = MediaPlayer.create(this, R.raw.blindinglights);
        PlaybackParams playbackParams = new PlaybackParams();
        playbackParams.setPitch(1.04f);
        mediaPlayer.setPlaybackParams(playbackParams);
        mediaPlayer.start();

        circleView.animate().scaleX(12f).scaleY(12f).alpha(0f).setDuration(4000).start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        circleView.setScaleX(1f); circleView.setScaleY(1f); circleView.setAlpha(1f);
                        circleView.animate().scaleX(12f).scaleY(12f).alpha(0f).setDuration(4000).start();
                    }
                });
                handler.postDelayed(this, 4000);
            }
        }, 4000);

        PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });

        /*PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPlaying){
                    PlayPause.setImageResource(R.drawable.ic_pause);
                    if (mediaPlayer == null){
                        storageReference = FirebaseStorage.getInstance().getReference().child("RunningWaters.mp3");

                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(uri.toString());

                                    mediaPlayer.prepareAsync();

                                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(final MediaPlayer mediaPlayer) {
                                            //LoaderDialog.dismiss();
                                            //totalTime = mediaPlayer.getDuration();
                                            //seekArc.setMaxProgress(totalTime);
                                            mediaPlayer.setLooping(true);
                                            mediaPlayer.start();
                                            //isPlaying = true;

                                            circleView.animate().scaleX(12f).scaleY(12f).alpha(0f).setDuration(4000).start();

                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            circleView.setScaleX(1f); circleView.setScaleY(1f); circleView.setAlpha(1f);
                                                            circleView.animate().scaleX(12f).scaleY(12f).alpha(0f).setDuration(4000).start();
                                                        }
                                                    });
                                                    handler.postDelayed(this, 4000);
                                                }
                                            }, 4000);
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                    else{
                        mediaPlayer.start();
                    }
                    isPlaying = true;
                }
                else{
                    PlayPause.setImageResource(R.drawable.ic_play);
                    isPlaying = false;
                    mediaPlayer.pause();
                    handler.removeCallbacksAndMessages(null);
                }
            }
        });*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}