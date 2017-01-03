package com.lsh.strategy.helper;

import com.lsh.strategy.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.geom.Point2D;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengxuefeng on 2016/11/8.
 */
public class DriverRank {

    private static Logger logger = LoggerFactory.getLogger(DriverRank.class);
    @Autowired
    private GetData getdata;


    //考虑司机家的位置 分地区的分段函数， 每个距离包含一个权重
    public Double disToScore(Double distance, String zoneId) {
        if (zoneId.contains("09")) {
            if (distance < 5.0) {
                return 0.9;
            } else if (distance < 10.0) {
                return 0.8;
            } else if (distance < 15.0) {
                return 0.7;
            } else if (distance < 20.0) {
                return 0.6;
            } else if (distance < 25.0) {
                return 0.45;
            } else if (distance < 30.0) {
                return 0.3;
            } else {
                return 0.2;
            }
        }
        return 0.5;
    }

    //考虑司机家的位置  读取每个司机家的位置 求出与线路的距离  换成权重   对没有司机家位置的   默认一个权重
    public Map<String, Double> getDriverHomeScore(List<String> driverIdList, String zoneId, Point2D.Double linePoint) {
        HashMap<String, Point2D.Double> driverHomeAddress = getdata.getDriverAddress(driverIdList);//读取司机家的位置
        Map<String, Double> driverScore = new HashMap<String, Double>();
        for (String driverId : driverIdList) {
            if (driverHomeAddress.containsKey(driverId)) {
                Point2D.Double driverAddress = driverHomeAddress.get(driverId);
                Double distabce = Util.getDistance(driverAddress.getX(), driverAddress.getY(), linePoint.getX(), linePoint.getY());
                logger.info("driverHome to line distance is : " + distabce);
                driverScore.put(driverId, disToScore(distabce, zoneId));
            } else {
                driverScore.put(driverId, 0.6);//没有司机家位置数据的  默认一个权重
            }
            logger.info("driver id and score is : " + driverId + " " + driverScore.get(driverId));
        }
        return driverScore;
    }


    public List<String> countWalk(List<String> driverIdList, List<String> addressIdList, HashMap<String, Integer> driverOrderCount, String zoneId, Point2D.Double linePoint) {
        long start = System.currentTimeMillis();
        HashMap<String, Integer> driverHasSent = new HashMap<String, Integer>();
        Map<String, Double> driverHasWalk = new HashMap<String, Double>();
        Integer hasSentMax = 0;
        for (String driverId : driverIdList) {
//            String driverId = entry.getKey();
            String getredis = getdata.getHas(driverId);
            Integer count = 0;
            if (getredis != null) {
                for (String arr : addressIdList) {
                    boolean judge = getredis.contains(arr);
                    if (judge) count++;
                }
            }
//            driverHAswalk.put(driverId, count);
            if (count > hasSentMax) hasSentMax = count;
            driverHasSent.put(driverId, count);
        }
        List<String> noDaySent = FileData.getNotSentDriverIdList();//拿到只能白天装货的司机id list
        //拿到每个司机对应的分数
        Map<String, Double> driverScore = getDriverHomeScore(driverIdList, zoneId, linePoint);
        for (String driverId : driverIdList) {

            Integer orderCount = 0;
            if (driverOrderCount.containsKey(driverId)) {
                orderCount = driverOrderCount.get(driverId);
            }
            if (orderCount > 21) orderCount = 21;
            if (driverHasSent.containsKey(driverId)) {
                Integer countSentMin = (hasSentMax + 2) > 10 ? (hasSentMax + 2) : 10;
                if (driverScore.containsKey(driverId)) {
                    Double driverWeight = 0.3 * driverHasSent.get(driverId) / countSentMin + 0.1 * orderCount / 21 + 0.6 * driverScore.get(driverId);
                    //如果该司机只能接受白天装货 如果需要晚上装货 就把他的权重降低点  这里是乘以0.5
                    Date now = new Date();
                    Integer nowHour = now.getHours();
                    if (noDaySent.contains(driverId) && (nowHour > 10 && nowHour < 19)){
                        driverWeight *= 0.5;
                        logger.info("this driver can not sent in the evering : " + driverId);
                    }
                    logger.info("driver id , driver has sent and  recent order count : " + driverId + " " + driverHasSent.get(driverId) + " " + orderCount);
                    driverHasWalk.put(driverId, driverWeight);
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.info("time spend in DriverRank : " + (long)(end - start));
        return Util.sortValue(driverHasWalk);
    }
}
