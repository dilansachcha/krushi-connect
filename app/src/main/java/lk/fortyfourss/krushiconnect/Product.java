package lk.fortyfourss.krushiconnect;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private double pricePerKg;
    private double stockKg;
    private String status;
    private String location;
    private DocumentReference category;
    private DocumentReference farmerId;
    private List<String> imageUrls;
    private String farmerName;

    @ServerTimestamp
    private Date createdAt;

    public Product() {
    }

    public Product(String id, String name, String description, double pricePerKg, double stockKg, String status,
                   String location, DocumentReference category, DocumentReference farmerId, List<String> imageUrls, Date createdAt, String farmerName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pricePerKg = pricePerKg;
        this.stockKg = stockKg;
        this.status = status;
        this.location = location;
        this.category = category;
        this.farmerId = farmerId;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.farmerName = farmerName;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(double pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public double getStockKg() {
        return stockKg;
    }

    public void setStockKg(double stockKg) {
        this.stockKg = stockKg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DocumentReference getCategory() {
        return category;
    }

    public void setCategory(DocumentReference category) {
        this.category = category;
    }

    public DocumentReference getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(DocumentReference farmerId) {
        this.farmerId = farmerId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {

        this.createdAt = createdAt;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }
}

