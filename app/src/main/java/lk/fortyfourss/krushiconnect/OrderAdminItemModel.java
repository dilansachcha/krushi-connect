package lk.fortyfourss.krushiconnect;

public class OrderAdminItemModel {
    private String name;
    private double boughtQuantity;
    private double price;
    private String farmerName;
    private String farmerMobile;
    private String farmerCity;

    public OrderAdminItemModel() {
    }

    public OrderAdminItemModel(String name, double boughtQuantity, double price,
                               String farmerName, String farmerMobile, String farmerCity) {
        this.name = name;
        this.boughtQuantity = boughtQuantity;
        this.price = price;
        this.farmerName = farmerName;
        this.farmerMobile = farmerMobile;
        this.farmerCity = farmerCity;
    }

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
}
