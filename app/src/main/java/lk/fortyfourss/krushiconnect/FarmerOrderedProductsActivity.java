package lk.fortyfourss.krushiconnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FarmerOrderedProductsActivity extends AppCompatActivity {

    private RecyclerView rvOrderedProducts;
    private FarmerOrderedProductsAdapter orderedProductsAdapter;
    private List<OrderedProduct> orderedProductList;
    private FirebaseFirestore db;
    private String farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_ordered_products);

        db = FirebaseFirestore.getInstance();
        farmerId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        rvOrderedProducts = findViewById(R.id.rvOrderedProducts);
        rvOrderedProducts.setLayoutManager(new LinearLayoutManager(this));

        orderedProductList = new ArrayList<>();
        orderedProductsAdapter = new FarmerOrderedProductsAdapter(this, orderedProductList);
        rvOrderedProducts.setAdapter(orderedProductsAdapter);

        loadOrderedProducts();
    }

    private void loadOrderedProducts() {
        if (farmerId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("orders")
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    orderedProductList.clear();

                    for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                        CollectionReference itemsCollection = orderDoc.getReference().collection("items");

                        itemsCollection.get().addOnSuccessListener(itemsSnapshot -> {
                            for (QueryDocumentSnapshot itemDoc : itemsSnapshot) {
                                String farmerIdFromDB = itemDoc.getString("farmerId");

                                if (farmerIdFromDB != null && farmerIdFromDB.equals(farmerId)) {

                                    String name = itemDoc.getString("name");
                                    double boughtQuantity = itemDoc.getDouble("boughtQuantity");
                                    String farmerName = itemDoc.getString("farmerName");
                                    String farmerMobile = itemDoc.getString("farmerMobile");
                                    String imageUrl = itemDoc.getString("imageUrl");

                                    Log.d("Firestore", "Fetched Order: " + name + " | " + boughtQuantity + " | " + farmerMobile);

                                    OrderedProduct orderedProduct = new OrderedProduct(name, boughtQuantity, farmerName, farmerMobile, imageUrl);
                                    orderedProductList.add(orderedProduct);
                                }
                            }
                            orderedProductsAdapter.notifyDataSetChanged();
                        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching order items", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching orders", e));
    }
}
