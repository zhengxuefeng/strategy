package com.lsh.strategy.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

    //读取数据库中订单相关信息
    public List<List<String>> getOrderInfo() {
        long start = System.currentTimeMillis();

        String redisResult = "";

        try {
            redisResult = redisHashDao.get("tms:driver_sql", "sql_result");
//            logger.info("redisResult : " + redisResult);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("read sql_redis is error :  " + e.toString());
        }
        List<List<String>> result = new ArrayList<>();
//        List result = new ArrayList<>();
        try {
            JSONObject object = JSON.parseObject(redisResult);
//            result = JSONArray.parseArray(object.get("line_info").toString());
//            result = JSONArray.parseArray(object.get("line_info").toString());
//            logger.info("put redis is  : " + result);
            for (JSONObject.Entry<String, Object> entry : object.entrySet()) {
                String value = entry.getValue().toString();
                String addressInfo = JSON.parseObject(value).get("address_info").toString();
                String receiptStatus = JSON.parseObject(value).get("receipt_status").toString();
                String transUid = JSON.parseObject(value).get("trans_uid").toString();
                String arrivedAt = JSON.parseObject(value).get("arrived_at").toString();
                List<String> sqlInfo = new ArrayList<>();
                sqlInfo.add(addressInfo);
                sqlInfo.add(receiptStatus);
                sqlInfo.add(transUid);
                sqlInfo.add(arrivedAt);
//                logger.info("sql redis is : " + sqlInfo);
                result.add(sqlInfo);
            }
        } catch (Exception e) {
            logger.error(e.toString());
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info("Time spent in getOrderInfo is " + (end - start));
        return result;
    }


    //读redis  司机ID对应的名字
    public String readDriverName(String driverId) {
        String driverName = "";
        try {
            driverName = redisHashDao.get("tms:driver_name", driverId);
        } catch (Exception e) {
            logger.info(e.toString());
            e.printStackTrace();
        }
        return driverName;
    }

    //读redis   查看司机id对应的最近三天送了几单   目前只考虑天津的情况
    public Integer getNumThreeDaySent(String driverId) {
        try {
            String countNum = redisHashDao.get("tms:driver_count_threeDay_sent", driverId);
            if (redisHashDao.hasKey("tms:driver_count_threeDay_sent", driverId)) {
                return Integer.valueOf(countNum);
            } else return 0;
        } catch (Exception e) {
            logger.info(e.toString());
            e.printStackTrace();
        }
        return 0;
    }

}
