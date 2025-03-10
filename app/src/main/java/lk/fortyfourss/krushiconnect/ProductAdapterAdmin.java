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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductAdapterAdmin extends RecyclerView.Adapter<ProductAdapterAdmin.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db;

    public ProductAdapterAdmin(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvProductName.setText(product.getName());
        holder.etProductPrice.setText(String.valueOf(product.getPricePerKg()));
        holder.tvProductStock.setText("Stock: " + product.getStockKg() + " kg (ltr or pcs)");
        holder.tvProductStatus.setText("Status: " + product.getStatus());

        holder.etProductPrice.setEnabled(false);
        holder.btnSavePrice.setVisibility(View.GONE);

        if (product.getCategory() instanceof DocumentReference) {
            ((DocumentReference) product.getCategory()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    holder.tvProductCategory.setText("Category: " + documentSnapshot.getString("name"));
                } else {
                    holder.tvProductCategory.setText("Category: Unknown");
                }
            }).addOnFailureListener(e -> {
                holder.tvProductCategory.setText("Category: Error");
                Log.e("Firestore", "Error fetching category", e);
            });
        } else {
            holder.tvProductCategory.setText("Category: Not Found");
        }

        if (product.getFarmerId() instanceof DocumentReference) {
            ((DocumentReference) product.getFarmerId()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("address")) {
                    String city = documentSnapshot.get("address.city", String.class);
                    if (city != null) {
                        holder.tvProductLocation.setText("Farmer's City: " + city);
                    } else {
                        holder.tvProductLocation.setText("Farmer's City: Unknown");
                    }
                } else {
                    holder.tvProductLocation.setText("Farmer's City: Not Found");
                }
            }).addOnFailureListener(e -> {
                holder.tvProductLocation.setText("Farmer's City: Error");
                Log.e("Firestore", "Error fetching farmer city", e);
            });
        }

        if ("Approved".equals(product.getStatus()) || "Pending Approval".equals(product.getStatus())) {
            holder.ivEditPrice.setVisibility(View.VISIBLE);
        } else {
            holder.ivEditPrice.setVisibility(View.GONE);
        }

        // Enable editing when edit icon is clicked
        holder.ivEditPrice.setOnClickListener(v -> {
            holder.etProductPrice.setEnabled(true);
            holder.etProductPrice.requestFocus();
            holder.btnSavePrice.setVisibility(View.VISIBLE);
        });

        // Save the new price to Firestore when Save button is clicked
        holder.btnSavePrice.setOnClickListener(v -> {
            String newPriceStr = holder.etProductPrice.getText().toString().trim();
            if (newPriceStr.isEmpty()) {
                Toast.makeText(context, "Price cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            double newPrice = Double.parseDouble(newPriceStr); // Convert to double

            db.collection("products").document(product.getId())
                    .update("pricePerKg", newPrice)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Price Updated!", Toast.LENGTH_SHORT).show();
                        product.setPricePerKg(newPrice);
                        holder.etProductPrice.setEnabled(false);
                        holder.btnSavePrice.setVisibility(View.GONE);
                        notifyItemChanged(position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update price!", Toast.LENGTH_SHORT).show();
                        Log.e("Admin", "Error updating price", e);
                    });
        });

        loadProductImages(holder, product);
        updateButtonVisibility(holder, product);

        holder.btnApprove.setOnClickListener(v -> updateProductStatus(product.getId(), "Approved"));
        holder.btnReject.setOnClickListener(v -> updateProductStatus(product.getId(), "Rejected"));
        holder.btnDelete.setOnClickListener(v -> deleteProduct(product.getId()));
    }



    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductCategory, tvProductStock, tvProductStatus, tvProductLocation;
        EditText etProductPrice;
        ImageView ivProductImage1, ivProductImage2, ivProductImage3, ivEditPrice;
        Button btnApprove, btnReject, btnDelete, btnSavePrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductStock = itemView.findViewById(R.id.tvProductStock);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
            tvProductLocation = itemView.findViewById(R.id.tvProductLocation);
            ivProductImage1 = itemView.findViewById(R.id.ivProductImage1);
            ivProductImage2 = itemView.findViewById(R.id.ivProductImage2);
            ivProductImage3 = itemView.findViewById(R.id.ivProductImage3);
            ivEditPrice = itemView.findViewById(R.id.ivEditPrice);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSavePrice = itemView.findViewById(R.id.btnSavePrice);
            etProductPrice = itemView.findViewById(R.id.etProductPrice);
            etProductPrice.setEnabled(false);
        }
    }


    private void loadProductImages(ProductViewHolder holder, Product product) {
        if (product.getImageUrls() != null) {
            int totalImages = product.getImageUrls().size();

            if (totalImages > 0 && product.getImageUrls().get(0) != null) {
                loadImage(holder.ivProductImage1, product.getImageUrls().get(0));
            } else {
                holder.ivProductImage1.setImageResource(R.drawable.placeholder_image);
            }

            if (totalImages > 1 && product.getImageUrls().get(1) != null) {
                loadImage(holder.ivProductImage2, product.getImageUrls().get(1));
            } else {
                holder.ivProductImage2.setImageResource(R.drawable.placeholder_image);
            }

            if (totalImages > 2 && product.getImageUrls().get(2) != null) {
                loadImage(holder.ivProductImage3, product.getImageUrls().get(2));
            } else {
                holder.ivProductImage3.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            holder.ivProductImage1.setImageResource(R.drawable.placeholder_image);
            holder.ivProductImage2.setImageResource(R.drawable.placeholder_image);
            holder.ivProductImage3.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void loadImage(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void updateProductStatus(String productId, String newStatus) {
        db.collection("products").document(productId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Product status updated to: " + newStatus, Toast.LENGTH_SHORT).show();

                    db.collection("products").document(productId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists() && documentSnapshot.contains("farmerId")) {
                                    DocumentReference farmerRef = documentSnapshot.getDocumentReference("farmerId");
                                    if (farmerRef != null) {
                                        sendNotificationToFarmer(farmerRef.getId(),
                                                newStatus.equals("Approved") ? "Product Approved" : "Product Rejected",
                                                newStatus.equals("Approved") ? "Your product has been approved!" : "Your product has been rejected.");
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Log.e("FCM", "Failed to fetch farmer ID", e));

                    refreshProductList();
                })
                .addOnFailureListener(e -> Log.e("Admin", "Failed to update status", e));
    }


    private void deleteProduct(String productId) {//approved ew witrk
        db.collection("products").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Product Deleted!", Toast.LENGTH_SHORT).show();
                    refreshProductList();
                })
                .addOnFailureListener(e -> Log.e("Admin", "Failed to delete product", e));
    }

    private void refreshProductList() {
        db.collection("products").get().addOnSuccessListener(querySnapshot -> {
            productList.clear();
            for (var document : querySnapshot) {
                Product product = document.toObject(Product.class);
                product.setId(document.getId());
                productList.add(product);
            }
            notifyDataSetChanged();
        }).addOnFailureListener(e -> Log.e("Firestore", "Error refreshing product list", e));
    }

    private void sendNotificationToFarmer(String farmerId, String title, String message) {
        db.collection("users").document(farmerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                        String fcmToken = documentSnapshot.getString("fcmToken");

                        if (fcmToken != null) {
                            sendFCMNotification(fcmToken, title, message);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("FCM", "Error fetching farmer's FCM token", e));
    }

    private void sendFCMNotification(String fcmToken, String title, String message) {
        String fcmServerKey = "YOUR_SERVER_KEY"; // Replace with your actual Firebase Server Key

        JSONObject notification = new JSONObject();
        JSONObject jsonRequest = new JSONObject();
        try {
            notification.put("title", title);
            notification.put("body", message);
            jsonRequest.put("to", fcmToken);
            jsonRequest.put("notification", notification);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                    "https://fcm.googleapis.com/fcm/send", jsonRequest,
                    response -> Log.d("FCM", "Notification Sent Successfully"),
                    error -> Log.e("FCM", "Error sending FCM", error)) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "key=" + fcmServerKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("FCM", "Error creating JSON for FCM", e);
        }
    }


    private void updateButtonVisibility(ProductViewHolder holder, Product product) {
        if ("Pending Approval".equals(product.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);
        } else if ("Approved".equals(product.getStatus())) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else if ("Rejected".equals(product.getStatus())) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }
}

