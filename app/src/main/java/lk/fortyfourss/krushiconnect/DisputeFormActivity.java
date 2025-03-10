package lk.fortyfourss.krushiconnect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.UUID;

public class DisputeFormActivity extends AppCompatActivity {

    private TextView tvOrderId;
    private Spinner spinnerIssue;
    private EditText etDescription;
    private ImageView ivUploadImage;
    private Button btnSubmitDispute, btnSelectImage;
    private Uri imageUri;
    private String orderId;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispute_form);

        tvOrderId = findViewById(R.id.tvOrderId);
        spinnerIssue = findViewById(R.id.spinnerIssue);
        etDescription = findViewById(R.id.etDescription);
        ivUploadImage = findViewById(R.id.ivUploadImage);
        btnSubmitDispute = findViewById(R.id.btnSubmitDispute);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        orderId = getIntent().getStringExtra("orderId");

        // Display Order ID (Ensuring It's Visible)
        if (orderId != null && !orderId.isEmpty()) {
            tvOrderId.setText("Order ID: " + orderId);
        } else {
            tvOrderId.setText("Order ID: Not Found");
        }

        String[] issues = {"Wrong Product", "Damaged Product", "Incomplete Order", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIssue.setAdapter(adapter);

        btnSelectImage.setOnClickListener(v -> selectImage());

        btnSubmitDispute.setOnClickListener(v -> submitDispute());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            ivUploadImage.setImageURI(imageUri);
            ivUploadImage.setVisibility(View.VISIBLE);
        }
    }

    private void submitDispute() {
        String issue = spinnerIssue.getSelectedItem().toString();
        String description = etDescription.getText().toString();

        if (description.isEmpty()) {
            Toast.makeText(this, "Please provide a description", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("disputes")
                .whereEqualTo("orderId", orderId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(DisputeFormActivity.this, "You have already submitted a dispute for this order.", Toast.LENGTH_LONG).show();
                    } else {
                        processDisputeSubmission(issue, description, userId);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(DisputeFormActivity.this, "Error checking disputes", Toast.LENGTH_SHORT).show());
    }

    private void processDisputeSubmission(String issue, String description, String userId) {
        HashMap<String, Object> disputeData = new HashMap<>();
        disputeData.put("orderId", orderId);
        disputeData.put("userId", userId);
        disputeData.put("issue", issue);
        disputeData.put("description", description);
        disputeData.put("timestamp", System.currentTimeMillis());

        if (imageUri != null) {
            String fileName = "disputes/" + UUID.randomUUID().toString();
            StorageReference fileRef = storageReference.child(fileName);
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        disputeData.put("imageUrl", uri.toString());
                        saveDisputeToFirestore(disputeData);
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(DisputeFormActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show()
            );
        } else {
            saveDisputeToFirestore(disputeData);
        }
    }

    private void saveDisputeToFirestore(HashMap<String, Object> disputeData) {
        db.collection("disputes").add(disputeData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(DisputeFormActivity.this, "Dispute submitted successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the form after submission
                })
                .addOnFailureListener(e ->
                        Toast.makeText(DisputeFormActivity.this, "Failed to submit dispute", Toast.LENGTH_SHORT).show());
    }
}
