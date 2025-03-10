package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class CustomerHomeActivity extends AppCompatActivity {

    private TextView tvWelcomeCustomer;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "CustomerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isCustomerLoggedIn";
    private Button btnLogout;
    private LinearLayout btnProfile, btnCart, btnOrders;
    private LinearLayout btnFruits, btnVegetables, btnGrains, btnAnimalProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        tvWelcomeCustomer = findViewById(R.id.tvWelcomeCustomer);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        btnCart = findViewById(R.id.btnCart);
        btnOrders = findViewById(R.id.btnOrders);

        btnFruits = findViewById(R.id.category_fruits);
        btnVegetables = findViewById(R.id.category_vegetables);
        btnGrains = findViewById(R.id.category_grains);
        btnAnimalProducts = findViewById(R.id.category_animal_products);

        btnFruits.setOnClickListener(v -> openProductsPage("Fruits"));
        btnVegetables.setOnClickListener(v -> openProductsPage("Vegetables"));
        btnGrains.setOnClickListener(v -> openProductsPage("Grains"));
        btnAnimalProducts.setOnClickListener(v -> openProductsPage("Animal Products"));

        fetchCustomerDetails();

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        tvWelcomeCustomer.setText("Welcome, " + (username != null ? username : "Customer"));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();

                    db.collection("users").document(userId)
                            .update("fcmToken", token)
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated in Firestore"))
                            .addOnFailureListener(e -> Log.e("FCM", "Failed to update token", e));
                });

        btnLogout.setOnClickListener(v -> logoutCustomer());

        btnProfile.setOnClickListener(v -> startActivity(new Intent(CustomerHomeActivity.this, CustomerProfileActivity.class)));

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        btnOrders.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerHomeActivity.this, CustomerOrderActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutCustomer());
    }

    private void fetchCustomerDetails() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String customerName = documentSnapshot.getString("username");
                        tvWelcomeCustomer.setText("Welcome, " + (customerName != null ? customerName : "Customer"));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
    }

    private void openProductsPage(String category) {
        Intent intent = new Intent(this, ProductsActivity.class);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }

    private void logoutCustomer() {
        SharedPreferences.Editor editor = getSharedPreferences("CustomerPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isCustomerLoggedIn", false);
        editor.apply();

        auth.signOut();

        startActivity(new Intent(CustomerHomeActivity.this, HomeActivity.class));
        finish();
    }
}

