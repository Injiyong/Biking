package cau.injiyong.biking;

public class RecentInformationItem {

    private String s_time;
    private String f_time;
    private String s_lat;
    private String s_long;
    private String f_lat;
    private String f_long;
    private String sum_dist;
    private String timer;

    public RecentInformationItem (){

    }

    public RecentInformationItem (String s_time,String f_time,String s_lat,String s_long,String f_lat,String f_long,String sum_dist, String timer)
    {
        this.s_time=s_time;
        this.f_time=f_time;
        this.s_lat=s_lat;
        this.s_long=s_long;
        this.f_lat=f_lat;
        this.f_long=f_long;
        this.sum_dist=sum_dist;
        this.timer=timer;
    }

    public String getS_time() {
        return s_time;
    }
    public void setS_time(String s_time){
        this.s_time = s_time;
    }

    public String getF_lat() {
        return f_lat;
    }

    public String getS_lat() {
        return s_lat;
    }

    public String getF_time() {
        return f_time;
    }

    public String getF_long() {
        return f_long;
    }

    public String getS_long() {
        return s_long;
    }

    public String getSum_dist() {
        return sum_dist;
    }

    public String getTimer() {
        return timer;
    }

    public void setF_lat(String f_lat) {
        this.f_lat = f_lat;
    }

    public void setF_long(String f_long) {
        this.f_long = f_long;
    }

    public void setF_time(String f_time) {
        this.f_time = f_time;
    }

    public void setS_lat(String s_lat) {
        this.s_lat = s_lat;
    }

    public void setSum_dist(String sum_dist) {
        this.sum_dist = sum_dist;
    }

    public void setS_long(String s_long) {
        this.s_long = s_long;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }
}

