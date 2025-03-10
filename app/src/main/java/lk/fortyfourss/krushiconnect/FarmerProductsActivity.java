package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FarmerProductsActivity extends AppCompatActivity {
    private RecyclerView rvFarmerProducts;
    private FarmerProductAdapter farmerProductAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private String farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_products);

        db = FirebaseFirestore.getInstance();
        farmerId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        rvFarmerProducts = findViewById(R.id.rvFarmerProducts);
        rvFarmerProducts.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        farmerProductAdapter = new FarmerProductAdapter(this, productList);
        rvFarmerProducts.setAdapter(farmerProductAdapter);

        loadAddedProducts();
    }

    private void loadAddedProducts() {
        if (farmerId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("products")
                .whereEqualTo("farmerId", db.document("users/" + farmerId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear(); //prna ewa clear

                    for (var document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        productList.add(product);
                    }

                    farmerProductAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching farmer's products", e));
    }

}
