package com.lsh.strategy.start;

import com.lsh.strategy.handler.RecommendHandler;
import com.lsh.strategy.helper.FileData;
import com.lsh.strategy.util.Util;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhengxuefeng on 2016/11/7.
 */
public class RecommendServer {

    private static Logger logger = LoggerFactory.getLogger(RecommendServer.class);
    private int port;
    private TServerSocket serverTransport;

    @Autowired
    private RecommendHandler rankImpl;

    public void setPort(int port) {
        this.port = port;
    }
    public void setRankImpl(RecommendHandler rankImpl) {
        this.rankImpl= rankImpl;
    }

    public void init() {
        try{
//            String beforeDay = "111111";
//            String currentTime = "7777777";
//            String sql = "select address_info, receipt_status, trans_uid, arrived_at from order_shipping_head where" +
//                    "created_at >= '" + beforeDay + "' and created_at <= '" + currentTime + "'";
//            System.out.println("sql语句: " + sql);

//
//
//
//             Date date = new Date();
//            System.out.println(date.getHours());
//            long start = System.currentTimeMillis();
           // System.out.println(start / 1000);
//            System.out.println("TTTTTTTTTTTT :" +  start);
//            if (Long.toString(start).compareTo("1477756800") > 0) System.out.println("more");
            FileData.getFileData();
            FileData.getNotSent();
            serverTransport = new TServerSocket(port);
            TMultiplexedProcessor mprocessor = new TMultiplexedProcessor();
            Class<?>  serviceClass = rankImpl.getClass();
            // 获取实现类接口
            Class<?> [] interfaces = serviceClass.getInterfaces();

            TProcessor processor = null;
            String serviceName = null;
            for(Class<?> clazz:interfaces){
                System.out.println("ThriftServer=========" + clazz.getSimpleName());
                String className = clazz.getEnclosingClass().getSimpleName();
                serviceName = clazz.getEnclosingClass().getName();
                System.out.println("serviceName=========" + serviceName + " " + className);
                String pname = serviceName + "$Processor";
                try {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    Class<?> pclass = classLoader.loadClass(pname);
                    if (!TProcessor.class.isAssignableFrom(pclass)) {
                        continue;
                    }
                    Constructor<?> constructor = pclass.getConstructor(clazz);
                    processor = (TProcessor) constructor.newInstance(rankImpl);
                    System.out.println("processor=========" + processor.getClass().getSimpleName());
                    mprocessor.registerProcessor(className, processor);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (processor == null) {
                throw new IllegalClassFormatException("service-class should implements Iface");
            }
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(
                    serverTransport).processor(mprocessor));
            System.out.println("Starting server on port " + port + "......");
            server.serve();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void close() {
        logger.info("DriverRecommend Server closed");
    }

    public static void main(String[] args) {
        try {
            ApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:ApplicationContext.xml");
            //RecommendTest p = appContext.getBean(RecommendTest.class);
            ((ClassPathXmlApplicationContext) appContext).close();
            logger.info("exit.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
