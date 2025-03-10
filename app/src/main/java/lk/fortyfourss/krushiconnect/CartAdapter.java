package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItemList;
    private Context context;
    private FirebaseFirestore db;
    private CartActivity cartActivity;

    public CartAdapter(Context context, List<CartItem> cartItemList, CartActivity cartActivity) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.cartActivity = cartActivity;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.ivProductImage);

        holder.tvProductName.setText(item.getProductName());
        holder.tvFarmerName.setText("" + item.getFarmerName());
        holder.tvPricePerKg.setText("Price: Rs. " + String.format("%.2f", item.getPricePerKg()) + "/kg");
        holder.tvSelectedQuantity.setText("Quantity: " + item.getSelectedQuantity() + " kg");
        holder.tvTotalPrice.setText("Total: Rs. " + String.format("%.2f", item.getTotalPrice()));

        holder.btnDelete.setOnClickListener(v -> removeItem(position, item));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvFarmerName, tvPricePerKg, tvSelectedQuantity, tvTotalPrice;
        Button btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvPricePerKg = itemView.findViewById(R.id.tvPricePerKg);
            tvSelectedQuantity = itemView.findViewById(R.id.tvSelectedQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public void removeItem(int position, CartItem item) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String productId = item.getProductId();

        db.collection("cart").document(userId).collection("items").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    double removedPrice = item.getTotalPrice();
                    cartItemList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItemList.size());

                    cartActivity.updateGrandTotalAfterRemoval(removedPrice);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show());
    }
}
