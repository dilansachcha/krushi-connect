package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("Price: Rs. " + product.getPricePerKg() + "/kg (ltr or pcs)");
        holder.tvProductStock.setText("Stock: " + product.getStockKg() + " kg (ltr or pcs)");

        if (product.getStockKg() <= 0) {
            holder.tvProductStock.setText("Out of Stock");
            holder.tvProductStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setClickable(false);
        } else {
            holder.tvProductStock.setText("Stock: " + product.getStockKg() + " kg (ltr or pcs)");
            holder.itemView.setAlpha(1f);
            holder.itemView.setClickable(true);
        }

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivProductImage);
        }

        if (product.getFarmerId() instanceof DocumentReference) {
            ((DocumentReference) product.getFarmerId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String farmerName = documentSnapshot.getString("username");

                            String city = "Unknown";
                            if (documentSnapshot.contains("address")) {
                                Map<String, Object> address = (Map<String, Object>) documentSnapshot.get("address");
                                if (address != null && address.containsKey("city")) {
                                    city = (String) address.get("city");
                                }
                            }

                            holder.tvFarmer.setText("Farmer: " + (farmerName != null ? farmerName : "Unknown"));
                            holder.tvLocation.setText("Location: " + city);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching farmer details", e));
        }

        holder.itemView.setOnClickListener(v -> {
            if (product.getStockKg() > 0) {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
                intent.putExtra("PRODUCT_PRICE", product.getPricePerKg());
                intent.putExtra("PRODUCT_STOCK", product.getStockKg());
                intent.putExtra("FARMER_ID", product.getFarmerId().getId());
                intent.putStringArrayListExtra("PRODUCT_IMAGES", new ArrayList<>(product.getImageUrls()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "This product is out of stock!", Toast.LENGTH_SHORT).show();
            }
        });



    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }


    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductStock, tvFarmer, tvLocation;
        ImageView ivProductImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            tvFarmer = itemView.findViewById(R.id.tvFarmer);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
        }
    }
}
