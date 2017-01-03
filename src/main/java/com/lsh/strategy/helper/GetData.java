package com.lsh.strategy.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsh.strategy.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.lsh.strategy.dao.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhengxuefeng on 2016/11/7.
 */
@Component
public class GetData {

    private static Logger logger = LoggerFactory.getLogger(GetData.class);

    @Autowired
    private RedisHashDao redisHashDao;

//    public RedisStringDao getRedisStringDao() {
//        return redisStringDao;
//    }
//
//    public void setRedisStringDao(RedisStringDao redisStringDao) {
//        this.redisStringDao = redisStringDao;
//    }


    /*
    读数据库
     */
    @Resource(name = "baseJdbcTemplate")
    public NamedParameterJdbcTemplate baseJdbcTemplate;

    public class ObjectArrayMapper implements RowMapper {
        int columnCount = -1;

        @Override
        public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
            if (columnCount == -1) columnCount = rs.getMetaData().getColumnCount();
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            return row;
        }
    }

    //读取数据库中订单相关信息
    public List<Object[]> getOrderInfo() {
        long start = System.currentTimeMillis();
        Date dNow = new Date();   //当前时间
        Date dBefore = new Date();
        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
        dBefore = calendar.getTime();   //得到前一天的时间

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置时间格式
        String defaultStartDate = sdf.format(dBefore);    //格式化前一天
        String defaultEndDate = sdf.format(dNow); //格式化当前时间
//        System.out.println("defaultStartDate : " + defaultStartDate + " defaultEndDate :  " + defaultEndDate);
        String currentTime = Util.timestamp(defaultEndDate);
        String beforeDay = Util.timestamp(defaultStartDate);
//        System.out.println("当前时间戳为" + currentTime);
//        System.out.println("昨天的时间戳为" + beforeDay);

        List<Object[]> result = new ArrayList<Object[]>();
        try {
            String sql = "select address_info, receipt_status, trans_uid, arrived_at from order_shipping_head where " + "created_at >= '" + beforeDay +  "'";
//            System.out.println("sql语句: " + sql);
            System.out.println(sql);
            result = baseJdbcTemplate.query(sql, new ObjectArrayMapper());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("read sql order_shipping_head is error :  " + e.toString());
        }
        long end = System.currentTimeMillis();
        logger.info("Time spent in getOrderInfo is " + (end - start));
        return result;
    }

    //读取数据库中所有绑定线路的司机
    public List<Object[]> getDriver() {
        Date now = new Date();   //当前时间
        Date before = new Date();
        Integer nowHour = now.getHours();
        Integer nowMinute = now.getMinutes();
        long timeStamp = System.currentTimeMillis();
        timeStamp /= 1000;
        if (nowHour > 10) {
            timeStamp -= (nowHour - 10) * 3600;//当天10点的时间戳
        } else {
            timeStamp -= 24 * 3600;
            timeStamp += (10 - nowHour) * 3600;
        }
        timeStamp -= nowMinute * 60;
        String startTime = String.valueOf(timeStamp);
        logger.info("bound line cal time is : " + Util.stampToDate(startTime));
        List<Object[]> result = new ArrayList<Object[]>();
        try {
            String sql = "select trans_uid from order_wave_head where status < 9 and waybill_no = '' and created_at > '" + startTime + "'";
            result = baseJdbcTemplate.query(sql, new ObjectArrayMapper());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("read sql order_wave_head is error :  " + e.toString());
        }
        return result;
    }


    /*
    读redis
     */
    public String getHas(String driverId) {
        try {
            return redisHashDao.get("tms:recommend_driver", driverId);
        } catch (Exception e) {
            logger.error("read redis is error : " + e.toString());
        }
        return null;
    }

    //读取redis中司机最近送过的运单数
    public HashMap<String, Integer> getOrderCount() {
        String redisResult = "";
        try {
            redisResult = redisHashDao.get("tms:driver_order_count", "orders-count");
        } catch (Exception e) {
            logger.error("read redis is error : " + e.toString());
        }
        HashMap<String, Integer> driverOrderCount = new HashMap<String, Integer>();
        try {
            JSONObject p = JSON.parseObject(redisResult);
            for (JSONObject.Entry<String, Object> entry : p.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                driverOrderCount.put(key.toString(), Integer.valueOf(value.toString()));
            }
        } catch (Exception e) {
            logger.error("analysis redis is error : " + e.toString());
        }
        return driverOrderCount;
    }

    public HashMap<String, Point2D.Double> getDriverAddress(List<String> driverIdList) {

        HashMap<String, Point2D.Double> driverHomeAddress = new HashMap<String, Point2D.Double>();
        String redisResult = "";
        for (String driverId : driverIdList) {
            try {
                redisResult = redisHashDao.get("tms:driver_home_address", driverId);
            } catch (Exception e) {
                logger.error("read getDriverAddress redis is error : " + e.toString());
            }
            if (redisResult != null) {
                try {
                    Double homeLng = Double.valueOf(JSON.parseObject(redisResult).get("lng").toString());
                    Double homeLat = Double.valueOf(JSON.parseObject(redisResult).get("lat").toString());
                    driverHomeAddress.put(driverId, new Point2D.Double(homeLng, homeLat));
                } catch (Exception e) {
                    logger.error("analysis redis is error : " + e.toString());
                }
            }
        }
        return driverHomeAddress;
    }

}
