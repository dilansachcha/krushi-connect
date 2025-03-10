package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FarmerSignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn, btnSignUpRedirect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView ivTogglePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore.setLoggingEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_signin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == 129) {
                etPassword.setInputType(144);
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
        btnSignUpRedirect = findViewById(R.id.btnSignUpRedirect);


        btnSignIn.setOnClickListener(v -> loginFarmer());


        btnSignUpRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerSignInActivity.this, FarmerSignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginFarmer() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter email and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists() && "farmer".equals(documentSnapshot.getString("role"))) {
                                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                                        getSharedPreferences("FarmerPrefs", MODE_PRIVATE)
                                                .edit()
                                                .putBoolean("isFarmerLoggedIn", true)
                                                .apply();

                                        startActivity(new Intent(this, FarmerDashboardActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Access denied. Only farmers can log in!", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

