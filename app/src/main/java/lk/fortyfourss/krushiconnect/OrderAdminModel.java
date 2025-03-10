package lk.fortyfourss.krushiconnect;

import java.util.List;

public class OrderAdminModel {
    private String orderId;
    private String customerName;
    private String customerMobile;
    private String customerAddress;
    private double totalAmount;
    private String deliveryStatus;
    private String status;
    private List<OrderAdminItemModel> items;

    public OrderAdminModel() {
    }

    public OrderAdminModel(String orderId, String customerName, String customerMobile, String customerAddress,
                           double totalAmount, String deliveryStatus, String status, List<OrderAdminItemModel> items) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerMobile = customerMobile;
        this.customerAddress = customerAddress;
        this.totalAmount = totalAmount;
        this.deliveryStatus = deliveryStatus;
        this.status = status;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderAdminItemModel> getItems() {
        return items;
    }

    public void setItems(List<OrderAdminItemModel> items) {
        this.items = items;
    }
}
