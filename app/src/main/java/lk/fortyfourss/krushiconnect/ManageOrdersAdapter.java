package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ManageOrdersAdapter extends RecyclerView.Adapter<ManageOrdersAdapter.ViewHolder> {
    private Context context;
    private List<OrderAdminModel> ordersList;
    private FirebaseFirestore db;

    public ManageOrdersAdapter(Context context, List<OrderAdminModel> ordersList) {
        this.context = context;
        this.ordersList = ordersList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderAdminModel order = ordersList.get(position);

        holder.tvOrderId.setText("Order ID: " + order.getOrderId());
        holder.tvCustomerName.setText("Customer: " + order.getCustomerName());
        holder.tvCustomerMobile.setText("Mobile: " + order.getCustomerMobile());
        holder.tvCustomerAddress.setText("City: " + order.getCustomerAddress());
        holder.tvTotalAmount.setText("Total: Rs. " + order.getTotalAmount());
        holder.tvDeliveryStatus.setText("Delivery: " + (order.getDeliveryStatus() != null ? order.getDeliveryStatus() : "N/A"));
        holder.tvOrderStatus.setText("Status: " + (order.getStatus() != null ? order.getStatus() : "Unknown"));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context, R.array.order_status_options, android.R.layout.simple_spinner_item);
        holder.spinnerOrderStatus.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(order.getStatus());
        holder.spinnerOrderStatus.setSelection(spinnerPosition);

        holder.btnUpdateStatus.setOnClickListener(v -> {
            String selectedStatus = holder.spinnerOrderStatus.getSelectedItem().toString();
            db.collection("orders").document(order.getOrderId())
                    .update("status", selectedStatus)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show();
                        order.setStatus(selectedStatus);
                        notifyDataSetChanged();

                        if (context instanceof ManageOrdersActivity) {  // cast
                            new Handler().postDelayed(() -> ((ManageOrdersActivity) context).loadOrders(), 1000);
                        }

                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show());
        });

        holder.btnCallCustomer.setOnClickListener(v -> {
            if (order.getCustomerMobile() != null && !order.getCustomerMobile().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + order.getCustomerMobile()));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Customer phone number unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        holder.rvOrderProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.rvOrderProducts.setAdapter(new OrderProductsAdminAdapter(context, order.getItems()));
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvCustomerMobile, tvCustomerAddress, tvTotalAmount, tvDeliveryStatus, tvOrderStatus;
        Spinner spinnerOrderStatus;
        Button btnUpdateStatus, btnCallCustomer;
        RecyclerView rvOrderProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerMobile = itemView.findViewById(R.id.tvCustomerMobile);
            tvCustomerAddress = itemView.findViewById(R.id.tvCustomerAddress);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDeliveryStatus = itemView.findViewById(R.id.tvDeliveryStatus);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            spinnerOrderStatus = itemView.findViewById(R.id.spinnerOrderStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            rvOrderProducts = itemView.findViewById(R.id.rvOrderProducts);
            btnCallCustomer = itemView.findViewById(R.id.btnCallCustomer);
        }
    }
}
