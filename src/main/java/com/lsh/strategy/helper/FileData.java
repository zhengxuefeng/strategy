package com.lsh.strategy.helper;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lsh.strategy.start.RecommendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhengxuefeng on 2016/11/29.
 */
public class FileData {

    private static HashMap<String, List<List<Point2D.Double>>> smallCarMap;//必须小车的区域   城市-区域list
    private static HashMap<String, List<List<Point2D.Double>>> certifiMap;//必须有证的区域   城市-区域list
    private static HashMap<String, List<List<Point2D.Double>>> limitNumMap;//限号区域   城市-区域list  暂时不考虑限号的情况
    private static HashMap<String, Map<Integer, String>> restrictNumber; //每个城市的每天的限号情况
    private static List<String> noDaySent = new ArrayList<String>();//不能晚上装货的司机id list
    private static Logger logger = LoggerFactory.getLogger(RecommendServer.class);

    //根据文件路径  读取文件里面的经纬度放进list中
    public static List<Point2D.Double> getFilePosition(String fileName) {
        File file = new File(fileName);
        BufferedReader read = null;
        try {
            List<Point2D.Double> positionList = new ArrayList<Point2D.Double>();
            read = new BufferedReader(new FileReader(file));
            String readInfo = null;
            while ((readInfo = read.readLine()) != null) {
                if (readInfo.charAt(0) == '#') continue;
                String[] p = readInfo.split(",");
                Double lng = Double.valueOf(p[0]);
                Double lat = Double.valueOf(p[1]);
//                System.out.println(lng + "  " + lat);
                positionList.add(new Point2D.Double(lng, lat));
            }
            return positionList;
        } catch (Exception e) {
            logger.info(e.toString());
            e.printStackTrace();
            return null;
        }
    }


    //读取限号情况文件 存进对应的map中  目前只考虑了北京和天津的限号情况
    public static void getRestrictNumber() {
        restrictNumber = new HashMap<String, Map<Integer, String>>();
        String directory = "./Number-limit/";
        File file = new File(directory);
        File[] tempList = file.listFiles();
        for (File item : tempList) {
            BufferedReader read = null;
            String fileName = item.toString();
            fileName = fileName.toLowerCase();
//            System.out.println("file name is : " + fileName);
            try {
                Map<Integer, String> dayLimitNumber = new HashMap<Integer, String>();
                read = new BufferedReader(new FileReader(item));
                String readInfo = null;
                Integer weekDay = 1;
                while ((readInfo = read.readLine()) != null) {
//                    System.out.println("content is : " + readInfo);
                    if (readInfo.charAt(0) == '#') continue;
                    dayLimitNumber.put(weekDay, readInfo);
                    weekDay += 1;
                }
                if (fileName.contains("dc09")) restrictNumber.put("dc09", dayLimitNumber);
                if (fileName.contains("dc10")) restrictNumber.put("dc10", dayLimitNumber);
            } catch (Exception e) {
                logger.info(e.toString());
                e.printStackTrace();
            }

        }
        System.out.println("$$$$$$" + restrictNumber.get("dc09").get(1));

    }

