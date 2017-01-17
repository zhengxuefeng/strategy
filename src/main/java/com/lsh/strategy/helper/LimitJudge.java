package com.lsh.strategy.helper;

import com.lsh.service.driver.recommend.AddressItem;
import com.lsh.service.driver.recommend.DriverItem;
import com.lsh.strategy.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengxuefeng on 2016/12/1.
 */
public class LimitJudge {

    private static Logger logger = LoggerFactory.getLogger(LimitJudge.class);

    @Autowired
    private GetData getData;

    //得到绑定线路的司机
    public List<String> removeRepeatDriver(){
        List<Object[]> result = getData.getDriver();
        List<String> boundLineDriverIdList = new ArrayList<String>();
        for (Object[] arr : result){
            if (arr[0] == null) continue;
            String driverId = arr[0].toString();
//            logger.info("driverId : " + driverId);
//            if (driverId !=null)
            boundLineDriverIdList.add(driverId);
        }
        logger.info("one day bound line driver list size is : " + boundLineDriverIdList.size());
        return boundLineDriverIdList;
    }



    //总箱数是否大于172箱
    public static boolean judgeTotalBox(Integer totalBox) {
        logger.info("total box count is : " + totalBox);
        if (totalBox > 180) return true;
    return false;
}

    //线路中是否有限行的点
    public static boolean judgeRestrictRegion(List<AddressItem> addressItemList) {
        for (AddressItem item : addressItemList) {
            if (item.getRestrictNo() == 1) return true;
        }
        return false;
    }

    //判断一个区域集合 是否有区域包含这条线路中的点
    public static boolean specialRegion(List<List<Point2D.Double>> pointCircleList, List<AddressItem> addressItemList) {

        for (List<Point2D.Double> pointCircle : pointCircleList) {
//            logger.info("position list is : " + pointCircle);
            for (AddressItem item : addressItemList) {
                List<Point2D.Double> pointCircleFlag = new ArrayList<Point2D.Double>(pointCircle);
                Double lng = Double.valueOf(item.getLng());
                Double lat = Double.valueOf(item.getLat());
                Point2D.Double p = new Point2D.Double(lng, lat);
                if (Util.checkWithJdkGeneralPath(p, pointCircleFlag)) {
                    return true;
                }
//                logger.info("position list is : " + pointCircle);
            }
        }
        return false;
    }


    //过滤掉一些司机
    public static List<String> filterDriver(List<DriverItem> driverItemList, String zoneId, List<AddressItem> addressItemList, Integer totalBox) {

        long startTime = System.currentTimeMillis();

        //需要判断 限号  证件 和 小车的区域
        List<String> needJudgeZoneId = new ArrayList<>();
        needJudgeZoneId.add("dc09");
        needJudgeZoneId.add("dc10");
        zoneId = zoneId.toLowerCase();
        System.out.println(zoneId);
        List<String> driverIdList = new ArrayList<String>();

        List<List<Point2D.Double>> smallCarList = FileData.getSmallLimit(zoneId);//小车区域
        List<List<Point2D.Double>> certificateList = FileData.getcertifiLimit(zoneId);//需要证的区域
        List<List<Point2D.Double>> restrictNumberRegion = FileData.getNumberLimit(zoneId);//限号区域
        Map<Integer, String> dayRestrictNumber = FileData.getrestrictNumber(zoneId);//限号的规则
//        logger.info("smallcar list size if : " + smallCarList.size());
//        logger.info("certificateList size is : " + certificateList.size());
//        logger.info("restrictNumberRegion size is : " + restrictNumberRegion.size());
        boolean isSmall = false;
        boolean isCertificate = false;
        boolean isRestrictNumber = false;
        if (needJudgeZoneId.contains(zoneId)) {
            isSmall = specialRegion(smallCarList, addressItemList);//用来判断这条线路是否有小车区域   0 为没有  1为有
            isCertificate = specialRegion(certificateList, addressItemList);//用来判断这条线路是否需要有证的司机 0 不需要  1 需要
            isRestrictNumber = specialRegion(restrictNumberRegion, addressItemList);//用来判断这条线路是否有限号的限 0 没有  1 有
        }

        boolean isTotalBoxCount = judgeTotalBox(totalBox);//总箱数是否大于180箱
        boolean isRestrict = judgeRestrictRegion(addressItemList);//整条线路是否有限行的点
        for (DriverItem item : driverItemList) {
//            logger.info("total box count and car type : " + isTotalBoxCount  + "   " + item.getCarType());
            if (item.getCarType() == 2) {
                if (isTotalBoxCount) {
                    logger.info("boxCountLimit:" + item.getDriverId());
                    continue;//箱数大于180  去除小车司机
                }
            } else if (item.getCarType() == 1) {
                if (isRestrict) {
                    logger.info("restrict:" + item.getDriverId());
                    continue;//有限行的点  大车不能去
                }
                if (isSmall) {
                    logger.info("smallCarRegion:" + item.getDriverId());
                    continue;//有小车区域   大车不能去
                }
                //现在只有北京需要判断有没有证的司机
                if (zoneId.equals("dc10") && isCertificate && item.getHas_certificate() == 0) {
                    logger.info("certificateLimit:" + item.getDriverId());
                    continue;//需要证的地方  大车司机没有证不能去
                }

            }
            if (needJudgeZoneId.contains(zoneId)) {
                //判断司机的车是否被限号
                String carNumber = item.getCarnumber().toLowerCase();//车牌号
                char lastNumber = carNumber.charAt(carNumber.length() - 1);
                if (lastNumber >= 'a' && lastNumber <= 'z') lastNumber = '0';
                Date date = new Date();
                Integer nowTime = date.getHours();
                Integer dayWeek = Util.whichDay(date);
                if (nowTime > 10) {
                    if (dayWeek == 7) dayWeek = 1;
                    else dayWeek += 1;
                }
//                logger.info("now hour is : " + nowTime + "need sent week is : " + dayWeek);
//                logger.info("car number is : " + carNumber + "  " + "last car number is :" + lastNumber);
                if (dayRestrictNumber.containsKey(dayWeek)) {
                    if (isRestrictNumber && dayRestrictNumber.get(dayWeek).contains(String.valueOf(lastNumber))) {
                        logger.info("car number is : " + carNumber);
                        logger.info("limit number is  : " + dayRestrictNumber.get(dayWeek));
                        logger.info("carNumberLimit:" + item.getDriverId());
                        continue;//存在限号的限  而司机的车牌又是限号的  则剔除掉
                    }
                }
            }
            driverIdList.add(item.getDriverId());
        }
        long endTime = System.currentTimeMillis();
        logger.info("Time spent in LimitJudge is " + (endTime - startTime));
        return driverIdList;
    }

}
