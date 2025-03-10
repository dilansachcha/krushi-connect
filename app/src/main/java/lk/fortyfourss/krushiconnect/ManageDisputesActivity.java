package lk.fortyfourss.krushiconnect;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ManageDisputesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDisputes;
    private DisputeAdapter disputeAdapter;
    private List<Dispute> disputeList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_disputes);

        recyclerViewDisputes = findViewById(R.id.recyclerViewDisputes);
        progressBar = findViewById(R.id.progressBar);

        recyclerViewDisputes.setLayoutManager(new LinearLayoutManager(this));
        disputeList = new ArrayList<>();
        disputeAdapter = new DisputeAdapter(this, disputeList);
        recyclerViewDisputes.setAdapter(disputeAdapter);

        db = FirebaseFirestore.getInstance();

        loadDisputes();
    }

    private void loadDisputes() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("disputes")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    disputeList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Dispute dispute = document.toObject(Dispute.class);
                        if (dispute != null) {
                            dispute.setDisputeId(document.getId());
                            disputeList.add(dispute);
                        }
                    }
                    disputeAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageDisputesActivity.this, "Failed to load disputes", Toast.LENGTH_SHORT).show();
                });
    }
}
