package lk.fortyfourss.krushiconnect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductAdapterAdmin productAdapter;
    private FirebaseFirestore db;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        rvProducts = findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        productAdapter = new ProductAdapterAdmin(this, productList);
        rvProducts.setAdapter(productAdapter);

        fetchProducts();
    }

    private void fetchProducts() {
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            productList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Product product = document.toObject(Product.class);
                product.setId(document.getId());

                if (product.getCategory() != null) {
                    product.getCategory().get()
                            .addOnSuccessListener(categoryDoc -> {
                                if (categoryDoc.exists()) {
                                    String categoryName = categoryDoc.getString("name");
                                    Log.d("ManageProducts", "Category Name: " + categoryName);
                                    productList.add(product);
                                    productAdapter.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("ManageProducts", "Error fetching category", e));
                } else {
                    productList.add(product);
                    productAdapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("ManageProducts", "Error fetching products", e);
            Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
        });
    }


}
