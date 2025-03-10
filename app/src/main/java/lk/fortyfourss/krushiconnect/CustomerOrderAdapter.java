package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;

    public CustomerOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Order ID: " + order.getOrderId());
        holder.tvOrderStatus.setText("Status: " + order.getStatus());
        holder.tvDeliveryStatus.setText("Delivery: " + order.getDeliveryStatus());
        holder.tvTotalAmount.setText("Grand Total: Rs. " + order.getTotalAmount());

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            CustomerOrderProductAdapter productAdapter = new CustomerOrderProductAdapter(context, order.getItems(), order.getDeliveryStatus());
            holder.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerViewProducts.setAdapter(productAdapter);
            holder.recyclerViewProducts.setVisibility(View.VISIBLE);
        } else {
            holder.recyclerViewProducts.setVisibility(View.GONE);
        }

        if ("Completed".equals(order.getStatus())) { //completed orderswlt witri
            holder.btnOpenDispute.setVisibility(View.VISIBLE);
            holder.btnOpenDispute.setOnClickListener(v -> {
                Intent intent = new Intent(context, DisputeFormActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                context.startActivity(intent);
            });
        } else {
            holder.btnOpenDispute.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvDeliveryStatus, tvTotalAmount;
        RecyclerView recyclerViewProducts;
        Button btnCallFarmer, btnOpenDispute;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDeliveryStatus = itemView.findViewById(R.id.tvDeliveryStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            recyclerViewProducts = itemView.findViewById(R.id.recyclerViewProducts);
            btnCallFarmer = itemView.findViewById(R.id.btnCallFarmer);
            btnOpenDispute = itemView.findViewById(R.id.btnOpenDispute);
        }
    }
}
