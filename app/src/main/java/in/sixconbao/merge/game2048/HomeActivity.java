package in.sixconbao.merge.game2048;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import in.sixconbao.merge.game2048.R;

import in.sixconbao.merge.game2048.Service.Music;

import com.google.android.gms.ads.AdError;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences sp;
    Music.HomeWatcher mHomeWatcher;
    private InterstitialAd interstitialAdHome;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        sp = getSharedPreferences("music_settings", MODE_PRIVATE);
        if (!sp.getBoolean("mute_music", false)) {
            playMusic();
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        interstitialAd();
        Animation btnScaleAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.scale_animation);

        Button btnNewGame = findViewById(R.id.btn_new_game);
        btnNewGame.startAnimation(btnScaleAnim);
        btnNewGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playClick();
                Intent intent = new Intent(HomeActivity.this, ChooseBoardActivity.class);
                startActivity(intent);
            }
        });

        Button btnHowtoPlay = findViewById(R.id.btn_load_game);
        btnHowtoPlay.startAnimation(btnScaleAnim);
        btnHowtoPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playClick();
                Intent intent = new Intent(HomeActivity.this, GameActivity.class);
                intent.putExtra("tutorial", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right_side, R.anim.slide_out_left_side);
            }
        });

        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.startAnimation(btnScaleAnim);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick();
                settingsDialog();
            }
        });

        Button btnScoreBoard = findViewById(R.id.btn_Score_board);
        btnScoreBoard.startAnimation(btnScaleAnim);
        btnScoreBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick();
                scoreBoardDialog();
            }
        });

        Button btnAbout = findViewById(R.id.btn_About);
        btnAbout.startAnimation(btnScaleAnim);
        btnAbout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playClick();
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
        Button btnMoreGames = findViewById(R.id.btn_MoreGames);
        btnMoreGames.startAnimation(btnScaleAnim);
        btnMoreGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick();
                if (interstitialAdHome != null) {

                    if (mServ != null) {
                        mServ.pauseMusic();
                    }
                    interstitialAdHome.show(HomeActivity.this);
                    interstitialAdHome.setFullScreenContentCallback(
                            new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    HomeActivity.this.interstitialAdHome = null;
                                    if (mServ != null) {
                                        mServ.resumeMusic();
                                        goToUrl(getResources().getString(R.string.moreGameURL));
                                    }
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    HomeActivity.this.interstitialAdHome = null;
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                }
                            });
                }
                else{
                    goToUrl(getResources().getString(R.string.moreGameURL));
                }

            }
        });
    }


    private void goToUrl(String s) {
        Uri uri = Uri.parse(s);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void scoreBoardDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences("2048", MODE_PRIVATE);
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.scoreboard);

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.90);
        Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);


        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ListView listView = dialog.findViewById(R.id.listview_score_board);
        ArrayList<ScoreBoardBuilder.Score> classicScores = ScoreBoardBuilder.createClassicArrayList(sharedPreferences, "0");
        ArrayList<ScoreBoardBuilder.Score> blocksScores = ScoreBoardBuilder.createClassicArrayList(sharedPreferences, "1");
        ArrayList<ScoreBoardBuilder.Score> shuffleScores = ScoreBoardBuilder.createClassicArrayList(sharedPreferences, "2");
        final ScoreBoardBuilder.ScoreAdapter scoreAdapterClassic = new ScoreBoardBuilder.ScoreAdapter(classicScores, this);
        final ScoreBoardBuilder.ScoreAdapter scoreAdapterBlocks = new ScoreBoardBuilder.ScoreAdapter(blocksScores, this);
        final ScoreBoardBuilder.ScoreAdapter scoreAdapterShuffle = new ScoreBoardBuilder.ScoreAdapter(shuffleScores, this);
        listView.setAdapter(scoreAdapterClassic);

        final Animation rightInAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_in_right_side);
        final Animation leftInAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_in_left_side);
        final Animation rightOutAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_out_right_side);
        final Animation leftOutAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_out_left_side);

        Animation anim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.scale_animation);
        final TextView currentModeTv = dialog.findViewById(R.id.textview_mode_type);
        final int[] index = {0};

        ImageButton btnRight = dialog.findViewById(R.id.right_btn);
        btnRight.startAnimation(anim);
        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index[0] == 2) {
                    index[0] = 0;
                } else {
                    index[0]++;
                }
                leftOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switch (index[0]) {
                            case 0:
                                playClick();
                                listView.setAdapter(scoreAdapterClassic);
                                currentModeTv.setText(getString(R.string.mode_classic));
                                break;
                            case 1:
                                playClick();
                                listView.setAdapter(scoreAdapterBlocks);
                                currentModeTv.setText(getString(R.string.mode_blocks));
                                break;
                            case 2:
                                playClick();
                                listView.setAdapter(scoreAdapterShuffle);
                                currentModeTv.setText(getString(R.string.mode_shuffle));
                                break;
                        }
                        listView.startAnimation(rightInAnim);
                    }
                });
                listView.startAnimation(leftOutAnim);


            }
        });

        ImageButton btnLeft = dialog.findViewById(R.id.left_btn);
        btnLeft.startAnimation(anim);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index[0] == 0) {
                    index[0] = 2;
                } else {
                    index[0]--;
                }

                rightOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        switch (index[0]) {
                            case 0:
                                playClick();
                                listView.setAdapter(scoreAdapterClassic);
                                currentModeTv.setText(getString(R.string.mode_classic));
                                break;
                            case 1:
                                playClick();
                                listView.setAdapter(scoreAdapterBlocks);
                                currentModeTv.setText(getString(R.string.mode_blocks));
                                break;
                            case 2:
                                playClick();
                                listView.setAdapter(scoreAdapterShuffle);
                                currentModeTv.setText(getString(R.string.mode_shuffle));
                                break;
                        }
                        listView.startAnimation(leftInAnim);
                    }
                });
                listView.startAnimation(rightOutAnim);


            }
        });
        Animation btnScaleAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.scale_animation);
        Button btnClose = dialog.findViewById(R.id.close_button);
        btnClose.setAnimation(btnScaleAnim);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick();
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void settingsDialog() {


        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.setting);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();

        final RadioGroup rgMusic = dialog.findViewById(R.id.radiogroup_music_select);
        final RadioButton musicOn = dialog.findViewById(R.id.music_on);
        final RadioButton musicOff = dialog.findViewById(R.id.music_off);


        if (!sp.getBoolean("mute_music", false)) {
            rgMusic.check(musicOn.getId());
            musicOn.setTextColor(Color.rgb(90, 85, 83));
        } else {
            rgMusic.check(musicOff.getId());
            musicOff.setTextColor(Color.rgb(90, 85, 83));
        }


        rgMusic.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                playClick();
                if (checkedId == musicOn.getId()) {
                    musicOn.setTextColor(Color.rgb(90, 85, 83));
                    musicOff.setTextColor(Color.rgb(167, 168, 168));
                    if (mServ != null) {
                        mServ.startMusic();
                    } else {
                        playMusic();
                    }
                    sp.edit().putBoolean("mute_music", false).apply();

                } else {
                    musicOff.setTextColor(Color.rgb(90, 85, 83));
                    musicOn.setTextColor(Color.rgb(167, 168, 168));
                    if (mServ != null) {
                        mServ.stopMusic();
                    }
                    sp.edit().putBoolean("mute_music", true).apply();

                }
            }
        });


        RadioGroup rgSound = dialog.findViewById(R.id.radiogroup_sound_select);
        final RadioButton soundOn = dialog.findViewById(R.id.sound_on);
        final RadioButton soundOff = dialog.findViewById(R.id.sound_off);

        if (!sp.getBoolean("mute_sounds", false)) {
            rgSound.check(soundOn.getId());
            soundOn.setTextColor(Color.rgb(90, 85, 83));
        } else {
            rgSound.check(soundOff.getId());
            soundOff.setTextColor(Color.rgb(90, 85, 83));
        }


        rgSound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == soundOn.getId()) {
                    sp = getSharedPreferences("music_settings", MODE_PRIVATE);
                    sp.edit().putBoolean("mute_sounds", false).apply();
                    playClick();
                    soundOn.setTextColor(Color.rgb(90, 85, 83));
                    soundOff.setTextColor(Color.rgb(167, 168, 168));

                } else {
                    sp.edit().putBoolean("mute_sounds", true).apply();
                    soundOff.setTextColor(Color.rgb(90, 85, 83));
                    soundOn.setTextColor(Color.rgb(167, 168, 168));
                }
            }
        });

        Animation btnScaleAnim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.scale_animation);
        Button closeBtn = dialog.findViewById(R.id.close_button);
        closeBtn.startAnimation(btnScaleAnim);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClick();

                if (interstitialAdHome != null) {

                    if (mServ != null) {
                        mServ.pauseMusic();
                    }
                    interstitialAdHome.show(HomeActivity.this);
                    interstitialAdHome.setFullScreenContentCallback(
                            new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    HomeActivity.this.interstitialAdHome = null;
                                    if (mServ != null) {
                                        mServ.resumeMusic();
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    HomeActivity.this.interstitialAdHome = null;
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                }
                            });
                }
                else{
                    dialog.dismiss();
                }


            }
        });

    }

    private void playClick() {
        SharedPreferences sp = getSharedPreferences("music_settings", MODE_PRIVATE);
        final MediaPlayer click = MediaPlayer.create(HomeActivity.this, R.raw.click);
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
    protected void onPause() {
        super.onPause();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this, Music.class);
        stopService(music);

    }

    @Override
    public void onBackPressed() {
        if (interstitialAdHome != null) {
            interstitialAdHome.show(this);
            interstitialAdHome.setFullScreenContentCallback(
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            HomeActivity.this.interstitialAdHome = null;
                            finish();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            HomeActivity.this.interstitialAdHome = null;
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
        InterstitialAd.load(
                this,
                getString(R.string.InterstitialAd),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        HomeActivity.this.interstitialAdHome = interstitialAd;
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        HomeActivity.this.interstitialAdHome = null;
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        HomeActivity.this.interstitialAdHome = null;
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
