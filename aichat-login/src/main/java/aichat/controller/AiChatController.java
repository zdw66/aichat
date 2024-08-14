package aichat.controller;


import aichat.redis.RedisUtil;
import aichat.utils.JwtUtil;
import aichat.utils.Result;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/aicaht")
@Slf4j
public class AiChatController {

    private static final String LOGIN_PREFIX = "loginCode";
    private static final String CACHE_KEY_SEPARATOR=".";

    private static final String GROUP_ID = "1810541336101667371";
    private static final String API_KEY = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJHcm91cE5hbWUiOiLlkajlpKfkvJ8iLCJVc2VyTmFtZSI6IuWRqOWkp-S8nyIsIkFjY291bnQiOiIiLCJTdWJqZWN0SUQiOiIxODEwNTQxMzM2MTEwMDU1OTc5IiwiUGhvbmUiOiIxNTMzMzQzNzIwNCIsIkdyb3VwSUQiOiIxODEwNTQxMzM2MTAxNjY3MzcxIiwiUGFnZU5hbWUiOiIiLCJNYWlsIjoiIiwiQ3JlYXRlVGltZSI6IjIwMjQtMDgtMDEgMjE6MDc6NDIiLCJpc3MiOiJtaW5pbWF4In0.VbEugvYEii4W70zg8dBrFhnd9o9WPUSVD4v5W5zSRnNsG7VqztHwwe9gR-CUo5u8OySTWNZkq1iUADL2l0P0HVl7pi3mrXD29m_KhTXlYmoWDG6MNENZAP4pHGX_F6pf-cditImuZipsbv1SDkXjyMKBP6JHUZt3tzjsxw3wGTWMXp0XJwPhzNFBRAMU9H0QGONtToW0HI3oqUcep3FUua3adUVEi5FnWFRuLncG9WWltJF_538p2cNzM8iYq8IJYGTI2nM8DrJZq0mX9XLJQ6_MUay9H475CqB2TjlL7FycAQM44dXGvYWfFXaHaP-cnh5x1kZ9HrV0TrUIK4SbmQ";

    private static JSONArray messages = new JSONArray();
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private JwtUtil jwtUtil;
    @PostMapping("/login")
    public Result<Boolean> login(@RequestParam String numKey, HttpServletResponse response) {
        // 在这里添加用户验证逻辑
        log.info("numKey:{}",numKey);
        String key = buildKey(LOGIN_PREFIX,numKey);
        if(!redisUtil.exist(key))return Result.fail("验证码过期");
        String id = redisUtil.get(key);
        String jwt = jwtUtil.createJWT(id);
        JwtUtil.setJwtCookie(response, jwt);
        return Result.ok(true);
    }

    @PostMapping("/validate")
    public Result<String> validateJwt(@RequestParam String message,HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    try {
                        Claims claims = jwtUtil.validateJWT(token);
                        String sender_name = claims.getSubject();
                        String url = "https://api.minimax.chat/v1/text/chatcompletion_pro?GroupId=" + GROUP_ID;
                        String apiKey = API_KEY;
                        // Prepare the request body
                        JSONObject requestBody = new JSONObject();
                        requestBody.put("model", "abab6.5-chat");
                        requestBody.put("tokens_to_generate", 1024);
                        requestBody.put("reply_constraints", new JSONObject().put("sender_type", "BOT").put("sender_name", "智能聊天助手"));

                        JSONObject userMessage = new JSONObject();
                        userMessage.put("sender_type", "USER");
                        userMessage.put("sender_name", sender_name);
                        userMessage.put("text", message);
                        messages.put(userMessage);
                        requestBody.put("messages", messages);

                        JSONArray botSettings = new JSONArray();
                        JSONObject botSetting = new JSONObject();
                        botSetting.put("bot_name", "智能聊天助手");
                        botSetting.put("content", "智能聊天给您的生活带来便利！");
                        botSettings.put(botSetting);
                        requestBody.put("bot_setting", botSettings);

                        // Send HTTP POST request
                        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setDoOutput(true);

                        OutputStream os = connection.getOutputStream();
                        os.write(requestBody.toString().getBytes());
                        os.flush();
                        os.close();

                        // Read response
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        JSONObject responseJson = new JSONObject(response.toString());
                        String reply = responseJson.getString("reply");
                        JSONArray choicesMessages = responseJson.getJSONArray("choices").getJSONObject(0).getJSONArray("messages");
                        for (int i = 0; i < choicesMessages.length(); i++) {
                            messages.put(choicesMessages.getJSONObject(i));
                        }
                        return Result.ok(reply);
                    } catch (Exception e) {
                        return Result.fail("Invalid JWT");
                    }
                }
            }
        }
        return Result.fail("No JWT found");
    }

    public String buildKey(String...strObjs){
        return Stream.of(strObjs).collect(Collectors.joining(CACHE_KEY_SEPARATOR));
    }
}
