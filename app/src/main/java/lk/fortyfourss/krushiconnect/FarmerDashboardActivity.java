package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FarmerDashboardActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "FarmerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isFarmerLoggedIn";
    private Button btnLogout;
    private Spinner spinnerOptions, spinnerCategory;
    private RecyclerView rvProducts;
    private CardView addProductLayout;
    private TextView tvWelcome;
    private EditText etProductName, etProductDescription, etProductPrice, etStockKg;
    private ImageView ivImage1, ivImage2, ivImage3;
    private Button btnUploadProduct;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private List<Product> productList;
    private ProductAdapter productAdapter;

    private String farmerId;
    private String farmerLocation;
    private String selectedCategory;

    private Uri imageUri1, imageUri2, imageUri3;
    private static final int PICK_IMAGE_REQUEST = 1;
    private int imageSelectionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore.setLoggingEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        farmerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        //spinnerOptions = findViewById(R.id.spinnerOptions);
        Button btnMyAddedProducts = findViewById(R.id.btnMyAddedProducts);
        Button btnMyOrderedProducts = findViewById(R.id.btnMyOrderedProducts);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rvProducts = findViewById(R.id.rvProducts);
        addProductLayout = findViewById(R.id.addProductLayout);
        tvWelcome = findViewById(R.id.tvWelcome);
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etProductPrice = findViewById(R.id.etProductPrice);
        etStockKg = findViewById(R.id.etStockKg);
        ivImage1 = findViewById(R.id.ivImage1);
        ivImage2 = findViewById(R.id.ivImage2);
        ivImage3 = findViewById(R.id.ivImage3);
        btnUploadProduct = findViewById(R.id.btnUploadProduct);
        btnLogout = findViewById(R.id.btnLogout);

        btnMyAddedProducts.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, FarmerProductsActivity.class);
            startActivity(intent);
        });


        btnMyOrderedProducts.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerDashboardActivity.this, FarmerOrderedProductsActivity.class);
            startActivity(intent);
        });



        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);

        fetchFarmerDetails();
        //setupSpinner();
        setupCategorySpinner();

        ivImage1.setOnClickListener(v -> selectImage(1));
        ivImage2.setOnClickListener(v -> selectImage(2));
        ivImage3.setOnClickListener(v -> selectImage(3));

        btnUploadProduct.setOnClickListener(v -> uploadProduct());

        btnLogout.setOnClickListener(v -> logoutFarmer());

        //  FCM thygnnw
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();

                    if (farmerId != null) { // Ensure farmerId is not null before updating Firestore
                        db.collection("users").document(farmerId)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated in Firestore"))
                                .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
                    }
                });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(FarmerDashboardActivity.this)
                        .setTitle("Logout Confirmation")
                        .setMessage("Do you want to logout as a Farmer?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            logoutFarmer();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
            }
        });

        addProductLayout.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);

    }

    private void fetchFarmerDetails() {
        if (farmerId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(farmerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String farmerName = documentSnapshot.getString("username");
                        tvWelcome.setText("Welcome, " + (farmerName != null ? farmerName : "Farmer"));

                        if (documentSnapshot.contains("address")) {
                            Map<String, Object> address = (Map<String, Object>) documentSnapshot.get("address");
                            if (address != null && address.containsKey("city")) {
                                farmerLocation = (String) address.get("city");
                                Log.d("Firestore", "Farmer Location: " + farmerLocation);
                            } else {
                                farmerLocation = "Unknown";
                                Log.e("Firestore", "City not found in address");
                            }
                        } else {
                            farmerLocation = "Unknown";
                            Log.e("Firestore", "Address not found in user document");
                        }

                    } else {
                        Toast.makeText(this, "Farmer details not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching farmer details", e));
    }


    private void selectImage(int index) {
        imageSelectionIndex = index;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (imageSelectionIndex == 1) {
                imageUri1 = selectedImageUri;
                ivImage1.setImageURI(imageUri1);
            } else if (imageSelectionIndex == 2) {
                imageUri2 = selectedImageUri;
                ivImage2.setImageURI(imageUri2);
            } else if (imageSelectionIndex == 3) {
                imageUri3 = selectedImageUri;
                ivImage3.setImageURI(imageUri3);
            }
        }
    }

    private void logoutFarmer() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();

        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(FarmerDashboardActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    public static void saveFarmerLoginState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        db.collection("categories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categories.clear();
                    for (var doc : queryDocumentSnapshots) {
                        String categoryName = doc.getString("name");
                        if (categoryName != null) {
                            categories.add(categoryName);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();

                    if (!categories.isEmpty()) {
                        spinnerCategory.setSelection(0);
                        selectedCategory = categories.get(0);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading categories", e));

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void uploadProduct() {
        Log.d("UploadProduct", "Starting product upload process...");

        String name = etProductName.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String stockKgStr = etStockKg.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || stockKgStr.isEmpty()) {
            Log.e("UploadProduct", "Validation failed: Missing fields");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri1 == null || imageUri2 == null || imageUri3 == null) {
            Log.e("UploadProduct", "Validation failed: Not all images selected");
            Toast.makeText(this, "Please select all 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePerKg = Double.parseDouble(priceStr);
        double stockKg = Double.parseDouble(stockKgStr);

        List<Uri> imageUris = List.of(imageUri1, imageUri2, imageUri3);
        List<String> imageUrls = new ArrayList<>();

        Log.d("UploadProduct", "Uploading images to Firebase Storage...");

        for (Uri uri : imageUris) {
            StorageReference fileRef = storageReference.child("products/" + UUID.randomUUID().toString());
            fileRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uriResult -> {
                            imageUrls.add(uriResult.toString());
                            Log.d("UploadProduct", "Image uploaded: " + uriResult.toString());

                            if (imageUrls.size() == 3) {
                                Log.d("UploadProduct", "All images uploaded, proceeding to fetch category reference...");
                                fetchCategoryAndSaveProduct(name, description, pricePerKg, stockKg, imageUrls);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("UploadProduct", "Image upload failed", e);
                        Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void fetchCategoryAndSaveProduct(String name, String description, double pricePerKg, double stockKg, List<String> imageUrls) {
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Log.e("UploadProduct", "Category selection failed: No category selected");
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UploadProduct", "Fetching category document for: " + selectedCategory);

        db.collection("categories")
                .whereEqualTo("name", selectedCategory)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String categoryId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d("UploadProduct", "Category found: " + categoryId);
                        saveProductToFirestore(name, description, pricePerKg, stockKg, imageUrls, categoryId);
                    } else {
                        Log.e("UploadProduct", "Category not found in Firestore");
                        Toast.makeText(this, "Category not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("UploadProduct", "Error fetching category", e));
    }

    private void saveProductToFirestore(String name, String description, double pricePerKg, double stockKg, List<String> imageUrls, String categoryId) {
        Log.d("UploadProduct", "Saving product to Firestore...");

        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", description);
        product.put("pricePerKg", pricePerKg);
        product.put("stockKg", stockKg);
        product.put("status", "Pending Approval");
        product.put("location", farmerLocation != null ? farmerLocation : "Unknown");
        product.put("farmerId", db.document("users/" + farmerId));
        product.put("imageUrls", imageUrls);
        product.put("category", db.document("categories/" + categoryId));
        product.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("products").add(product)
                .addOnSuccessListener(documentReference -> {
                    Log.d("UploadProduct", "Product successfully added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Product uploaded successfully!", Toast.LENGTH_SHORT).show();
                    clearAddProductForm();
                    refreshActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("UploadProduct", "Error adding product to Firestore", e);
                    Toast.makeText(this, "Product upload failed!", Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private void clearAddProductForm() {
        etProductName.setText("");
        etProductDescription.setText("");
        etProductPrice.setText("");
        etStockKg.setText("");
        ivImage1.setImageResource(android.R.color.transparent);
        ivImage2.setImageResource(android.R.color.transparent);
        ivImage3.setImageResource(android.R.color.transparent);
        imageUri1 = imageUri2 = imageUri3 = null;
    }

}
