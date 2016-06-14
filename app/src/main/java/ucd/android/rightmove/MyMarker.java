package ucd.android.rightmove;

public class MyMarker {
    private String date_of_sale;
    private String address;
    private String county;
    private double price;
    private String description;
    private Double latitude;
    private Double longitude;

    public MyMarker(String date_of_sale, String address, String county, double price, String description, Double latitude, Double longitude ){
        this.date_of_sale = date_of_sale;
        this.address = address;
        this.county = county;
        this.price = price;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate(){
        return date_of_sale;
    }

    public String getAddress(){
        return address;
    }

    public String getCounty(){
        return county;
    }

    public String getPrice(){
        return price+"";
    }

    public String getDescription(){
        return description;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }
}
