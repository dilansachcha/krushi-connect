package lk.fortyfourss.krushiconnect;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDetails;
    private Button btnDownloadReceipt;
    private FirebaseFirestore db;
    private String orderId, userId;
    private String orderDetails;

    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDetails = findViewById(R.id.tvOrderDetails);
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);

        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("ORDER_ID");
        userId = getIntent().getStringExtra("USER_ID");

        tvOrderId.setText("Order ID: " + orderId);

        fetchOrderDetails();

        btnDownloadReceipt.setOnClickListener(v -> generateReceiptPDF());
    }

    private String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void fetchOrderDetails() {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        totalAmount = documentSnapshot.getDouble("totalAmount");
                        String status = documentSnapshot.getString("status");

                        Long timestampLong = documentSnapshot.getLong("timestamp");
                        long timestamp = (timestampLong != null) ? timestampLong : 0;

                        orderDetails = "Total Amount: Rs. " + totalAmount + "\n"
                                + "Order Status: " + status + "\n"
                                + "Timestamp: " + getFormattedDate(timestamp);

                        tvOrderDetails.setText(orderDetails);
                    } else {
                        Toast.makeText(this, "Order details not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch order details", Toast.LENGTH_SHORT).show());
    }



    private void generateReceiptPDF() {
        if (orderDetails == null) {
            Toast.makeText(this, "Order details are missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(22);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint textPaint = new Paint();
        textPaint.setTextSize(18);
        textPaint.setColor(Color.BLACK);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.DKGRAY);
        linePaint.setStrokeWidth(2);

        int startX = 50;
        int startY = 80;
        int lineSpacing = 40;

        // Draw Border
        canvas.drawRect(30, 30, 570, 770, linePaint);

        // Title
        canvas.drawText("Krushi Connect - Order Receipt", startX + 70, startY, titlePaint);
        startY += lineSpacing + 20;

        // Order Details
        canvas.drawLine(startX, startY, 550, startY, linePaint);
        startY += 30;

        canvas.drawText("Order ID:", startX, startY, textPaint);
        canvas.drawText(orderId, startX + 150, startY, textPaint);
        startY += lineSpacing;

        canvas.drawText("Total Amount:", startX, startY, textPaint);
        canvas.drawText("Rs. " + totalAmount, startX + 150, startY, textPaint);
        startY += lineSpacing;

        canvas.drawText("Order Status:", startX, startY, textPaint);
        canvas.drawText("Paid", startX + 150, startY, textPaint);
        startY += lineSpacing;

        canvas.drawText("Date & Time:", startX, startY, textPaint);
        canvas.drawText(getFormattedDate(), startX + 150, startY, textPaint);
        startY += lineSpacing;

        canvas.drawLine(startX, startY, 550, startY, linePaint);
        startY += 30;

        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("Thank you for shopping with Krushi Connect!", startX + 30, startY, textPaint);

        pdfDocument.finishPage(page);

        // Saving
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Receipt_" + orderId + ".pdf");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.flush();
            fos.close();
            pdfDocument.close();

            if (file.exists()) {
                Toast.makeText(this, "Receipt saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                openPDF(file);
            } else {
                Toast.makeText(this, "Error: Receipt not found after saving!", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving receipt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void openPDF(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer found!", Toast.LENGTH_SHORT).show();
        }
    }




}
