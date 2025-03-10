package lk.fortyfourss.krushiconnect;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvTotalRevenue, tvTotalProductsSold;
    private WebView chartWebView;
    private FirebaseFirestore db;
    private double totalRevenue = 0;
    private int totalOrders = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);

        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalProductsSold = findViewById(R.id.tvTotalProductsSold);
        chartWebView = findViewById(R.id.chartWebView);

        setupDrawer(toolbar);
        fetchDashboardData();
    }

    private void setupDrawer(Toolbar toolbar) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_manage_products) {
                authenticateBeforeAccessing(() -> startActivity(new Intent(AdminDashboardActivity.this, ManageProductsActivity.class)));
            } else if (id == R.id.nav_manage_users) {
                authenticateBeforeAccessing(() -> startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class)));
            } else if (id == R.id.nav_manage_orders) {
                authenticateBeforeAccessing(() -> startActivity(new Intent(AdminDashboardActivity.this, ManageOrdersActivity.class)));
            } else if (id == R.id.nav_manage_disputes) {
                authenticateBeforeAccessing(() -> startActivity(new Intent(AdminDashboardActivity.this, ManageDisputesActivity.class)));
            } else if (id == R.id.nav_admin_logout) {
                showLogoutDialog();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void authenticateBeforeAccessing(Runnable onSuccessAction) {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Fingerprint authentication not supported!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(AdminDashboardActivity.this, "Fingerprint Verified!", Toast.LENGTH_SHORT).show();
                        onSuccessAction.run();  // Execute the intended action after successful authentication
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(AdminDashboardActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(AdminDashboardActivity.this, "Authentication Error: " + errString, Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Admin Access Required")
                .setSubtitle("Authenticate with fingerprint to proceed")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }


    private void fetchDashboardData() {
        db.collection("orders").get().addOnSuccessListener(querySnapshot -> {
            double totalRevenue = 0;
            int totalOrders = 0;
            Map<String, Double> revenueByDay = new TreeMap<>();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (int i = 0; i < 7; i++) {
                String dateKey = sdf.format(calendar.getTime());
                revenueByDay.put(dateKey, 0.0);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            for (QueryDocumentSnapshot document : querySnapshot) {
                if (document.contains("totalAmount") && document.contains("timestamp")) {
                    double amount = document.getDouble("totalAmount");
                    long timestamp = document.getLong("timestamp");

                    String orderDate = sdf.format(new Date(timestamp));//timestamp date format ekt

                    if (revenueByDay.containsKey(orderDate)) {
                        revenueByDay.put(orderDate, revenueByDay.get(orderDate) + amount);
                        totalRevenue += amount;
                    }
                }
                totalOrders++;
            }

            tvTotalRevenue.setText(String.format(Locale.US, "Total Revenue: Rs. %.2f", totalRevenue));
            tvTotalProductsSold.setText(String.format(Locale.US, "Total Orders: %d", totalOrders));

            loadChart(revenueByDay);
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminDashboardActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadChart(Map<String, Double> revenueByDay) {
        StringBuilder days = new StringBuilder();
        StringBuilder revenues = new StringBuilder();

        for (Map.Entry<String, Double> entry : revenueByDay.entrySet()) {
            days.append("'").append(entry.getKey()).append("',");
            revenues.append(entry.getValue()).append(",");
        }

        if (days.length() > 0) days.deleteCharAt(days.length() - 1);
        if (revenues.length() > 0) revenues.deleteCharAt(revenues.length() - 1);

        // HTML + JavaScript
        String htmlData = "<html><head>"
                + "<script src='https://cdnjs.cloudflare.com/ajax/libs/echarts/5.4.0/echarts.min.js'></script>"
                + "</head><body>"
                + "<div id='chart' style='width:100%;height:300px;'></div>"
                + "<script>"
                + "var chart = echarts.init(document.getElementById('chart'));"
                + "var option = {"
                + "title: { text: 'Daily Revenue for Current Week' },"
                + "tooltip: { trigger: 'axis' },"
                + "xAxis: { type: 'category', data: [" + days + "] },"
                + "yAxis: {"
                + "    type: 'value',"
                + "    name: 'Rs.',"
                + "    max: 10000,"
                + "    axisLabel: {"
                + "        formatter: function(value) { return (value / 1000) + 'K'; }"
                + "    }"
                + "},"
                + "series: [{ type: 'line', data: [" + revenues + "] }]"
                + "};"
                + "chart.setOption(option);"
                + "</script></body></html>";

        chartWebView.getSettings().setJavaScriptEnabled(true);
        chartWebView.setWebViewClient(new WebViewClient());
        chartWebView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }


    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isAdminLoggedIn", false);
                    editor.apply();
                    startActivity(new Intent(AdminDashboardActivity.this, AdminLoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, AdminLoginActivity.class));
        finish();
    }
}
