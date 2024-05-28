package in.sixconbao.merge.game2048;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import in.sixconbao.merge.game2048.R;


public class SplaceActivity extends AppCompatActivity {

    SplaceActivity activity;
    Context context;
    AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splace_screen);
        context = activity = this;
        appUpdateManager = AppUpdateManagerFactory.create(context);
        UpdateApp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;


        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, IMMEDIATE, activity, 101);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void HomeScreen() {
        Animation scaleAnim = AnimationUtils.loadAnimation(SplaceActivity.this, R.anim.scale_anim_logoimg);
        ImageView splaceImg = findViewById(R.id.splaceImg);
        splaceImg.startAnimation(scaleAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplaceActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);

    }

    public void UpdateApp() {
        try {
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo, IMMEDIATE, activity, 101);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                    HomeScreen();
                }
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                HomeScreen();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode != RESULT_OK) {
                HomeScreen();
            } else {
                HomeScreen();
            }
        }
    }

}


