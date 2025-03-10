package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logo = findViewById(R.id.imageView);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animations1);
        logo.startAnimation(animation);

        new Handler().postDelayed(() -> {
            Uri data = getIntent().getData();
            if (data != null) {
                String deepLink = data.toString();
                Log.d("DeepLink", "Received deep link: " + deepLink);

                if (deepLink.contains("firebaseapp.com/__/auth/action") && deepLink.contains("continueUrl=")) {
                    Uri uri = Uri.parse(deepLink);
                    String continueUrl = uri.getQueryParameter("continueUrl");

                    if (continueUrl != null && continueUrl.contains("krushiconnect.page.link/PZXe")) {
                        Log.d("DeepLink", "Extracted continueUrl: " + continueUrl);
                        openAdminLogin();
                        return;
                    }
                }

                if (deepLink.contains("krushiconnect.page.link/PZXe")) {
                    Log.d("DeepLink", "Direct deep link detected, opening AdminLoginActivity");
                    openAdminLogin();
                    return;
                }
            }

            openHomeScreen();
        }, 2500);
    }


    private void openAdminLogin() {
        Intent adminIntent = new Intent(MainActivity.this, AdminLoginActivity.class);
        adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(adminIntent);
        finish();
    }

    private void openHomeScreen() {
        Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }
}
