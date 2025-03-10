package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText etAdminEmail;
    private Button btnSendLink;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);

        etAdminEmail = findViewById(R.id.etAdminEmail);
        btnSendLink = findViewById(R.id.btnSendLink);
        progressBar = findViewById(R.id.progressBar);

        btnSendLink.setOnClickListener(v -> sendSignInLink());
        checkSignInLink();
    }

    private void sendSignInLink() {
        String email = etAdminEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter admin email!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.sendSignInLinkToEmail(email, getActionCodeSettings())
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("adminEmail", email);
                        editor.apply();
                        Toast.makeText(this, "Sign-in link sent! Check your email.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send link", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkSignInLink() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            String emailLink = intent.getData().toString();
            Log.d("DeepLink", "Received deep link: " + emailLink);

            if (auth.isSignInWithEmailLink(emailLink)) {
                String email = sharedPreferences.getString("adminEmail", null);
                if (email != null) {

                    auth.signInWithEmailLink(email, emailLink)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("DeepLink", "Sign-in successful!");
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        Log.d("DeepLink", "user role for UID: " + user.getUid());
                                        verifyAdminRole(user.getUid());
                                    }
                                } else {
                                    Toast.makeText(this, "Sign-in failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "No email saved. Please enter your email again.", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(this, AdminLoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            }
        }
    }

    private void verifyAdminRole(String userId) {
        Log.d("DeepLink", "FirestoreUID: " + userId);

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Log.d("DeepLink", "Firestore returned role: " + role);

                        if ("admin".equals(role)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isAdminLoggedIn", true);
                            editor.apply();

                            Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            auth.signOut();
                            Toast.makeText(this, "Access Denied! Only Admins Can Log In.", Toast.LENGTH_SHORT).show();
                            restartLogin();
                        }
                    } else {
                        auth.signOut();
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                        restartLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    auth.signOut();
                    Toast.makeText(this, "Error verifying admin role!", Toast.LENGTH_SHORT).show();
                    restartLogin();
                });
    }

    private void restartLogin() {
        Intent intent = new Intent(AdminLoginActivity.this, AdminLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    private ActionCodeSettings getActionCodeSettings() {
        return ActionCodeSettings.newBuilder()
                .setUrl("https://krushiconnect.page.link/PZXe")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(getPackageName(), true, null)
                .build();
    }
}
