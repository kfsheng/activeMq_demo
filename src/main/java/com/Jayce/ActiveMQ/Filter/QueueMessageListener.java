package com.Jayce.ActiveMQ.Filter;

import com.Jayce.Redis.Utils.HttpClientUtil;
import com.sun.deploy.net.HttpUtils;
import org.apache.http.HttpException;
import org.apache.http.client.utils.HttpClientUtils;
import sun.net.www.http.HttpClient;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/5.
 */
public class QueueMessageListener implements MessageListener {
    public void onMessage(Message message) {
        TextMessage tm = (TextMessage) message;
        try {
            System.out.println("QueueMessageListener监听到了文本消息：\t"
                    + tm.getText());
            String url = "http://localhost:8081/loanCallBack/testMqAsyc";
            Map<String,String> map = new HashMap<String,String>();
            map.put("code","1");
            String result = HttpClientUtil.doPost(url,map);
            System.out.println("推送返回参数"+result);
            //do something ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]){
        String url = "http://localhost:8081/loanCallBack/testMqAsyc";
        Map<String,String> map = new HashMap<String,String>();
        map.put("code","1");
        try {
            String result = HttpClientUtil.doPost(url,map);
            System.out.println("推送返回参数"+result);
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }
}
