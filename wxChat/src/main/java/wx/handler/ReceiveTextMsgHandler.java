package wx.handler;



import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wx.redis.RedisUtil;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ReceiveTextMsgHandler implements WxChatMsgHandler{

    private static final String LOGIN_PREFIX = "loginCode";

    private static Set<String> UserNmae= new HashSet<>();
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_TIME = 5 * 60 * 1000; // 5分钟 (以毫秒为单位)
    private static Set<String> generatedCodes = new HashSet<>();
    private static long codeGenerationTime = 0;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public WxChatMsgTypeEnum getMsgType() {
        return WxChatMsgTypeEnum.TEXT_MSG;
    }

    @Override
    public String dealMsg(Map<String, String> msgMap) {

        //规则判断
        String content = msgMap.get("Content");
        String toUserName = msgMap.get("ToUserName");
        String fromUserName = msgMap.get("FromUserName");
        String numContent="";
        boolean check=true;
        if("视频爬虫".equals(content)){
            numContent="源码链接为：https://gitee.com/zhoudawei666/my_null.git";
        } else if("会员卡系统".equals(content)){
            numContent="源码链接为：https://gitee.com/zhoudawei666/card.git";
        }else if("投票系统".equals(content)){
            numContent="源码链接为：https://gitee.com/zhoudawei666/my_vue_first_old.git";
        }else if("帮助".equals(content)){
            numContent="\uD83C\uDF89可回复以下关键字获取帮助\uD83C\uDF89\n" +
                    "【验证码】可登录MYCLUB社区网站。\n" +
                    "【视频爬虫】获取视频爬虫源码链接。\n" +
                    "【会员卡系统】获取超市会员管理系统源码链接。\n" +
                    "【投票系统】获取在线投系统源码链接。" ;
        }else if("验证码".equals(content)){

            String uniqueCode = generateUniqueCode();
            numContent="您当前的验证码是："+uniqueCode+"(五分钟内有效)";

            String numKey = redisUtil.buildKey(LOGIN_PREFIX,uniqueCode);
            redisUtil.setNx(numKey,fromUserName, 5L, TimeUnit.MINUTES);

        }else if("登录".equals(content)){
            if(!UserNmae.contains(fromUserName)){
                UserNmae.add(fromUserName);
                try {
                    ServerSocket serverSocket = new ServerSocket(5555);
                    System.out.println("Server started, waiting for client...");

                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected");

                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String message = fromUserName;
                    System.out.println("Received message to client: " + message);

                    String response = fromUserName;
                    out.println(response);

                    clientSocket.close();
                    serverSocket.close();
                    UserNmae.remove(fromUserName);
                } catch (IOException e) {
                    e.printStackTrace();
                    numContent="登录失败！";
                }
            }
            numContent="登录成功！";
        }
        else{
            numContent="\uD83C\uDF89回复【帮助】试试新功能吧！";
        }
        String replyContent="<xml>\n" +
                "  <ToUserName><![CDATA["+fromUserName+"]]></ToUserName>\n" +
                "  <FromUserName><![CDATA["+toUserName+"]]></FromUserName>\n" +
                "  <CreateTime>12345678</CreateTime>\n" +
                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                "  <Content><![CDATA["+numContent+"]]></Content>\n" +
                "</xml>";

        return replyContent;
    }
    public static String generateUniqueCode() {
        Random random = new Random();
        String code;

        // 确保生成的验证码唯一且在有效期内
        do {
            code = String.format("%06d", random.nextInt(1000000)); // 随机生成6位数
        } while (generatedCodes.contains(code) || !isCodeValid());

        // 添加到已生成集并记录生成时间
        generatedCodes.add(code);
        codeGenerationTime = System.currentTimeMillis();

        // 清理过期的验证码
        cleanUpExpiredCodes();

        return code;
    }

    private static boolean isCodeValid() {
        return System.currentTimeMillis() - codeGenerationTime < EXPIRY_TIME;
    }

    private static void cleanUpExpiredCodes() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - codeGenerationTime >= EXPIRY_TIME) {
            generatedCodes.clear(); // 清空过期的验证码
            codeGenerationTime = currentTime; // 更新最后生成时间
        }
    }
}
