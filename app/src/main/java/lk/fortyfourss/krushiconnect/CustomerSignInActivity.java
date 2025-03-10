package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerSignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnGoToSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView ivTogglePassword;

    private static final String PREFS_NAME = "CustomerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isCustomerLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == 129) { //pw hidden
                etPassword.setInputType(144); // shown pw
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                etPassword.setInputType(129);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoToSignUp = findViewById(R.id.btnGoToSignUp);
        progressBar = findViewById(R.id.progressBar);

        btnSignIn.setOnClickListener(v -> loginCustomer());

        btnGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSignInActivity.this, CustomerSignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginCustomer() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter email and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();


                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (documentSnapshot.exists() && "customer".equals(documentSnapshot.getString("role"))) {
                                        saveCustomerLoginState();

                                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, HomeActivity.class)); // Redirect to logged-in Home
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Access Denied! Only customers can log in.", Toast.LENGTH_SHORT).show();
                                        auth.signOut();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCustomerLoginState() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
}

