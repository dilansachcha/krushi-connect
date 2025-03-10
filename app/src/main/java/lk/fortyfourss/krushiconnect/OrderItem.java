package lk.fortyfourss.krushiconnect;

public class OrderItem {
    private String name;
    private double boughtQuantity;
    private double price;
    private String farmerName;
    private String farmerMobile;
    private String farmerCity;
    private String deliveryStatus;
    private String imageUrl;

    public OrderItem() {}

    public OrderItem(String name, double boughtQuantity, double price, String farmerName, String farmerMobile, String farmerCity, String deliveryStatus, String imageUrl) {
        this.name = name;
        this.boughtQuantity = boughtQuantity;
        this.price = price;
        this.farmerName = farmerName;
        this.farmerMobile = farmerMobile;
        this.farmerCity = farmerCity;
        this.deliveryStatus = deliveryStatus;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() {
        return name;
    }
    public double getBoughtQuantity() {
        return boughtQuantity;
    }

    public double getPrice() {
        return price;
    }

    public String getFarmerName() {
        return farmerName;
    }
    public String getFarmerMobile() {
        return farmerMobile;
    }
    public String getFarmerCity() {
        return farmerCity;
    }
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}
