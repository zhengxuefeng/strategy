package com.lsh.strategy.util;

/**
 * Created by zhengxuefeng on 2016/11/8.
 */
public class Point {
    private double lng;
    private double lat;
    public Point(double lngp, double latp){
        this.lng = lngp;
        this.lat = latp;
    }

    public double getLng(){
        return lng;
    }
    public double getLat(){
        return lat;
    }
}
