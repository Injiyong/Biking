package cau.injiyong.biking;


public class RoadInfoItem {

    private Double startLat;
    private Double startLon;
    private Double finishLat;
    private Double finishLon;
    private double total_rate;
    private int total_user;

    public RoadInfoItem(){

    }

    public RoadInfoItem(Double startLat,Double startLon, Double finishLat,Double finishLon,double total_rate,int total_user){
        this.startLat = startLat;
        this.startLon = startLon;
        this.finishLat = finishLat;
        this.finishLon = finishLon;
        this.total_rate = total_rate;
        this.total_user = total_user;
    }

    public Double getStartLat() {
        return startLat;
    }

    public Double getStartLon() {
        return startLon;
    }

    public Double getFinishLat() {
        return finishLat;
    }

    public Double getFinishLon() {
        return finishLon;
    }

    public double getTotal_rate() {
        return total_rate;
    }

    public void setTotal_user(int total_user) {
        this.total_user = total_user;
    }

    public void setTotal_rate(double total_rate) {
        this.total_rate = total_rate;
    }

    public int getTotal_user() {
        return total_user;
    }
}
