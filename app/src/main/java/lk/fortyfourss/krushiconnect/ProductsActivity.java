package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private FirebaseFirestore db;
    private List<Product> productList;
    private ProgressBar progressBar;
    private TextView tvNoProducts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvNoProducts = findViewById(R.id.tvNoProducts);
        EditText searchBar = findViewById(R.id.search_bar);
        ImageView filterIcon = findViewById(R.id.filter_icon);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();
        selectedCategory = getIntent().getStringExtra("CATEGORY");

        fetchProducts();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            searchBar.setText("");
            spinnerFilter.setSelection(0, true);
            swipeRefreshLayout.setRefreshing(false);
            recreate();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                this, R.array.filter_options, android.R.layout.simple_spinner_dropdown_item
        );
        spinnerFilter.setAdapter(filterAdapter);

        filterIcon.setOnClickListener(v -> {
            spinnerFilter.setVisibility(View.VISIBLE);

            if (!searchBar.getText().toString().isEmpty()) {
                searchBar.setText("");
            }
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                applyFilter(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        View locationSection = findViewById(R.id.location_section);
        locationSection.setOnClickListener(v -> openProductLocationsInMaps());

    }


    private void fetchProducts() {
        progressBar.setVisibility(View.VISIBLE);
        productList.clear();

        db.collection("products")
                .whereEqualTo("status", "Approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        tvNoProducts.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<Product> tempList = new ArrayList<>();
                    int totalProducts = queryDocumentSnapshots.size();
                    final int[] processedProducts = {0};

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());

                        DocumentReference categoryRef = product.getCategory();
                        DocumentReference farmerRef = product.getFarmerId();

                        if (categoryRef != null) {
                            categoryRef.get().addOnSuccessListener(categoryDoc -> {
                                if (categoryDoc.exists()) {
                                    String categoryName = categoryDoc.getString("name");
                                    if (categoryName != null && categoryName.equalsIgnoreCase(selectedCategory)) {
                                        tempList.add(product);
                                    }
                                }
                                checkAndUpdateProducts(tempList, totalProducts, processedProducts);
                            });
                        } else {
                            checkAndUpdateProducts(tempList, totalProducts, processedProducts);
                        }

                        if (farmerRef != null) {
                            farmerRef.get().addOnSuccessListener(farmerDoc -> {
                                if (farmerDoc.exists()) {
                                    String farmerName = farmerDoc.getString("username");
                                    product.setFarmerName(farmerName);
                                }
                                checkAndUpdateProducts(tempList, totalProducts, processedProducts);
                            });
                        } else {
                            checkAndUpdateProducts(tempList, totalProducts, processedProducts);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching products", e);
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void checkAndUpdateProducts(List<Product> tempList, int totalProducts, int[] processedProducts) {//products load - UIt klin
        processedProducts[0]++;
        if (processedProducts[0] >= totalProducts) {
            updateProductList(tempList);
        }
    }

    private void updateProductList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        productAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        tvNoProducts.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
    }


    private void openProductLocationsInMaps() {
        if (productList.isEmpty()) {
            Toast.makeText(this, "No products available to display on map!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> uniqueLocations = new ArrayList<>();
        for (Product product : productList) {
            if (product.getLocation() != null && !product.getLocation().isEmpty() && !uniqueLocations.contains(product.getLocation())) {
                uniqueLocations.add(product.getLocation());
            }
        }

        if (uniqueLocations.isEmpty()) {
            Toast.makeText(this, "No valid locations found!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder mapQuery = new StringBuilder("geo:0,0?q=");
        for (int i = 0; i < uniqueLocations.size(); i++) {
            mapQuery.append(uniqueLocations.get(i));
            if (i < uniqueLocations.size() - 1) {
                mapQuery.append("|");
            }
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(mapQuery.toString()));
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps app is not installed!", Toast.LENGTH_LONG).show();
        }
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) {
            fetchProducts();
            return;
        }

        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            String productName = product.getName().toLowerCase();
            String productLocation = product.getLocation() != null ? product.getLocation().toLowerCase() : "";
            String farmerName = product.getFarmerName() != null ? product.getFarmerName().toLowerCase() : "";

            if (productName.contains(query.toLowerCase()) ||
                    productLocation.contains(query.toLowerCase()) ||
                    farmerName.contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        productAdapter.updateList(filteredList);
    }


    private void applyFilter(String filterType) {
        if (productList == null || productList.isEmpty()) return;

        List<Product> sortedList = new ArrayList<>(productList);

        switch (filterType) {
            case "Newest":
                // Sort by createdAt (Newest First)
                Collections.sort(sortedList, (p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                break;
            case "Price Low to High":
                // Sort by price (Ascending)
                Collections.sort(sortedList, Comparator.comparingDouble(Product::getPricePerKg));
                break;
            case "Price High to Low":
                // Sort by price (Descending)
                Collections.sort(sortedList, (p1, p2) -> Double.compare(p2.getPricePerKg(), p1.getPricePerKg()));
                break;
        }

        productAdapter.updateList(sortedList);
    }


}
