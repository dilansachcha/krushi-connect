package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView imgLogo = findViewById(R.id.imgLogo);

        Intent deepLinkIntent = getIntent();
        if (deepLinkIntent != null && deepLinkIntent.getData() != null) {
            String deepLink = deepLinkIntent.getData().toString();
            if (deepLink.contains("krushiconnect.page.link/PZXe")) { //DynamicLink
                Intent adminIntent = new Intent(HomeActivity.this, AdminLoginActivity.class);
                adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(adminIntent);
                finish();
                return;
            }
        }


        SharedPreferences adminPrefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        boolean isAdminLoggedIn = adminPrefs.getBoolean("isAdminLoggedIn", false);

        if (isAdminLoggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
            return;
        }

        SharedPreferences customerPrefs = getSharedPreferences("CustomerPrefs", MODE_PRIVATE);
        boolean isCustomerLoggedIn = customerPrefs.getBoolean("isCustomerLoggedIn", false);

        if (isCustomerLoggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {

            startActivity(new Intent(this, CustomerHomeActivity.class));
            finish();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("FarmerPrefs", MODE_PRIVATE);
        boolean isFarmerLoggedIn = sharedPreferences.getBoolean("isFarmerLoggedIn", false);

        if (isFarmerLoggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && "farmer".equals(documentSnapshot.getString("role"))) {
                            startActivity(new Intent(this, FarmerDashboardActivity.class));
                            finish();
                        }
                    });
        }

        setContentView(R.layout.activity_home);

        LinearLayout footer = findViewById(R.id.footer);
        if (isCustomerLoggedIn) {
            footer.setVisibility(View.GONE);
        }

        LinearLayout btnFruits = findViewById(R.id.category_fruits);
        LinearLayout btnVegetables = findViewById(R.id.category_vegetables);
        LinearLayout btnGrains = findViewById(R.id.category_grains);
        LinearLayout btnAnimalProducts = findViewById(R.id.category_animal_products);

        btnFruits.setOnClickListener(v -> openProductsPage("Fruits"));
        btnVegetables.setOnClickListener(v -> openProductsPage("Vegetables"));
        btnGrains.setOnClickListener(v -> openProductsPage("Grains"));
        btnAnimalProducts.setOnClickListener(v -> openProductsPage("Animal Products"));

        LinearLayout farmerSignIn = findViewById(R.id.farmer_signin);
        LinearLayout customerSignIn = findViewById(R.id.customer_signin);

        farmerSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FarmerSignUpActivity.class);
            startActivity(intent);
        });

        customerSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CustomerSignUpActivity.class);
            startActivity(intent);
        });


        LinearLayout settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

    }

    private void openProductsPage(String category) {
        Intent intent = new Intent(this, ProductsActivity.class);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }
}
