package com.yannqing.mackradio.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //调用
     List<String> res = getPicture("在一个遥远的未来，宇宙中有一个被繁星环绕的星球，名为赛伦迪尔。赛伦迪尔星球上，有一座悬浮在空中的奇迹之城——天穹城。这座城市以其高科技和魔法的完美结合而闻名，居民们生活在和谐与繁荣之中。\n" +
             "\n" +
             "天穹城中，有一个古老的传说，讲述的是一位名为星辰守护者的神秘人物。据说，每当城市面临危机，星辰守护者就会出现，以他那控制星辰力量的能力，保护城市免受灾难。\n" +
             "\n" +
             "然而，随着时间的流逝，这个传说逐渐被人遗忘。直到有一天，一个名为莉亚的少女突然出现在天穹城。她拥有一双能够看穿宇宙奥秘的眼睛，以及一种能够与星辰对话的神奇能力。莉亚的到来，似乎预示着一场前所未有的风暴即将来临。\n" +
             "\n" +
             "果不其然，就在莉亚抵达天穹城不久，一场宇宙风暴悄然逼近。风暴中，一股黑暗力量从裂缝中涌出，它的目标直指天穹城的核心能源——星辰之心。\n" +
             "\n" +
             "城市中的居民们陷入了恐慌，但莉亚却显得异常平静。她站在城市的最高点，凝视着那股黑暗力量，开始吟唱古老的星辰咒语。随着咒语的进行，天空中的星辰开始闪烁，一束束星光汇聚成一道道光流，流向莉亚的双手。\n" +
             "\n" +
             "当黑暗力量降临时，莉亚释放了所有汇聚的星光，与黑暗力量展开了激烈的碰撞。光芒与黑暗在空中交织，整个天穹城都被这场宇宙级的对决所震撼。最终，在莉亚的坚持与星辰力量的帮助下，黑暗力量被击退，宇宙风暴也渐渐平息。\n" +
             "\n" +
             "莉亚不仅拯救了天穹城，也成为了新一代的星辰守护者。她的事迹迅速传遍了整个星球，成为了新的传说。而每当夜幕降临，星辰闪烁之时，人们都会仰望星空，感谢那位守护着他们的星辰守护者。\n" +
             "\n" +
             "从此，莉亚与天穹城的居民们一同维护着城市的和平与繁荣，而星辰之心也成为了城市永恒的守护之源。\n");
        System.out.println();
    }

    /**
     * 获取图片（coze）
     * @param content
     * @return
     */
    private static List<String> getPicture(String content) {
        // Replace with your Personal_Access_Token, Bot_Id, and UserId
        String personalAccessToken = "pat_GBSt86m4MdawUCdecLg4Z00crnLzY8U0bhTBFoMCZK6WkMGAA6f30W1CYh95l9fD";
        String botId = "7366443219188170804";
        String userId = "7345861690204536843";
        String yourQuery = content;

        // Create JSON body
        JSONObject message = new JSONObject()
                .set("role", "user")
                .set("content", yourQuery)
                .set("content_type", "text");

        JSONArray messagesArray = new JSONArray().put(message);

        JSONObject requestBody = new JSONObject()
                .set("bot_id", botId)
                .set("user_id", userId)
                .set("stream", false)
                .set("auto_save_history", true)  // Set auto_save_history to true
                .set("additional_messages", messagesArray);

        // Create and send POST request
        HttpRequest request = HttpRequest.post("https://api.coze.cn/v3/chat")
                .header("Authorization", "Bearer " + personalAccessToken)
                .header("Content-Type", "application/json")
                .body(requestBody.toString());

        // Send request and get response
        HttpResponse response = request.execute();

        // Ensure the response uses UTF-8 encoding
        response.charset("UTF-8");

        // Parse and handle the response
        String conversationId = ""; // conversationid
        String chatId = ""; //chatId
        try {
            JSONObject jsonResponse = new JSONObject(response.body());
            int responseCode = jsonResponse.getInt("code");
            JSONObject data = jsonResponse.getJSONObject("data");

            if (responseCode == 0) {
                System.out.println("Request is being processed.");
                chatId = data.getStr("id");
                conversationId = data.getStr("conversation_id");
                String status = data.getStr("status");

            } else {
                System.out.println("An error occurred:");
                System.out.println("Code: " + responseCode);
                System.out.println("Message: " + jsonResponse.getStr("msg"));
            }
        } catch (Exception e) {
            System.out.println("Failed to parse JSON response.");
            System.out.println("Raw response: " + response.body());
        }

//        Thread.sleep(10000);
        List pictureList = new ArrayList<>(); //存放图片地址
        //轮询获取内容响应
        pollForImages(conversationId, chatId, personalAccessToken, pictureList);

        System.out.println(pictureList);
        return pictureList;
    }

    /**
     * 轮询查询内容生成
     * @param conversationId
     * @param chatId
     * @param personalAccessToken
     * @param pictureList
     */
    private static void pollForImages(String conversationId, String chatId, String personalAccessToken,List pictureList)  {
        String url = "https://api.coze.cn/v3/chat/message/list?conversation_id=" + conversationId + "&chat_id=" + chatId;
        HttpRequest getRequest = HttpRequest.get(url)
                .header("Authorization", "Bearer " + personalAccessToken)
                .header("Content-Type", "application/json");

        boolean finished = false;
        while (!finished) {
            try {
                HttpResponse getResponse = getRequest.execute();
                getResponse.charset("UTF-8");
                String responseBody = getResponse.body();

                // 解析响应内容
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.getInt("code") == 0) {
                    JSONArray data = jsonResponse.getJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            JSONObject message = data.getJSONObject(i);
                            if (message.containsKey("content")) {
                                String content = message.getStr("content");
                                // 检查内容中是否包含图片地址
                                if (content.contains("[Image")) {
                                    // 提取图片地址
                                    extractImageUrls(content,pictureList);
                                    finished = true; //轮询结束标识
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Error occurred: " + jsonResponse.getStr("msg"));
                    finished = true; // 如果发生错误，结束轮询
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // 等待一段时间后重试，例如等待5秒
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                finished = true;
            }
        }
    }

    /**
     * 从给定的 JSON 字符串中提取图片 URL。
     * @param jsonString 包含 content 的 JSON 字符串
     * @return 提取出的图片 URL 列表
     */
    public static void extractImageUrls(String jsonString,List pictureList)  {
        // 正则表达式，用于匹配 [Image] 标识符后的 URL
        Pattern pattern = Pattern.compile("\\[Image.*?\\]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(jsonString);

        // 提取匹配的 URL
        while (matcher.find()) {
            pictureList.add(matcher.group(1));
        }
    }
}