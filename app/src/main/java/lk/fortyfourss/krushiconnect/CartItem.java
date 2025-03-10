package lk.fortyfourss.krushiconnect;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String productId;
    private String productName;
    private String farmerId;
    private String farmerName;
    private String farmerMobile;
    private String farmerCity;
    private double pricePerKg;
    private double selectedQuantity;
    private double totalPrice;
    private String productImage;

    public CartItem() {
    }

    public CartItem(String productId, String productName, String farmerId, String farmerName, String farmerMobile,
                    String farmerCity, double pricePerKg, double selectedQuantity, double totalPrice, String productImage) {
        this.productId = productId;
        this.productName = productName;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.farmerMobile = farmerMobile;
        this.farmerCity = farmerCity;
        this.pricePerKg = pricePerKg;
        this.selectedQuantity = selectedQuantity;
        this.totalPrice = totalPrice;
        this.productImage = productImage;
    }

    protected CartItem(Parcel in) {
        productId = in.readString();
        productName = in.readString();
        farmerId = in.readString();
        farmerName = in.readString();
        farmerMobile = in.readString();
        farmerCity = in.readString();
        pricePerKg = in.readDouble();
        selectedQuantity = in.readDouble();
        totalPrice = in.readDouble();
        productImage = in.readString();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(productName);
        dest.writeString(farmerId);
        dest.writeString(farmerName);
        dest.writeString(farmerMobile);
        dest.writeString(farmerCity);
        dest.writeDouble(pricePerKg);
        dest.writeDouble(selectedQuantity);
        dest.writeDouble(totalPrice);
        dest.writeString(productImage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getProductId() {
        return productId;
    }
    public String getProductName() {
        return productName;
    }
    public String getFarmerId() {
        return farmerId;
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
    public double getPricePerKg() {
        return pricePerKg;
    }
    public double getSelectedQuantity() {
        return selectedQuantity;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public String getProductImage() {
        return productImage;
    }
}
