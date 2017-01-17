package com.lsh.strategy.handler;


import com.facebook.fb303.fb_status;
import com.lsh.service.driver.recommend.*;
import com.lsh.strategy.helper.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by zhengxuefeng on 2016/11/7.
 */
public class RecommendHandler implements RecommendService.Iface {

    private static Logger logger = LoggerFactory.getLogger(RecommendHandler.class);
    //@Autowired
    //private AddressItem addressItem;
    @Autowired
    private CalComeTime calComeTime;

    @Autowired
    private DriverRank driverRank;

    @Autowired
    private GetData getData;

    @Autowired
    private LimitJudge limitJudge;

    public void init() {

    }

    public void close() {
        logger.debug("RecommendHandler close() is called ");
    }


    //返回服务 司机列表  顺序为  可选司机 时间限制司机 特殊条件司机 绑点司机  不满足硬性条件司机
    public OutputDriverRec finalResult(HashMap<String, DriverItem> recordInfo, List<String> driverSort,
                                       List<String> noTimeSatisfyDriverIdList, List<String> beFilteredDriverIdList,
                                       List<String> boundLineDriverIdList, List<String> notMeetLimitDRiverIdList, String lineNumber, String boundLineDriverId) {

        OutputDriverRec recommendDriverId = new OutputDriverRec();
        List<DriverItem> recResult = new ArrayList<DriverItem>();
        for (String driverId : driverSort) {
            if (recordInfo.containsKey(driverId) && (!recResult.contains(recordInfo.get(driverId))))
                recResult.add(recordInfo.get(driverId));
        }
        for (String driverId : beFilteredDriverIdList) {
            if (recordInfo.containsKey(driverId) && (!recResult.contains(recordInfo.get(driverId))))
                recResult.add(recordInfo.get(driverId));
        }
        for (String driverId : noTimeSatisfyDriverIdList) {
            if (recordInfo.containsKey(driverId) && (!recResult.contains(recordInfo.get(driverId))))
                recResult.add(recordInfo.get(driverId));
        }

        for (String driverId : boundLineDriverIdList) {
            if (recordInfo.containsKey(driverId) && (!recResult.contains(recordInfo.get(driverId))))
                recResult.add(recordInfo.get(driverId));
        }
        for (String driverId : notMeetLimitDRiverIdList) {
            if (recordInfo.containsKey(driverId) && (!recResult.contains(recordInfo.get(driverId))))
                recResult.add(recordInfo.get(driverId));
        }
        logger.info("return driver list is " + recResult.size());
        logger.info("available 可选司机个数为：" + driverSort.size());
        logger.info("lineNumber " + lineNumber + " available : " + driverSort);
//        for (String driverId : driverSort) {
//            logger.info("available driverId : " + driverId);
//        }
        logger.info("specialLimit 被特殊条件过滤的司机个数为：" + beFilteredDriverIdList.size());
        logger.info("lineNumber " + lineNumber + " specialLimit : " + beFilteredDriverIdList);
//        for (String driverId : beFilteredDriverIdList) {
//            logger.info("specialLimit 被特殊条件过滤 : " + driverId);
//        }
        logger.info("timeLimit 被时间过滤的司机个数为：" + noTimeSatisfyDriverIdList.size());
        logger.info("lineNumber " + lineNumber + " timeLimit : " +noTimeSatisfyDriverIdList);
//        for (String driverId : noTimeSatisfyDriverIdList) {
//            logger.info("timeLimit 被时间过滤 : " + driverId);
//        }
        logger.info("boundLine 已经绑定线路司机个数为：" + boundLineDriverIdList.size());
        logger.info("lineNumber " + lineNumber + " boundLine : " + boundLineDriverIdList);
//        for (String driverId : boundLineDriverIdList) {
//            logger.info("boundLine 已经绑定线路 : " + driverId);
//        }
        logger.info("notMeet 被硬性条件过滤的司机个数为：" + notMeetLimitDRiverIdList.size());
        logger.info("lineNumber " + lineNumber + " notMeet : " + notMeetLimitDRiverIdList);
//        for (String driverId : notMeetLimitDRiverIdList) {
//            logger.info("notMeet 被硬性条件过滤 : " + driverId);
//        }
        recommendDriverId.setRecommendDriverList(recResult);

        //查看绑定司机排在第几位
        Integer rank = 1;
        boolean flag = false;
        String boundDriverName = getData.readDriverName(boundLineDriverId);
        for (DriverItem item : recResult){
            if (item.getDriverId().equals(boundLineDriverId)){
                logger.info("boundline driver rank " + lineNumber + ":" + rank);
                logger.info("boundline driver name is : " + boundDriverName);
                flag = true;
            }
            rank ++;
        }
        if (!flag) logger.info("no find bound driver ");
        return recommendDriverId;

    }

