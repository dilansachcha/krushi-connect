package lk.fortyfourss.krushiconnect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private TextView tvCartTitle, tvGrandTotal, tvDeliveryFee;
    private Spinner spinnerDeliveryOptions;
    private Button btnCheckout;
    private String userId;
    private double totalAmount;
    private boolean isDeliverySelected = false;

    static final int PAYHERE_REQUEST = 11001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "You must be logged in to view the cart!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvCartTitle = findViewById(R.id.tvCartTitle);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        btnCheckout = findViewById(R.id.btnCheckout);

        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItemList, this);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems(userId);

        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        spinnerDeliveryOptions = findViewById(R.id.spinnerDeliveryOptions);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Pick Up", "Get it Delivered"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeliveryOptions.setAdapter(adapter);

        spinnerDeliveryOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();

                isDeliverySelected = selectedOption.equals("Get it Delivered");
                updateGrandTotal(); // Recalculate
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnCheckout.setOnClickListener(v -> initiatePayment());
    }

    private void initiatePayment() {
        if (cartItemList == null || cartItemList.isEmpty()) {
            Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        InitRequest req = new InitRequest();
        req.setMerchantId("1229619");
        req.setCurrency("LKR");
        req.setAmount(totalAmount);
        req.setOrderId("230000123");
        req.setItemsDescription("Krushi Connect Payment");
        req.getCustomer().setFirstName("Saman");
        req.getCustomer().setLastName("Perera");
        req.getCustomer().setEmail("samanp@gmail.com");
        req.getCustomer().setPhone("+94771234567");
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK && response != null && response.isSuccess()) {
                Log.i("Krushi", "Payment Success: " + response.getData().toString());

                processOrder();
            } else {
                Log.i("Krushi", "Payment Failed or Canceled");
                Toast.makeText(this, "Payment Failed or Canceled! Try Again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void updateStockAndClearCart(DocumentReference orderRef) {
        for (CartItem item : cartItemList) {
            String productId = item.getProductId();
            double purchasedQuantity = item.getSelectedQuantity();

            db.collection("products").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double currentStock = documentSnapshot.getDouble("stockKg");
                            if (currentStock != null && currentStock >= purchasedQuantity) {
                                double updatedStock = Math.round((currentStock - purchasedQuantity) * 100.0) / 100.0;

                                Map<String, Object> stockUpdate = new HashMap<>();
                                stockUpdate.put("stockKg", updatedStock);

                                if (updatedStock == 0) {
                                    stockUpdate.put("isAvailable", false);
                                }

                                db.collection("products").document(productId).update(stockUpdate);
                            }
                        }
                    });

            // Remove Product from Cart
            db.collection("cart").document(userId).collection("items").document(productId)
                    .delete();
        }

        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CartActivity.this, OrderConfirmationActivity.class);
        intent.putExtra("ORDER_ID", orderRef.getId());
        startActivity(intent);
        finish();
    }



    public void loadCartItems(String userId) {
        db.collection("cart").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList.clear();
                    totalAmount = 0.0;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        CartItem item = document.toObject(CartItem.class);
                        if (item != null) {
                            cartItemList.add(item);
                            totalAmount += item.getTotalPrice();
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateGrandTotal();
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Error loading cart items", e));
    }

    public void updateGrandTotalAfterRemoval(double removedPrice) {
        totalAmount -= removedPrice;
        if (totalAmount < 0) totalAmount = 0.0;
        updateGrandTotal();
        updateCartBadge();
    }

    private void updateGrandTotal() {
        double total = 0.0;

        for (CartItem item : cartItemList) {
            total += item.getTotalPrice();
        }

        // delivery fee
        double deliveryFee = isDeliverySelected ? cartItemList.size() * 200.0 : 0.0;

        totalAmount = total + deliveryFee;

        tvGrandTotal.setText(String.format("Grand Total: Rs. %.2f", totalAmount));

        if (isDeliverySelected) {
            tvDeliveryFee.setVisibility(View.VISIBLE);
            tvDeliveryFee.setText(String.format("Delivery Fee: Rs. %.2f", deliveryFee));
        } else {
            tvDeliveryFee.setVisibility(View.GONE);
        }

        tvCartTitle.setText("Your Cart (" + cartItemList.size() + " items)");
    }



    public void updateCartBadge() {
        db.collection("cart").document(userId).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Intent intent = new Intent("UPDATE_CART_BADGE");
                    intent.putExtra("cart_count", count);
                    sendBroadcast(intent);
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Error updating cart badge", e));
    }

    private void processOrder() {
        double deliveryFee = isDeliverySelected ? cartItemList.size() * 200.0 : 0.0;

        // Prepare Order Data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("totalAmount", totalAmount);
        orderData.put("deliveryFee", deliveryFee);
        orderData.put("status", "Paid");
        orderData.put("timestamp", System.currentTimeMillis());
        orderData.put("delivery_status", isDeliverySelected ? "Delivery" : "Pickup");

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(orderRef -> {
                    saveOrderItems(orderRef);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Order Failed", Toast.LENGTH_SHORT).show());
    }


    private void saveOrderItems(DocumentReference orderRef) {
        for (CartItem item : cartItemList) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", item.getProductName());
            productData.put("boughtQuantity", item.getSelectedQuantity());
            productData.put("price", item.getTotalPrice());
            productData.put("farmerName", item.getFarmerName());
            productData.put("farmerMobile", item.getFarmerMobile());
            productData.put("farmerCity", item.getFarmerCity());
            productData.put("delivery_status", isDeliverySelected ? "Delivery" : "Pickup");

            orderRef.collection("items").add(productData)
                    .addOnFailureListener(e ->
                            Log.e("OrderItems", "Failed to save order items", e));
        }

        updateStockAndClearCart(orderRef);
    }




}

