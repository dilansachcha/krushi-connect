package lk.fortyfourss.krushiconnect;

public class FarmerOrderedProductModel {
    private String itemId;
    private String orderId;
    private String name;
    private double boughtQuantity;
    private String productStatus;
    private String deliveryStatus;
    private String customerId;
    private String customerName;
    private String customerCity;
    private String customerMobile;

    // Empty constructor required for Firestore deserialization
    public FarmerOrderedProductModel() {
    }

    // Full constructor for initialization
    public FarmerOrderedProductModel(String itemId, String orderId, String name, double boughtQuantity,
                                     String productStatus, String deliveryStatus, String customerId,
                                     String customerName, String customerCity, String customerMobile) {
        this.itemId = itemId;
        this.orderId = orderId;
        this.name = name;
        this.boughtQuantity = boughtQuantity;
        this.productStatus = productStatus;
        this.deliveryStatus = deliveryStatus;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerCity = customerCity;
        this.customerMobile = customerMobile;
    }

    // Getters
    public String getItemId() { return itemId; }
    public String getOrderId() { return orderId; }
    public String getName() { return name; }
    public double getBoughtQuantity() { return boughtQuantity; }
    public String getProductStatus() { return productStatus; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerCity() { return customerCity; }
    public String getCustomerMobile() { return customerMobile; }

    // Setters
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public void setDeliveryStatus(String deliveryStatus) {this.deliveryStatus = deliveryStatus; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerCity(String customerCity) { this.customerCity = customerCity; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
}
