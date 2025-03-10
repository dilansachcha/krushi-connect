package lk.fortyfourss.krushiconnect;

public class OrderedProduct {
    private String name;
    private double boughtQuantity;
    private String farmerName;
    private String farmerMobile;
    private String imageUrl;

    public OrderedProduct() {}

    public OrderedProduct(String name, double boughtQuantity, String farmerName, String farmerMobile, String imageUrl) {
        this.name = name;
        this.boughtQuantity = boughtQuantity;
        this.farmerName = farmerName;
        this.farmerMobile = farmerMobile;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBoughtQuantity() {
        return boughtQuantity;
    }

    public void setBoughtQuantity(double boughtQuantity) {
        this.boughtQuantity = boughtQuantity;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getFarmerMobile() {
        return farmerMobile;
    }

    public void setFarmerMobile(String farmerMobile) {
        this.farmerMobile = farmerMobile;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

