package com.xsy.springbootinit.utils;

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
        List<String> picture = getPicture("灵渊大陆，东域，青云国。在青云国的边境，有一个不起眼的小镇，名为碧溪镇。镇上有一个少年，名叫云凡。他天生体弱，无法修炼，常受人白眼，但内心却怀有不凡的梦想——探索灵渊大陆的奥秘，成为传说中的灵尊。云凡的父亲是一位普通的铁匠，母亲早逝，留给他的只有一块看似普通的玉佩。然而，云凡十六岁生辰那日，玉佩突然发出耀眼的光芒，一股神秘力量涌入他的体内，彻底改变了他的命运。那一夜，云凡做了一个梦。梦中，他站在一片星辰璀璨的虚空之中，一位白发老者向他走来，声音虚无缥缈：“云凡，你的命运之轮已经开始转动，拿起你的剑，守护你心中的正义。”醒来后，云凡发现玉佩中蕴含着一部古老的修炼法门——《星辰变》。这部法门与众不同，它不依赖于外界的灵气，而是通过观想星辰，吸收宇宙之力。云凡开始按照《星辰变》的指引修炼，他的身体逐渐变得强健，灵气在体内流转，形成了一个微小的星核。随着修炼的深入，星核逐渐壮大，释放出强大的力量。碧溪镇外，有一片被称为妖兽森林的禁地。云凡为了检验自己的修炼成果，决定进入森林猎杀妖兽。在森林深处，他遇到了一只凶猛的火狼。火狼眼中闪烁着凶狠的光芒，向云凡扑来。云凡深吸一口气，调动体内的星辰之力，一拳轰出，拳风中带着星辰的轨迹，直接击中火狼的头部。火狼哀嚎一声，倒地不起。云凡上前，从火狼身上取出了一颗火红色的内丹。这次战斗，让云凡意识到了自己修炼法门的强大，也让他更加坚定了修炼的决心。随着实力的提升，云凡在碧溪镇的名声也逐渐响亮。他的行为引起了青云国大家族——萧家的注意。萧家家主萧天雄看中了云凡的潜力，决定收他为徒，带他进入更广阔的世界。云凡离开了碧溪镇，踏上了前往青云国都城的道路。在萧家，他接触到了更高级的修炼法门，结识了志同道合的伙伴，也见识了灵渊大陆的广阔与神秘。然而，随着实力的增长，云凡也逐渐卷入了灵渊大陆的纷争与斗争。他发现，自己的身世似乎与一个古老的预言有关，而这个预言，关乎着整个灵渊大陆的命运。 ");
        System.out.println(picture);
    }

    /**
     * 获取图片（coze）
     * @param content
     * @return
     */
    private static List<String> getPicture(String content) {
        // Replace with your Personal_Access_Token, Bot_Id, and UserId
        String personalAccessToken = "pat_GBSt86m4MdawUCdecLg4Z00crnLzY8U0bhTBFoMCZK6WkMGAA6f30W1CYh95l9fD";
        String botId = "7389169608911241254";
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
        pollForImages(conversationId, chatId, personalAccessToken, content,pictureList);

        return pictureList;
    }

    /**
     * 轮询查询内容生成
     * @param conversationId
     * @param chatId
     * @param personalAccessToken
     * @param pictureList
     */
    private static void pollForImages(String conversationId, String chatId, String personalAccessToken,String text,List pictureList)  {
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
                System.out.println(responseBody);
                // 解析响应内容
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.getInt("code") == 0) {
                    JSONArray data = jsonResponse.getJSONArray("data");
                    if (data != null) {
                        JSONObject message = data.getJSONObject(data.size() - 1);
                        if (message.containsKey("content")) {
                            String content = message.getStr("content");
                            // 检查内容中是否包含图片地址
                            if (message.containsKey("type")){
                                String answerType = message.getStr("type");
                                if (answerType.equals("answer") && content.contains("[Image")){
                                    // 提取图片地址
                                    extractImageUrls(content,pictureList);
                                    finished = true; //轮询结束标识
                                }else if (!"function_call".equals(answerType)){
                                    // 如果不是，则模型未生成图片,重新调用
                                    getPicture(text);
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