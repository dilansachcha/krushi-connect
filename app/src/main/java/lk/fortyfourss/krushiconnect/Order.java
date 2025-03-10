package lk.fortyfourss.krushiconnect;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private double totalAmount;
    private String deliveryStatus;
    private String status;
    private long timestamp;
    private List<OrderItem> items;

    public Order() {}

    public Order(String orderId, String userId, double totalAmount, String deliveryStatus, String status, long timestamp, List<OrderItem> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.deliveryStatus = deliveryStatus;
        this.status = status;
        this.timestamp = timestamp;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }
    public String getUserId() {
        return userId;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    public String getStatus() {
        return status;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public List<OrderItem> getItems() {
        return items;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}
