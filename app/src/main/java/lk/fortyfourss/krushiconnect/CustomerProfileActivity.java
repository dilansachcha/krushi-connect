package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CustomerProfileActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etMobile;
    private EditText etStreet, etHousingNo, etCity, etDistrict, etPostalCode;
    private ImageView ivEditUsername, ivEditMobile, ivEditAddress;
    private Button btnSaveChanges, btnChangePassword;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = user.getUid();

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etStreet = findViewById(R.id.etStreet);
        etHousingNo = findViewById(R.id.etHousingNo);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etPostalCode = findViewById(R.id.etPostalCode);

        ivEditUsername = findViewById(R.id.ivEditUsername);
        ivEditMobile = findViewById(R.id.ivEditMobile);
        ivEditAddress = findViewById(R.id.ivEditAddress);

        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);

        btnSaveChanges.setVisibility(View.GONE);

        loadCustomerProfile();

        ivEditUsername.setOnClickListener(v -> enableEditing(etUsername));
        ivEditMobile.setOnClickListener(v -> enableEditing(etMobile));
        ivEditAddress.setOnClickListener(v -> {
            enableEditing(etStreet);
            enableEditing(etHousingNo);
            enableEditing(etCity);
            enableEditing(etDistrict);
            enableEditing(etPostalCode);
        });

        btnSaveChanges.setOnClickListener(v -> updateCustomerProfile());

        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(CustomerProfileActivity.this, ChangePasswordActivity.class)));
    }

    private void loadCustomerProfile() {
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etUsername.setText(documentSnapshot.getString("username"));
                etEmail.setText(documentSnapshot.getString("email"));
                etMobile.setText(documentSnapshot.getString("mobile"));

                Map<String, Object> address = (Map<String, Object>) documentSnapshot.get("address");
                if (address != null) {
                    etStreet.setText((String) address.get("street"));
                    etHousingNo.setText((String) address.get("housingNo"));
                    etCity.setText((String) address.get("city"));
                    etDistrict.setText((String) address.get("district"));
                    etPostalCode.setText((String) address.get("postalCode"));
                }
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void enableEditing(EditText editText) {
        editText.setEnabled(true);
        btnSaveChanges.setVisibility(View.VISIBLE);
    }

    private void updateCustomerProfile() {
        String username = etUsername.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String street = etStreet.getText().toString().trim();
        String housingNo = etHousingNo.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Username and Mobile are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("mobile", mobile);

        Map<String, Object> address = new HashMap<>();
        address.put("street", street);
        address.put("housingNo", housingNo);
        address.put("city", city);
        address.put("district", district);
        address.put("postalCode", postalCode);
        updates.put("address", address);

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    loadCustomerProfile();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                });
    }
}