    @Override
    public OutputDriverRec driverRecommend(InputDriverRec req) throws TException {
        long start = System.currentTimeMillis();
        if (req == null) {
            logger.info("input is null");
            return null;
        }

        Double sumLng = 0.0;
        Double sumLat = 0.0;
        logger.info("inputreqis : " + req.getDriverList());
        String boundLineDriverId = req.getBoundLineDriverId();
        if (boundLineDriverId != null)
            logger.info("bound line driverId is : " + boundLineDriverId);
        else logger.info("no bound line driverId");
        if (req.getLineNumber() != null)
            logger.info("line number is : " + req.getLineNumber());
        else logger.info("no line number");
        List<AddressItem> addressInfo = req.getAddressList();
        List<String> addressIdList = new ArrayList<String>();//只存放地址id  用作司机熟悉度的判断
        Integer addressCount = 0;
        for (AddressItem item : addressInfo) {
            addressIdList.add(item.getAddressId());
            sumLng += Double.valueOf(item.getLng());
            sumLat += Double.valueOf(item.getLat());
            addressCount += 1;
        }
        //求出线路的中心位置
        Point2D.Double linePoint = new Point2D.Double(sumLng / addressCount, sumLat / addressCount);
        logger.info("线路中间点位置为：" + linePoint);

        List<String> boundLineDriverIdList = new ArrayList<>();//传进来司机中绑定司机列表
        List<String> notMeetLimitDRiverIdList = new ArrayList<String>();//不满足硬性条件的司机ID list
        List<String> noTimeSatisfyDriverIdList = new ArrayList<String>();//不满足时间限制的司机
        List<String> beFilteredDriverIdList = new ArrayList<String>();//被特殊条件过滤掉的司机
        List<String> driverSort = new ArrayList<String>();//最后排序后司机的列表
        List<String> allboundLineDriverIdList = limitJudge.removeRepeatDriver();//一天内所有绑定线路的司机ID列表


        // 排序的时候只需要知道司机的id就可以了， 与司机id存在map中  服务返回时需要知道司机的其他信息  这个时候就可以根据司机id得到了
        //把已经绑定线路的司机去掉
        //这里  recordinfo里买呢对于这三辆特殊的车  记录的是小车  而 noboundLineDriverItemList里面记录的是大车
        List<DriverItem> driverItemList = req.getDriverList();
        logger.info("input driver list size is :" + driverItemList.size());
        HashMap<String, DriverItem> recordInfo = new HashMap<String, DriverItem>();//记录司机相关信息
        List<DriverItem> noboundLineDriverItemList = new ArrayList<DriverItem>();//

//        Date now = new Date();
//        Integer nowHour = now.getHours();

        List<String> noBoundLineDriverIdList = new ArrayList<String>();//没有绑定线路的司机ID list
        for (DriverItem item : driverItemList) {
            recordInfo.put(item.getDriverId(), item);
            //判断特殊司机 这三个司机只能白天送
//            if (nowHour > 10 && nowHour < 19) {
//                if (item.getDriverId().equals("2238759565034500935")) {
//                    beFilteredDriverIdList.add("2238759565034500935");
//                    continue;
//                } else if (item.getDriverId().equals("8469911886884912356")) {
//                    beFilteredDriverIdList.add("8469911886884912356");
//                    continue;
//                }
//            }
            if (allboundLineDriverIdList.contains(item.getDriverId()) && (!item.getDriverId().equals(boundLineDriverId))) {
                boundLineDriverIdList.add(item.getDriverId());
                continue;
            }
            if (item.getDriverId().equals("218472916299087279")) {
                item.setCarType(1);
            }
            if (item.getDriverId().equals("1730556454450908558")) {
                item.setCarType(1);
            }
            if (item.getDriverId().equals("7224384990447067144")) {
                item.setCarType(1);
            }
            noBoundLineDriverIdList.add(item.getDriverId());
            noboundLineDriverItemList.add(item);

        }
        logger.info("bound line driver size is : " + boundLineDriverIdList.size());
        if (noboundLineDriverItemList.size() == 0) {
            logger.info("all driver has bounded line, so no satisfy driver");
            return finalResult(recordInfo, driverSort, noTimeSatisfyDriverIdList, beFilteredDriverIdList, boundLineDriverIdList, notMeetLimitDRiverIdList, req.getLineNumber(), boundLineDriverId);
        }
        logger.info("no bound line driver list size is : " + noboundLineDriverItemList.size());
//        req.setDriverList(noboundLineDriverItemList);

        logger.info("ware position : " + req.getWarehouse().toLowerCase());

        //根据筛选条件  筛选出满足条件的司机id  list
        List<String> driverIdList = LimitJudge.filterDriver(noboundLineDriverItemList, req.getWarehouse().toLowerCase(), req.getAddressList(), req.getTotalBoxCount());
        logger.info("driverIdList size is : " + driverIdList.size());

        //收集被硬性条件过滤掉的司机
        for (String driverId : noBoundLineDriverIdList) {
            if (!driverIdList.contains(driverId)) {
                notMeetLimitDRiverIdList.add(driverId);
            }
        }


        if (driverIdList.size() == 0) {
            logger.info("limit judge cause no driver is satisfy");
            return finalResult(recordInfo, driverSort, noTimeSatisfyDriverIdList, beFilteredDriverIdList, boundLineDriverIdList, notMeetLimitDRiverIdList, req.getLineNumber(), boundLineDriverId);
        }
        //特殊司机处理  如果被过滤  放在被特殊条件过滤的司机列表里面
        List<String> noSpecialDriver = new ArrayList<String>();
        if (req.getWarehouse().toLowerCase().contains("09")) {
            if (req.getTotalBoxCount() > 240)
                for (String driverId : driverIdList) {
                    if (driverId.equals("218472916299087279")) {
                        beFilteredDriverIdList.add("218472916299087279");
                        continue;
                    } else if (driverId.equals("1730556454450908558")) {
                        beFilteredDriverIdList.add("1730556454450908558");
                        continue;
                    } else if (driverId.equals("7224384990447067144")) {
                        beFilteredDriverIdList.add("7224384990447067144");
                        continue;
                    }
                    noSpecialDriver.add(driverId);
                }
        }
        logger.info("driverId size is : " + driverIdList.size());
        if (noSpecialDriver.size() > 0)
            driverIdList = noSpecialDriver;
        logger.info("driverId size is : " + driverIdList.size());
        logger.info("noSpecialDriver size is : " + noSpecialDriver.size());
        //如果里面有小车  则优先
        List<String> smallCarList = new ArrayList<String>();
        for (String driverId : driverIdList) {
            if (recordInfo.get(driverId).getCarType() == 2) {
                smallCarList.add(driverId);
            } else {
                beFilteredDriverIdList.add(driverId);
            }
        }
        if (smallCarList.size() > 0) {
            logger.info("存在小车司机");
            for (String driverId : smallCarList){
                logger.info("小车司机 : " + driverId);
            }
            driverIdList = smallCarList;
        }else {
            beFilteredDriverIdList.clear();
        }
        logger.info("driverId size is : " + driverIdList.size());
        logger.info("smallCarList size is : " + smallCarList.size());
        int timeLimit_minute = 500;//  时间限制
        //计算出每个司机回来的时间   并筛选出回来时间满足的司机可以推荐
        List<String> TimeSatisfyDriverIdList = calComeTime.comeback(driverIdList, req.getWarehouse().toLowerCase(), timeLimit_minute);
        if (TimeSatisfyDriverIdList.size() == 0) {
            logger.info("time limit cause not driver is satisfy");
//            TimeSatisfyDriverIdList = driverIdList;
            for (String driverId : driverIdList) {
                TimeSatisfyDriverIdList.add(driverId);
            }
        }
        //被时间过滤掉的司机存在对应的list中
        for (String driverId : driverIdList) {
            if (!TimeSatisfyDriverIdList.contains(driverId)) {
                noTimeSatisfyDriverIdList.add(driverId);
            }
        }


        logger.info("timeStatisftDriverIdList size is : " + TimeSatisfyDriverIdList.size());
        HashMap<String, Integer> driverOrderCount = getData.getOrderCount();
        if (driverOrderCount.size() == 0) {
            logger.info("count driver order redis is null");
        }

        //对每个司机优先级排序
        driverSort = driverRank.countWalk(TimeSatisfyDriverIdList, addressIdList, driverOrderCount, req.warehouse.toLowerCase(), linePoint);
        logger.info("rank driver size is : " + driverSort.size());
//        OutputDriverRec recommendDriverId = new OutputDriverRec();

//        int count = driverSort.size();
//        if (count == 0) {
////            logger.info("no driver recommend");
////            List<DriverItem> nullDriverList = new ArrayList<DriverItem>();
//            recommendDriverId.setRecommendDriverList(nullDriverList);
//            return recommendDriverId;
//        }

//        List<DriverItem> recResult = new ArrayList<DriverItem>();
//        for (String driverId : driverSort) {
////            DriverItem flag = new DriverItem();
////            flag.setDriverId(item);
////            if (recordInfo.get(item).contains(1))
////                flag.setCarType(Integer.valueOf(recordInfo.get(item).get(1).toString()));
////            if (recordInfo.get(item).contains(2))
////                flag.setCarnumber(recordInfo.get(2).toString());
////            if (recordInfo.get(item).contains(3))
////                flag.setHas_certificate(Integer.valueOf(recordInfo.get(3).toString()));
////            if (recordInfo.get(item).contains(4))
////                flag.setHas_wrap(Integer.valueOf(recordInfo.get(4).toString()));
//            if (recordInfo.containsKey(driverId))
//                recResult.add(recordInfo.get(driverId));
//        }
//
//        recommendDriverId.setRecommendDriverList(recResult);
//        logger.info("get satisfy driver");
        long end = System.currentTimeMillis();
        logger.info("Time spent in service is " + (end - start));
        return finalResult(recordInfo, driverSort, noTimeSatisfyDriverIdList, beFilteredDriverIdList, boundLineDriverIdList, notMeetLimitDRiverIdList, req.getLineNumber(), boundLineDriverId);
    }

    @Override
    public String getName() throws TException {
        return null;
    }

    @Override
    public String getVersion() throws TException {
        return null;
    }

    @Override
    public fb_status getStatus() throws TException {
        return null;
    }

    @Override
    public String getStatusDetails() throws TException {
        return null;
    }

    @Override
    public Map<String, Long> getCounters() throws TException {
        return null;
    }

    @Override
    public long getCounter(String s) throws TException {
        return 0;
    }

    @Override
    public void setOption(String s, String s1) throws TException {

    }

    @Override
    public String getOption(String s) throws TException {
        return null;
    }

    @Override
    public Map<String, String> getOptions() throws TException {
        return null;
    }

    @Override
    public String getCpuProfile(int i) throws TException {
        return null;
    }

    @Override
    public long aliveSince() throws TException {
        return 0;
    }

    @Override
    public void reinitialize() throws TException {

    }

    @Override
    public void shutdown() throws TException {

    }

}
