package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private CustomerOrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private String userId;
    private ProgressBar progressBar;
    private TextView tvNoOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_orders);

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progressBar);
        tvNoOrders = findViewById(R.id.tvNoOrders);

        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new CustomerOrderAdapter(this, orderList);
        recyclerViewOrders.setAdapter(orderAdapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCustomerOrders();
    }

    private void loadCustomerOrders() {
        progressBar.setVisibility(View.VISIBLE);
        CollectionReference ordersRef = db.collection("orders");

        ordersRef.whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(document.getId());

                            if (document.contains("delivery_status")) {
                                order.setDeliveryStatus(document.getString("delivery_status"));
                            } else {
                                order.setDeliveryStatus("Pickup");
                            }


                            fetchOrderItems(order);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CustomerOrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    Log.e("CustomerOrderActivity", "Error loading orders", e);
                });
    }


    private void fetchOrderItems(Order order) {
        db.collection("orders").document(order.getOrderId()).collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrderItem> items = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        OrderItem item = document.toObject(OrderItem.class);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    order.setItems(items);
                    orderList.add(order);
                    orderAdapter.notifyDataSetChanged();


                    progressBar.setVisibility(View.GONE);
                    tvNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("CustomerOrderActivity", "Failed to fetch order items", e);
                });
    }


    public void callFarmer(String farmerMobile) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + farmerMobile));
        startActivity(intent);
    }
}
