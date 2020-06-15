package cau.injiyong.biking;

public class SearchEntity {
    private String title;
    private String address;
    private String lat;
    private String lon;

    public SearchEntity(String title, String address,String lat,String lon) {
        this.title = title;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() { return lat; }

    public String getLon() { return lon; }
}