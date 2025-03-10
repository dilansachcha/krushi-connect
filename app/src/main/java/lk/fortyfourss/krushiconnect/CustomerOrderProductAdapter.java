package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CustomerOrderProductAdapter extends RecyclerView.Adapter<CustomerOrderProductAdapter.ProductViewHolder> {

    private Context context;
    private List<OrderItem> productList;
    private String deliveryStatus;

    public CustomerOrderProductAdapter(Context context, List<OrderItem> productList, String deliveryStatus) {
        this.context = context;
        this.productList = productList;
        this.deliveryStatus = deliveryStatus;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        OrderItem product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvBoughtQuantity.setText("Quantity: " + product.getBoughtQuantity() + " kg");
        holder.tvPrice.setText("Price: Rs. " + product.getPrice());
        holder.tvFarmerName.setText("" + product.getFarmerName());
        holder.tvFarmerCity.setText("City: " + product.getFarmerCity());

        if ("Pickup".equals(deliveryStatus)) {
            holder.btnCallFarmer.setVisibility(View.VISIBLE);
            holder.btnCallFarmer.setOnClickListener(v -> {
                String farmerMobile = product.getFarmerMobile();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + farmerMobile));
                context.startActivity(intent);
            });
        } else {
            holder.btnCallFarmer.setVisibility(View.GONE);
        }

        if ("Pickup".equals(deliveryStatus)) {
            holder.btnSeeRoute.setVisibility(View.VISIBLE);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String customerCity = documentSnapshot.getString("address.city");

                            String farmerCity = product.getFarmerCity();

                            holder.btnSeeRoute.setOnClickListener(v -> openRouteInMaps(customerCity, farmerCity));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to load customer city", e));
        } else {
            holder.btnSeeRoute.setVisibility(View.GONE);
        }

    }

    private void openRouteInMaps(String customerCity, String farmerCity) {
        Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + customerCity + "&destination=" + farmerCity + "&travelmode=driving");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Google Maps is not installed!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvBoughtQuantity, tvPrice, tvFarmerName, tvFarmerCity;
        Button btnCallFarmer, btnSeeRoute;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBoughtQuantity = itemView.findViewById(R.id.tvBoughtQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvFarmerCity = itemView.findViewById(R.id.tvFarmerCity);
            btnCallFarmer = itemView.findViewById(R.id.btnCallFarmer);
            btnSeeRoute = itemView.findViewById(R.id.btnSeeRoute);
        }
    }
}
