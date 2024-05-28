package in.sixconbao.merge.game2048.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import in.sixconbao.merge.game2048.R;


public class Music extends Service implements MediaPlayer.OnErrorListener {

    private final IBinder mBinder = new ServiceBinder();

    MediaPlayer mediaPlayer;

    private int length = 0;

    public Music() {
    }

    public class ServiceBinder extends Binder {
        public Music getService() {
            return Music.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_sound);
        mediaPlayer.setOnErrorListener(this);

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.3f, 0.3f);
        }


        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            public boolean onError(MediaPlayer mp, int what, int
                    extra) {

                onError(mediaPlayer, what, extra);
                return true;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
        return START_NOT_STICKY;
    }

    public void pauseMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                length = mediaPlayer.getCurrentPosition();
            }
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(length);
                mediaPlayer.start();
            }
        }
    }

    public void startMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.background_sound);
        mediaPlayer.setOnErrorListener(this);

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.2f, 0.2f);
            mediaPlayer.start();
        }

    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } finally {
                mediaPlayer = null;
            }
        }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } finally {
                mediaPlayer = null;
            }
        }
        return false;
    }

    public static class HomeWatcher {

        private Context mContext;
        private IntentFilter mFilter;
        private OnHomePressedListener mListener;
        private InnerRecevier mRecevier;

        public HomeWatcher(Context context) {
            mContext = context;
            mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        }

        public void setOnHomePressedListener(OnHomePressedListener listener) {
            mListener = listener;
            mRecevier = new InnerRecevier();
        }

        public void startWatch() {
            if (mRecevier != null) {
                mContext.registerReceiver(mRecevier, mFilter);
            }
        }


        class InnerRecevier extends BroadcastReceiver {
            final String SYSTEM_DIALOG_REASON_KEY = "reason";
            final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
            final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    if (reason != null) {
                        if (mListener != null) {
                            if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                                mListener.onHomePressed();
                            } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                                mListener.onHomeLongPressed();
                            }
                        }
                    }
                }
            }
        }

        public interface OnHomePressedListener {
            void onHomePressed();

            void onHomeLongPressed();
        }
    }
}