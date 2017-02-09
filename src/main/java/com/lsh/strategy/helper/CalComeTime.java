package com.lsh.strategy.helper;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.lsh.strategy.util.*;

/**
 * Created by zhengxuefeng on 2016/11/8.
 */
public class CalComeTime {

    @Autowired
    private GetData getData;

    private static Logger logger = LoggerFactory.getLogger(CalComeTime.class);

//    public class ObjectArrayMapper implements RowMapper {
//        int columnCount = -1;
//
//        @Override
//        public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
//            if (columnCount == -1) columnCount = rs.getMetaData().getColumnCount();
//            Object[] row = new Object[columnCount];
//            for (int i = 0; i < columnCount; i++) {
//                row[i] = rs.getObject(i + 1);
//            }
//            return row;
//        }
//    }

    //计算超市和仓库的距离
    public Double calDistance(String lng, String lat, Point2D.Double warehouse) {
        Double marketLng = new Double(lng);
        Double marketLat = new Double(lat);
        Point marketPosition = new Point(marketLng, marketLat);
        Point warehousePosition = new Point(warehouse.getX(), warehouse.getY());

        double distance = Util.getDistance(marketPosition, warehousePosition);
        return distance;
    }

//    @Resource(name = "baseJdbcTemplate")
//    public NamedParameterJdbcTemplate baseJdbcTemplate;

