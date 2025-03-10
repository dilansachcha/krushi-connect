package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FarmerOrderedProductsAdapter extends RecyclerView.Adapter<FarmerOrderedProductsAdapter.OrderedProductViewHolder> {

    private Context context;
    private List<OrderedProduct> orderedProductList;

    public FarmerOrderedProductsAdapter(Context context, List<OrderedProduct> orderedProductList) {
        this.context = context;
        this.orderedProductList = orderedProductList;
    }

    @NonNull
    @Override
    public OrderedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_farmer_ordered_product, parent, false);
        return new OrderedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderedProductViewHolder holder, int position) {
        OrderedProduct orderedProduct = orderedProductList.get(position);

        holder.tvProductName.setText(orderedProduct.getName());
        holder.tvBoughtQuantity.setText("Quantity: " + orderedProduct.getBoughtQuantity() + " Kg");
        holder.tvCustomerName.setText("Ordered By: " + orderedProduct.getFarmerName());
        holder.tvCustomerPhone.setText("Phone: " + orderedProduct.getFarmerMobile());

        Glide.with(context)
                .load(orderedProduct.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.ivProductImage);

        holder.btnCallCustomer.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + orderedProduct.getFarmerMobile()));
            context.startActivity(callIntent);
        });
    }

    @Override
    public int getItemCount() {
        return orderedProductList.size();
    }

    public static class OrderedProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvBoughtQuantity, tvCustomerName, tvCustomerPhone;
        ImageView ivProductImage;
        Button btnCallCustomer;

        public OrderedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBoughtQuantity = itemView.findViewById(R.id.tvBoughtQuantity);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnCallCustomer = itemView.findViewById(R.id.btnCallCustomer);
        }
    }
}
