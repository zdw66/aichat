package com.aichat.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageUtil {

    public static Map<String,String> parseXml(final String msg){
        //将解析结果存储在HasMap中
        Map<String,String>map=new HashMap<>();

        //从request中取得输入流
        try(InputStream inputStream = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8))){
            //读取输入流
            SAXReader reader =  new SAXReader();
            Document document = reader.read(inputStream);
            //得到xml根元素
            Element root = document.getRootElement();
            //得到根元素的所有子节点
            List<Element> elementList = root.elements();

            //遍历所有节点
            for(Element e:elementList){
                map.put(e.getName(),e.getText());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return map;
    }
}