    //得到每个司机回来的时间 筛选出时间上满足的司机id   时间限制参数
    public List<String> comeback(List<String> driverIdList, String zoneId, int timeLimit_minute) {

        long startTime = System.currentTimeMillis();

//        Date dNow = new Date();   //当前时间
//        Date dBefore = new Date();
//        Calendar calendar = Calendar.getInstance(); //得到日历
//        calendar.setTime(dNow);//把当前时间赋给日历
//        calendar.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
//        dBefore = calendar.getTime();   //得到前一天的时间
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置时间格式
//        String defaultStartDate = sdf.format(dBefore);    //格式化前一天
//        String defaultEndDate = sdf.format(dNow); //格式化当前时间
////        System.out.println("defaultStartDate : " + defaultStartDate + " defaultEndDate :  " + defaultEndDate);
//        String currentTime = Util.timestamp(defaultEndDate);
//        String beforeDay = Util.timestamp(defaultStartDate);
//        System.out.println("当前时间戳为" + currentTime);
//        System.out.println("昨天的时间戳为" + beforeDay);
//
//        List<Object[]> result1 = new ArrayList<Object[]>();
//        try {
//            String sql = "select address_info, receipt_status, trans_uid, arrived_at from order_shipping_head where " +
//                    "created_at >= '" + beforeDay + "' and created_at <= '" + currentTime + "'";
////            System.out.println("sql语句: " + sql);
//
//            result1 = baseJdbcTemplate.query(sql, new ObjectArrayMapper());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.debug("sql is error :  " + e.toString());
//        }

        Point2D.Double warehouse = FileData.warehousePosition(zoneId);
        if (warehouse == null) {
            logger.info("zoneId is error : " + zoneId);
            return null;
        }
        System.out.println("仓库位置为 ：" + warehouse.getX() + "  " + warehouse.getY());
        Map<String, Double> comeDistance = new HashMap<String, Double>();//统计每个司机最后被签收一单与仓库的距离
        Map<String, Integer> countNoSent = new HashMap<String, Integer>();//统计每个司机没有送到点的个数
        Map<String, String> lastOrderReceiptTime = new HashMap<String, String>();//每个司机 记录最后被签收一单的时间

//        GetData getData = new GetData();
        List<List<String>> result = getData.getOrderInfo();
        for (List<String> arr : result) {
            if (arr.get(0) == null || arr.get(1) == null || arr.get(2) == null || arr.get(3) == null) continue;
            String driverId = arr.get(2);
            if (!driverIdList.contains(driverId)) continue;
            String address = arr.get(0);
            String status = arr.get(1);
            String receiptTime = arr.get(3);
            String shopPosition_lng = "";
            String shopPosition_lat = "";
            try {
                Object addressInfo = JSON.parseObject(address).get("real_position");
                Object addressInfo2 = JSON.parseObject(addressInfo.toString()).get("position");
                Object position_lng = JSON.parseObject(addressInfo2.toString()).get("lng");
                Object position_lat = JSON.parseObject(addressInfo2.toString()).get("lat");
                shopPosition_lng = position_lng.toString();
                shopPosition_lat = position_lat.toString();
            } catch (Exception e) {
                logger.debug("JSON is error " + e.toString());
            }

            //System.out.println(position_lng.toString() + " " + position_lat.toString());
            Double distance = calDistance(shopPosition_lng, shopPosition_lat, warehouse);
            long start = System.currentTimeMillis() / 1000;
            if (status.equals("1")) {//表示送到了
//                logger.info("receipt time is :" + receiptTime);
                if (Long.valueOf(receiptTime) == 0) receiptTime = String.valueOf(start);
//                logger.info("receipt time is :" + receiptTime);
                if (lastOrderReceiptTime.containsKey(driverId)) {
                    String lastReceiptTime = lastOrderReceiptTime.get(driverId);
                    if (lastReceiptTime.compareTo(receiptTime) < 0) continue;//之前存的订单的时间在这一单之前 就不用跟新
                }
                lastOrderReceiptTime.put(driverId, receiptTime);
                comeDistance.put(driverId, distance);
            } else if (status.equals("0")) {//如果订单没有送到 对应的 未送达点数个数增加
                Integer count = 0;
                if (countNoSent.containsKey(driverId)) count = countNoSent.get(driverId);
                count += 1;
                countNoSent.put(driverId, count);
            }
        }

        List<String> satisfyDriverList = new ArrayList<String>();// 保存时间上能满足的司机id list
        //跟新每个司机对应的回来的时间  如果全部送完   则用最后一单回来的时间加上最后一单签收的时间作为回来的时间
        // 如果没有送完   则用最后一单回来的时间加上最后一单签收的时间 再加上剩余点所需要的时间   作为回来的时间 精确到秒  其中会剔除未送点大于8个的司机
        for (String driverId : driverIdList) {
            if ((!comeDistance.containsKey(driverId)) && (!countNoSent.containsKey(driverId))) {// 没有记录的认为是新司机
                satisfyDriverList.add(driverId);
            } else {
                long comeTime = 0;
                Double dis = comeDistance.get(driverId);
                logger.info("driver come distance : " + driverId + " " + dis);
                if (countNoSent.containsKey(driverId)) {//司机有没有送完的点
                    int notSentCount = countNoSent.get(driverId);
                    logger.info("driver not sent point is : " + driverId + " " + notSentCount);
                    if (notSentCount > 8) {
                        logger.info("delete driver id is : " + driverId + " because not sent number is more 8");
                        continue;//剔除未送点大于8个的司机
                    }
                    if (lastOrderReceiptTime.containsKey(driverId))
                        comeTime = (long) ((dis / 50 * 60) * 60 + (30 * notSentCount)) * 60 + Long.valueOf(lastOrderReceiptTime.get(driverId));
                } else {//司机全部送完
                    if (lastOrderReceiptTime.containsKey(driverId))
                        comeTime = (long) (dis / 50 * 60) * 60 + Long.valueOf(lastOrderReceiptTime.get(driverId));
                }
                logger.info("driver come time is : " + driverId + " " + comeTime);
//                logger.info("driver lastOrderReceiptTime : " + driverId + Long.valueOf(lastOrderReceiptTime.get(driverId)));
                long start = System.currentTimeMillis() / 1000;
                long time_limit = timeLimit_minute * 60;
//                logger.info("last order receipt time is : " + lastOrderReceiptTime.get(driverId));
//                logger.info("come time is : " + String.valueOf(comeTime) + "  " + " now time is :  " + Util.stampToDate(String.valueOf(start)) + "come distance is : " + dis);
//                logger.info("time difference is : " + (comeTime - start));
                if (comeTime - start > time_limit) {
                    logger.info("delete driver id is " + driverId + " this driver comeback time is more " + timeLimit_minute + "minute");
                    logger.info("该司机没有送到单数为：" + countNoSent.containsKey(driverId));
                    logger.info("回来时间为：" + (comeTime - start));
                    continue;// 剔除回来时间大于500分钟的司机
                }
                satisfyDriverList.add(driverId);
            }
        }
        long endTime = System.currentTimeMillis();
        logger.info("Time spent in comeback is " + (endTime - startTime));
        return satisfyDriverList;
    }
}
