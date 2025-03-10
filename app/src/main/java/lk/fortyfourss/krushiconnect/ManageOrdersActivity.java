package lk.fortyfourss.krushiconnect;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private ManageOrdersAdapter ordersAdapter;
    private List<OrderAdminModel> ordersList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        ordersList = new ArrayList<>();
        ordersAdapter = new ManageOrdersAdapter(this, ordersList);
        rvOrders.setAdapter(ordersAdapter);

        loadOrders();
    }

    public void loadOrders() {
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            ordersList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                OrderAdminModel order = document.toObject(OrderAdminModel.class);
                order.setOrderId(document.getId());

                order.setStatus(document.contains("status") ? document.getString("status") : "Unknown");
                order.setDeliveryStatus(document.contains("delivery_status") ? document.getString("delivery_status") : "Not Available");

                String customerId = document.getString("userId");
                db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        order.setCustomerName(userDoc.getString("username"));
                        order.setCustomerMobile(userDoc.getString("mobile"));
                        order.setCustomerAddress(userDoc.getString("address.city"));
                    }

                    db.collection("orders").document(order.getOrderId()).collection("items")
                            .get().addOnSuccessListener(itemSnapshots -> {
                                List<OrderAdminItemModel> items = new ArrayList<>();
                                for (QueryDocumentSnapshot itemDoc : itemSnapshots) {
                                    items.add(itemDoc.toObject(OrderAdminItemModel.class));
                                }
                                order.setItems(items);
                                ordersList.add(order);
                                ordersAdapter.notifyDataSetChanged();
                            });
                });
            }
        }).addOnFailureListener(e -> Log.e("ManageOrders", "Error loading orders", e));
    }
}
