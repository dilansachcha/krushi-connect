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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class DisputeAdapter extends RecyclerView.Adapter<DisputeAdapter.ViewHolder> {

    private Context context;
    private List<Dispute> disputeList;

    public DisputeAdapter(Context context, List<Dispute> disputeList) {
        this.context = context;
        this.disputeList = disputeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dispute, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dispute dispute = disputeList.get(position);

        holder.tvOrderId.setText("Order ID: " + dispute.getOrderId());
        holder.tvUserId.setText("User ID: " + dispute.getUserId());
        holder.tvIssue.setText("Issue: " + dispute.getIssue());
        holder.tvDescription.setText("Details: " + dispute.getDescription());

        if (dispute.getImageUrl() != null && !dispute.getImageUrl().isEmpty()) {
            holder.ivDisputeImage.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(dispute.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.ivDisputeImage);
        } else {
            holder.ivDisputeImage.setVisibility(View.GONE);
        }

        holder.btnResolve.setOnClickListener(v -> resolveDispute(dispute.getDisputeId(), position));
    }


    private void resolveDispute(String disputeId, int position) {
        Toast.makeText(context, "Resolve feature is disabled for now.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public int getItemCount() {
        return disputeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvUserId, tvIssue, tvDescription;
        ImageView ivDisputeImage;
        Button btnResolve;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvIssue = itemView.findViewById(R.id.tvIssue);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivDisputeImage = itemView.findViewById(R.id.ivDisputeImage);
            btnResolve = itemView.findViewById(R.id.btnResolve);
        }
    }
}
