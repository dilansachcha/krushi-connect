package lk.fortyfourss.krushiconnect;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageCarouselAdapter carouselAdapter;
    private TextView tvProductName, tvProductDescription, tvProductPrice, tvStock, tvFarmerName, tvFarmerLocation;
    private Button btnIncrease, btnDecrease;
    private Button btnAddToCart, btnAddToFavorites;
    private double stock = 1.0;
    private double selectedQuantity = 0.1;

    private String productId, farmerId;
    private FirebaseFirestore db;
    private Handler handler;
    private Runnable autoScrollRunnable;

    private SharedPreferences customerPrefs;
    private static final String PREFS_NAME = "CustomerPrefs";
    private static final String KEY_IS_LOGGED_IN = "isCustomerLoggedIn";

    private TextView tvSelectedQuantity;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        customerPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.viewPager);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvStock = findViewById(R.id.tvStock);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvFarmerLocation = findViewById(R.id.tvFarmerLocation);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnAddToFavorites = findViewById(R.id.btnAddToFavorites);
        tvSelectedQuantity = findViewById(R.id.tvSelectedQuantity);

        tvSelectedQuantity.setText(String.valueOf(selectedQuantity));

        productId = getIntent().getStringExtra("PRODUCT_ID");
        String name = getIntent().getStringExtra("PRODUCT_NAME");
        String description = getIntent().getStringExtra("PRODUCT_DESCRIPTION");
        double price = getIntent().getDoubleExtra("PRODUCT_PRICE", 0);
        farmerId = getIntent().getStringExtra("FARMER_ID");

        List<String> imageUrls = getIntent().getStringArrayListExtra("PRODUCT_IMAGES");
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = new ArrayList<>();
            imageUrls.add("@drawable/placeholder_image");
        }

        // Set Initial Data (Except Stock)
        tvProductName.setText(name);
        tvProductDescription.setText(description);
        tvProductPrice.setText("Price: Rs. " + price + "/kg (ltr or pcs)");
        tvStock.setText("Stock: Loading...");

        loadStockFromFirestore();

        carouselAdapter = new ImageCarouselAdapter(this, imageUrls);
        viewPager.setAdapter(carouselAdapter);

        autoScrollCarousel();

        fetchFarmerDetails();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateCartBadge();
        }

        btnIncrease.setOnClickListener(v -> {
            if (selectedQuantity + 0.1 <= stock) { //click ekt +0.1i
                selectedQuantity = Math.round((selectedQuantity + 0.1) * 10.0) / 10.0;
                tvSelectedQuantity.setText(String.valueOf(selectedQuantity));
            }
        });

        btnDecrease.setOnClickListener(v -> {
            if (selectedQuantity > 0.1) { //
                selectedQuantity = Math.round((selectedQuantity - 0.1) * 10.0) / 10.0;
                tvSelectedQuantity.setText(String.valueOf(selectedQuantity));
            }
        });

        // Add to Cart
        btnAddToCart.setOnClickListener(v -> checkUserBeforeAddingToCart());

        if (getIntent().getDoubleExtra("PRODUCT_STOCK", 0) <= 0) {
            btnAddToCart.setVisibility(View.GONE);
            tvStock.setText("Out of Stock");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Add to Favorites
        btnAddToFavorites.setOnClickListener(v -> {
            Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkUserBeforeAddingToCart() {
        boolean isCustomerLoggedIn = customerPrefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isCustomerLoggedIn) {
            addToCart();
        } else {
            showLoginDialog();
        }
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login Required")
                .setMessage("You need to log in as a customer to add products to the cart.")
                .setPositiveButton("Login as Customer", (dialog, which) -> {
                    Intent intent = new Intent(ProductDetailActivity.this, CustomerSignInActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void addToCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference cartRef = db.collection("cart").document(userId).collection("items").document(productId);

        db.collection("users").document(farmerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String farmerMobile = documentSnapshot.getString("mobile");
                String farmerCity = "";

                if (documentSnapshot.contains("address")) {
                    Map<String, Object> address = (Map<String, Object>) documentSnapshot.get("address");
                    if (address != null && address.containsKey("city")) {
                        farmerCity = address.get("city").toString();
                    }
                }

                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("productId", productId);
                cartItem.put("productName", tvProductName.getText().toString());
                cartItem.put("farmerId", farmerId);
                cartItem.put("farmerName", tvFarmerName.getText().toString());
                cartItem.put("farmerMobile", farmerMobile);
                cartItem.put("farmerCity", farmerCity);
                cartItem.put("pricePerKg", getIntent().getDoubleExtra("PRODUCT_PRICE", 0));
                cartItem.put("selectedQuantity", selectedQuantity);
                cartItem.put("totalPrice", selectedQuantity * getIntent().getDoubleExtra("PRODUCT_PRICE", 0));
                cartItem.put("productImage", getIntent().getStringArrayListExtra("PRODUCT_IMAGES").get(0));

                cartRef.set(cartItem)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProductDetailActivity.this, "Added to Cart!", Toast.LENGTH_SHORT).show();
                            updateCartBadge();
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProductDetailActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch farmer details", e));
    }



    private void updateCartBadge() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cart").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (toolbar != null) {
                        MenuItem cartItem = toolbar.getMenu().findItem(R.id.action_cart);
                        if (cartItem != null) {
                            cartItem.setIcon(R.drawable.ic_cart_with_badge);
                            if (cartItem.getActionView() != null) {
                                cartItem.getActionView().setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                                TextView badgeText = cartItem.getActionView().findViewById(R.id.cart_badge);
                                badgeText.setText(String.valueOf(count));
                            }
                        }
                    }
                });
    }



    private void loadStockFromFirestore() {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double fetchedStock = documentSnapshot.getDouble("stockKg");
                        if (fetchedStock != null) {
                            stock = fetchedStock;
                            tvStock.setText("Stock: " + stock + " kg (ltr or pcs)");
                        }
                    } else {
                        Log.e("Firestore", "Product not found!");
                        tvStock.setText("Stock: Not Available");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching stock", e);
                    tvStock.setText("Stock: Error Loading");
                });
    }

    // Fetch Farmer Details (Name & Location)
    private void fetchFarmerDetails() {
        db.collection("users").document(farmerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvFarmerName.setText("Farmer: " + documentSnapshot.getString("username"));
                        if (documentSnapshot.contains("address")) {
                            tvFarmerLocation.setText("Location: " + documentSnapshot.get("address.city"));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching farmer details", e));
    }

    private void autoScrollCarousel() {
        handler = new Handler(Looper.getMainLooper());
        autoScrollRunnable = new Runnable() {//worker thread
            @Override
            public void run() {
                int nextItem = viewPager.getCurrentItem() + 1;
                if (nextItem >= carouselAdapter.getItemCount()) {
                    nextItem = 0;
                }
                viewPager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 3000); // 3n 3t
            }
        };
        handler.postDelayed(autoScrollRunnable, 3000);
    }

    private void updateQuantitySelector() {
        tvSelectedQuantity.setText(String.valueOf(selectedQuantity));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_detail_menu, menu);

        MenuItem cartItem = menu.findItem(R.id.action_cart);
        View actionView = cartItem.getActionView();
        TextView cartBadge = actionView.findViewById(R.id.cart_badge);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateCartBadge(cartBadge);
        } else {
            cartBadge.setVisibility(View.GONE);
        }

        actionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        return true;
    }


    private void updateCartBadge(TextView cartBadge) {
        db.collection("cart").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int itemCount = queryDocumentSnapshots.size();
                    if (itemCount > 0) {
                        cartBadge.setVisibility(View.VISIBLE);
                        cartBadge.setText(String.valueOf(itemCount));
                    } else {
                        cartBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_favorite) {
            Toast.makeText(this, "Added to Favorites!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