    //读取配置文件  主要是各种限制区域 和限号情况 存进对应的map中
    public static void getFileData() {

        smallCarMap = new HashMap<String, List<List<Point2D.Double>>>();
        certifiMap = new HashMap<String, List<List<Point2D.Double>>>();
        limitNumMap = new HashMap<String, List<List<Point2D.Double>>>();

        String directory = "./Region-limit/";
        File file = new File(directory);
        File[] tempList = file.listFiles();
        if (tempList == null) {
            System.out.println("read file fail");
            return;
        }
        List<List<Point2D.Double>> small_dc09 = new ArrayList<List<Point2D.Double>>();
        List<List<Point2D.Double>> small_dc10 = new ArrayList<List<Point2D.Double>>();

        List<List<Point2D.Double>> certifi_dc09 = new ArrayList<List<Point2D.Double>>();
        List<List<Point2D.Double>> certifi_dc10 = new ArrayList<List<Point2D.Double>>();

        List<List<Point2D.Double>> number_dc09 = new ArrayList<List<Point2D.Double>>();
        List<List<Point2D.Double>> number_dc10 = new ArrayList<List<Point2D.Double>>();

        for (File item : tempList) {
            String fimeName = item.toString();
//            System.out.println(fimeName);
            List<Point2D.Double> result = getFilePosition(fimeName);
            if (fimeName.contains("small")) {

                if (fimeName.contains("dc09")) {
                    small_dc09.add(result);
                } else if (fimeName.contains("dc10")) {
                    small_dc10.add(result);
                }

            } else if (fimeName.contains("certificate")) {
                if (fimeName.contains("dc09")) {
                    certifi_dc09.add(result);
                } else if (fimeName.contains("dc10")) {
                    certifi_dc10.add(result);
                }
            } else if (fimeName.contains("car_number")) {
                if (fimeName.contains("dc09")) {
                    number_dc09.add(result);
                } else if (fimeName.contains("dc10")) {
                    number_dc10.add(result);
                }
            }
        }
        smallCarMap.put("dc09", small_dc09);
        smallCarMap.put("dc10", small_dc10);

        certifiMap.put("dc09", certifi_dc09);
        certifiMap.put("dc10", certifi_dc10);

        limitNumMap.put("dc09", number_dc09);
        limitNumMap.put("dc10", number_dc10);
//        for (Map.Entry<String, List<List<Point2D.Double>>> entry : smallCarMap.entrySet()) {
//            System.out.println(entry.getKey() + ": ");
//            List<List<Point2D.Double>> p = entry.getValue();
//            for (List<Point2D.Double> arr : p) {
//                System.out.println(arr);
//            }
//        }
        System.out.println("dc09 count is  " + certifiMap.get("dc09").size());
        getRestrictNumber();
    }

    public static List<List<Point2D.Double>> getSmallLimit(String zoneId) {
        if (!smallCarMap.containsKey(zoneId)) {
            logger.info("zoneid is not exist in smallCarMap" + zoneId);
            return null;
        }
        return smallCarMap.get(zoneId);
    }

    public static List<List<Point2D.Double>> getNumberLimit(String zoneId) {
        if (!limitNumMap.containsKey(zoneId)) {
            logger.info("zoneid is not exist in limitNumMap" + zoneId);
            return null;
        }
        return limitNumMap.get(zoneId);
    }

    public static List<List<Point2D.Double>> getcertifiLimit(String zoneId) {
        if (!certifiMap.containsKey(zoneId)) {
            logger.info("zoneid is not exist in certifiMap" + zoneId);
            return null;
        }
        return certifiMap.get(zoneId);
    }

    public static Map<Integer, String> getrestrictNumber(String zoneId) {
        if (!restrictNumber.containsKey(zoneId)) {
            logger.info("zoneid is not exist in restrictNumber" + zoneId);
            return null;
        }
        return restrictNumber.get(zoneId);
    }

    public static Point2D.Double warehousePosition(String zone) {
        zone = zone.toLowerCase();

        if (zone.equals("dc09")) {
            return new Point2D.Double(117.005456, 39.11394);
        } else if (zone.equals("dc10")) {
            return new Point2D.Double(116.522884, 40.003638);
        } else if (zone.equals("dc55")) {
            return new Point2D.Double(120.308824, 30.330043);
        } else if (zone.equals("dc59-1")) {
            return new Point2D.Double(120.308724, 30.331043);
        } else if (zone.equals("dc40")) {
            return new Point2D.Double(120.308824, 30.330043);
        } else {
            logger.info("zoneid is error, no find Warehpuse position" + zone);
            return null;
        }
    }


    //读取不能晚上拉货的司机
    public static void getNotSent() {
//        List<String> noDaySent = new ArrayList<String>();
        String fileName = "./day_sent_driver.txt";
        File file = new File(fileName);
        BufferedReader read = null;
        try {
            read = new BufferedReader(new FileReader(file));
            String readInfo = null;
            while ((readInfo = read.readLine()) != null) {
                noDaySent.add(readInfo);
            }

        } catch (Exception e) {
            logger.info("read file data is error " + e.toString());
        }
        logger.info("no day sent size is " + noDaySent.size());
    }
    public static List<String> getNotSentDriverIdList(){
        return noDaySent;
    }


}
