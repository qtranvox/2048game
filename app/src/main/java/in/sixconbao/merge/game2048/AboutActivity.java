package in.sixconbao.merge.game2048;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import in.sixconbao.merge.game2048.R;

import in.sixconbao.merge.game2048.Service.Music;


public class AboutActivity extends AppCompatActivity {

    Music.HomeWatcher mHomeWatcher;
    private InterstitialAd interstitialAdHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        SharedPreferences sp = getSharedPreferences("music_settings", MODE_PRIVATE);
        if (!sp.getBoolean("mute_music", false)) {
            playMusic();
        }

        interstitialAd();
        Animation scaleAnim = AnimationUtils.loadAnimation(AboutActivity.this, R.anim.scale_animation);
        Button btnClose = findViewById(R.id.btn_close_about);
        btnClose.startAnimation(scaleAnim);
        TextView tv = findViewById(R.id.textseven);
        tv.startAnimation(scaleAnim);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                playClick();
            }
        });

    }




    private void playClick() {
        SharedPreferences sp = getSharedPreferences("music_settings", MODE_PRIVATE);
        final MediaPlayer click = MediaPlayer.create(AboutActivity.this, R.raw.click);
        if (!sp.getBoolean("mute_sounds", false)) {
            click.start();
        }
    }

    private void playMusic() {
        doBindService();
        Intent music = new Intent();
        music.setClass(this, Music.class);
        startService(music);

        mHomeWatcher = new Music.HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new Music.HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

    }

    private boolean mIsBound = false;
    private Music mServ;
    private ServiceConnection Scon = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((Music.ServiceBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService() {
        bindService(new Intent(this, Music.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(Scon);
            mIsBound = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }

    }

    @Override
    public void onPause() {

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isInteractive();
        }
        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

        doUnbindService();
        Intent music = new Intent();
        music.setClass(this, Music.class);
        stopService(music);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (interstitialAdHome != null) {
            interstitialAdHome.show(this);
            interstitialAdHome.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                           AboutActivity.this.interstitialAdHome = null;
                            finish();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                           AboutActivity.this.interstitialAdHome = null;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                        }
                    });
        } else {
            super.onBackPressed();
        }

    }


    public void interstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,getString(R.string.InterstitialAd),adRequest,new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                       AboutActivity.this.interstitialAdHome = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                       AboutActivity.this.interstitialAdHome = null;
                                    }
                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                       AboutActivity.this.interstitialAdHome = null;
                                    }
                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                    }
                                });
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        interstitialAdHome = null;

                    }
                });
    }
}