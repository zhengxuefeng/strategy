package com.lsh.strategy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/**
 * Created by zhengxuefeng on 2016/11/8.
 */
public class Util {

    private static Logger logger = LoggerFactory.getLogger(Util.class);
    //判断一个点在不在多边形内
    public static boolean checkWithJdkGeneralPath(Point2D.Double point, List<Point2D.Double> polygon) {
//        logger.info("polygon size is  " + polygon.size());
        java.awt.geom.GeneralPath p = new java.awt.geom.GeneralPath();

        Point2D.Double first = polygon.get(0);
        p.moveTo(first.x, first.y);
        polygon.remove(0);
        for (Point2D.Double d : polygon) {
            p.lineTo(d.x, d.y);
        }

        p.lineTo(first.x, first.y);

        p.closePath();

        return p.contains(point);
    }


    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt * 1000);
        res = simpleDateFormat.format(date);
        return res;
    }
    //将时间转换为时间戳
    public static String timestamp(String time){
        SimpleDateFormat pTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String aclTime = String.valueOf(pTime.parse(time).getTime()/1000);
        String aclTime = "";
        try{
            aclTime = String.valueOf(pTime.parse(time).getTime()/1000);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return aclTime;
    }
    //根据经纬度计算两点之间距离
    private static final double EARTH_RADIUS = 6378.137;
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    /** *//**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为千米
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double getDistance(double lng1, double lat1, double lng2, double lat2){

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
    public static double  getDistance(Point x, Point y){
        return getDistance(x.getLng(), x.getLat(), y.getLng(), y.getLat());
    }

    /*
    map按value从大到小排序
     */
    public static List<String> sortValue(Map<String, Double> myMap){
        List<Map.Entry<String, Double>> listData = new ArrayList<Map.Entry<String, Double>>(myMap.entrySet());
//        Map<String, Integer> ans = new HashMap<String, Integer>();
        //ans.clear();
        Collections.sort(listData, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<String> resultList = new ArrayList<String>();
        for (Map.Entry<String, Double> arr : listData) {
            logger.info("司机id为：" + arr.getKey() + "   " + "权重为：" + arr.getValue());
            resultList.add(arr.getKey());
            //ans.put(arr.getKey(), arr.getValue());
        }
        return resultList;
    }

    //获取星期几
    public static Integer whichDay(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Integer day =  cal.get(Calendar.DAY_OF_WEEK);
        if (day == 1) day = 7;
        else day -= 1;
//        System.out.println("weedday is : " + day);
        return day;
    }

}
