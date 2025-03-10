package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderProductsAdminAdapter extends RecyclerView.Adapter<OrderProductsAdminAdapter.ViewHolder> {
    private Context context;
    private List<OrderAdminItemModel> productsList;

    public OrderProductsAdminAdapter(Context context, List<OrderAdminItemModel> productsList) {
        this.context = context;
        this.productsList = productsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderAdminItemModel product = productsList.get(position);

        holder.tvProductName.setText("Product: " + product.getName());
        holder.tvFarmerName.setText("Farmer: " + product.getFarmerName());
        holder.tvProductQuantity.setText("Quantity: " + product.getBoughtQuantity() + " kg");
        holder.tvProductPrice.setText("Price: Rs. " + product.getPrice());
        holder.tvFarmerMobile.setText("Mobile: " + product.getFarmerMobile());
        holder.tvFarmerCity.setText("City: " + product.getFarmerCity());

        holder.btnCallFarmer.setOnClickListener(v -> {
            if (product.getFarmerMobile() != null && !product.getFarmerMobile().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + product.getFarmerMobile()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Farmer phone number unavailable", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvFarmerName, tvProductQuantity, tvProductPrice, tvFarmerMobile, tvFarmerCity;
        Button btnCallFarmer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvFarmerMobile = itemView.findViewById(R.id.tvFarmerMobile);
            tvFarmerCity = itemView.findViewById(R.id.tvFarmerCity);
            btnCallFarmer = itemView.findViewById(R.id.btnCallFarmer);
        }
    }

}
