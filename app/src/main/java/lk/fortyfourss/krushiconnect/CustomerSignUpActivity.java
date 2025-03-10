package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CustomerSignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etMobile;
    private EditText etStreet, etHousingNo, etCity, etDistrict, etPostalCode;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView ivTogglePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etPassword = findViewById(R.id.etPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);

        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == 129) {
                etPassword.setInputType(144);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                etPassword.setInputType(129); // Hide password
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            }
            etPassword.setSelection(etPassword.getText().length());
        });


        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etMobile = findViewById(R.id.etMobile);
        etStreet = findViewById(R.id.etStreet);
        etHousingNo = findViewById(R.id.etHousingNo);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etPostalCode = findViewById(R.id.etPostalCode);
        btnSignUp = findViewById(R.id.btnSignUp);
        Button btnGoToSignIn = findViewById(R.id.btnGoToSignIn);
        progressBar = findViewById(R.id.progressBar);

        btnSignUp.setOnClickListener(v -> registerCustomer());
        btnGoToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSignUpActivity.this, CustomerSignInActivity.class);
            startActivity(intent);
            finish();
        });



    }

    private void registerCustomer() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String housingNo = etHousingNo.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(mobile) || TextUtils.isEmpty(street) || TextUtils.isEmpty(housingNo) ||
                TextUtils.isEmpty(city) || TextUtils.isEmpty(district) || TextUtils.isEmpty(postalCode)) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> address = new HashMap<>();
                        address.put("street", street);
                        address.put("housingNo", housingNo);
                        address.put("city", city);
                        address.put("district", district);
                        address.put("postalCode", postalCode);

                        Map<String, Object> customer = new HashMap<>();
                        customer.put("email", email);
                        customer.put("username", username);
                        customer.put("mobile", mobile);
                        customer.put("address", address); // Address stored as a map
                        customer.put("role", "customer");
                        customer.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(userId).set(customer)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, CustomerSignInActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
