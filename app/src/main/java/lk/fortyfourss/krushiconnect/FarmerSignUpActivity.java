package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FarmerSignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etMobile, etCity, etDistrict, etHousingNo, etPostalCode, etStreet;
    private Button btnSignUp, btnAlreadyHaveAccount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView ivTogglePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore.setLoggingEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_signup);

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
        etUsername = findViewById(R.id.etUsername);
        etMobile = findViewById(R.id.etMobile);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etHousingNo = findViewById(R.id.etHousingNo);
        etPostalCode = findViewById(R.id.etPostalCode);
        etStreet = findViewById(R.id.etStreet);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnAlreadyHaveAccount = findViewById(R.id.btnAlreadyHaveAccount);

        btnSignUp.setOnClickListener(v -> registerFarmer());

        btnAlreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerSignUpActivity.this, FarmerSignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerFarmer() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String housingNo = etHousingNo.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();
        String street = etStreet.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and Password required!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        Map<String, Object> address = new HashMap<>();
                        address.put("city", city);
                        address.put("district", district);
                        address.put("housingNo", housingNo);
                        address.put("postalCode", postalCode);
                        address.put("street", street);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("username", username);
                        userData.put("mobile", mobile);
                        userData.put("address", address); //nested map ekt
                        userData.put("role", "farmer");
                        userData.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(FarmerSignUpActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(FarmerSignUpActivity.this, FarmerSignInActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(FarmerSignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    } else {
                        Toast.makeText(FarmerSignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

