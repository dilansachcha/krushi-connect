package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FarmerProductAdapter extends RecyclerView.Adapter<FarmerProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db;

    public FarmerProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText("Price: Rs. " + product.getPricePerKg() + "/Kg");
        holder.etProductStock.setText(String.valueOf(product.getStockKg()));
        holder.tvProductStatus.setText("Status: " + product.getStatus());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrls().get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivProductImage);
        }

        holder.ivEditStock.setOnClickListener(v -> {
            if ("Approved".equals(product.getStatus())) {
                holder.etProductStock.setEnabled(true);
                holder.etProductStock.requestFocus();
                holder.ivEditStock.setVisibility(View.GONE);
                holder.btnUpdateStock.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(context, "Stock can only be updated for Approved products!", Toast.LENGTH_SHORT).show();
            }
        });


        holder.btnUpdateStock.setOnClickListener(v -> {
            String updatedStockStr = holder.etProductStock.getText().toString().replace("Stock: ", "").replace(" Kg", "").trim();

            if (!updatedStockStr.isEmpty()) {
                try {
                    double updatedStock = Double.parseDouble(updatedStockStr);
                    updateStock(product, updatedStock, holder);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid stock value!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Stock value cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateStock(Product product, double newStock, ProductViewHolder holder) {
        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(context, "Error: Product ID is missing!", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Error: Product ID is null or empty");
            return;
        }

        db.collection("products").document(product.getId())
                .update("stockKg", newStock)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Stock updated!", Toast.LENGTH_SHORT).show();
                    holder.etProductStock.setEnabled(false);
                    holder.ivEditStock.setVisibility(View.VISIBLE);
                    holder.btnUpdateStock.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error updating stock", e));
    }



    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductStatus;
        EditText etProductStock;
        ImageView ivProductImage, ivEditStock;
        Button btnUpdateStock;
        CardView cardView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            etProductStock = itemView.findViewById(R.id.etProductStock);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivEditStock = itemView.findViewById(R.id.ivEditStock);
            btnUpdateStock = itemView.findViewById(R.id.btnUpdateStock);
            cardView = itemView.findViewById(R.id.cardViewProduct);
        }
    }
}
