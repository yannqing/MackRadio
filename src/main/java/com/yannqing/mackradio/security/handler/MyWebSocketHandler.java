package com.yannqing.mackradio.security.handler;

import cn.hutool.core.date.StopWatch;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.handler.ResultHandler;
import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.tool.AppClient;
import com.yannqing.mackradio.tool.RequestDataTool;
import com.yannqing.mackradio.utils.JwtUtils;
import com.yannqing.mackradio.utils.RedisCache;
import com.yannqing.mackradio.utils.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    // 关键参数
    private static final String APP_ID = "d197c215";
    private static final String API_KEY = "ae92bd51bbce5d485e29c00d814bb2a1";
    private static final String API_SECRET = "ZDA2ZWQyYzdiNjE2NjQ5NjY0NzE5YTQ1";
    private final Duration timeout = Duration.ofMinutes(6); // 设置超时时间为8分钟
    String textFilePath = "./text/";
    String textFileName = "";
    Instant startTime;
    @Resource
    private UserService userService;
    @Resource
    private ObjectMapper objectMapper;
    @Autowired
    private RedisCache redisCache;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String jsonStr = message.getPayload();
        // 解析 JSON 字符串到 JSONObject
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jsonStr);

        // 获取 JSON 对象的值
        String text = jsonObject.getStr("text");
        int userId = jsonObject.getInt("userId");
        // 执行长时间任务
        String result = executeLongRunningTask(text, userId);
        session.sendMessage(new TextMessage(result));
    }

    private String executeLongRunningTask(String text, int userId) throws UnsupportedAudioFileException, IOException, InterruptedException {




        // 执行你的长时间任务，返回结果
        // ...
        String mp4 = getMp4(text, userId);

        return JSONUtil.toJsonStr(ResultUtils.success(Code.SUCCESS, mp4 + ".mp4","获取mp4成功"));
    }

    /**
     * 获取mp4视频文件
     *
     * @param text
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getMp4(String text, int userId) throws IOException, InterruptedException, IllegalArgumentException, UnsupportedAudioFileException {
        StopWatch totalWatch = new StopWatch();
        totalWatch.start();
        if (text.length() > 3000) {
            //异常抛出，如需修改文字，注意修改全局异常！
            throw new RuntimeException("输入文本不能超过3000字，请重试！");
        }
        User loginUser = getLoginUser(userId);
        if (loginUser == null) {
            throw new IllegalStateException("未登录，请重新登录！");
        }
        if (loginUser.getAccessTimes() <= 0) {
            throw new IllegalArgumentException("您的可访问次数不足，请重试！");
        }
        log.info("生成txt文件开始");
        stringToText(text);
        String name = UUID.randomUUID() + ".mp4";
        getMp3BySH(text, name);

        //用户可访问次数-1
        userService.update(new UpdateWrapper<User>().eq("id", loginUser.getId()).set("accessTimes", loginUser.getAccessTimes() - 1));
        log.info("返回成功！");
        totalWatch.stop();
        log.info("总耗时：{}", totalWatch.getTotalTimeMillis());
        return name;
    }
    /**
     * 讯飞模型语音生成 脚本执行--
     *
     * @param text
     * @param name
     * @throws IOException
     */
    public void getMp3BySH(String text, String name) throws IOException, InterruptedException, UnsupportedAudioFileException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        AppClient appClient = new AppClient(API_KEY, API_SECRET);

        // 1.创建任务
        String createUrl = RequestDataTool.getCreateUrl();
        String rawCreateRequestJsonStr = RequestDataTool.getCreateRequestJsonStr();
        // 重构requestData内容,对具体参数重写
        String createRequestData = buildCreateRequestData(JSONObject.parseObject(rawCreateRequestJsonStr));
        // 请求
        String createResp = appClient.doRequest(createRequestData, createUrl);
        // 判断请求结果
        JSONObject createRespObj = JSONObject.parseObject(createResp);
        Object code = JSONPath.eval(createRespObj, "$.header.code");
        if (code != null) {
            if ((int) code != 0) {
                // 创建任务失败，打印报错后中止
                log.error("create task failed, code = {}, message = {}", code, JSONPath.eval(createRespObj, "$.header.message"));
                throw new IllegalArgumentException("讯飞创建任务失败，请重试");
            }
        }

        // 取 task id
        String taskId = (String) JSONPath.eval(createRespObj, "$.header.task_id");
        // 2.查询任务
        String queryUrl = RequestDataTool.getQueryUrl();
        String rawQueryRequestJsonStr = RequestDataTool.getQueryRequestJsonStr();
        // 重构requestData内容,对具体参数重写
        String queryRequestData = buildQueryRequestData(JSONObject.parseObject(rawQueryRequestJsonStr), taskId);

        // 定时调度查询任务

        while (true) {
            int taskStatus;
            JSONObject queryRespObj;
            // 请求
            String queryResp = appClient.doRequest(queryRequestData, queryUrl);
            // 判断请求结果
            queryRespObj = JSONObject.parseObject(queryResp);
            Object qryCode = JSONPath.eval(queryRespObj, "$.header.code");
            if (qryCode != null) {
                if ((int) qryCode != 0) {
                    log.error("query task failed, code = {}, message = {}", qryCode, JSONPath.eval(queryRespObj, "$.header.message"));
                    throw new IllegalArgumentException("query task failed");
                }
            }
            // 判断任务状态
            taskStatus = Integer.parseInt((String) JSONPath.eval(queryRespObj, "$.header.task_status"));
            if (taskStatus == 5) {
                // 处理结果数据
                ResultHandler.respDataPostProcess(queryRespObj);
                stopWatch.stop();
                log.info("讯飞语音生成耗时：{}", stopWatch.getTotalTimeMillis());
//                nameToWav();
                //将生成的lame文件转为wav
                toWav(taskId);


                String audioPath = "./music/" + UUID.randomUUID() + ".wav";  //有背景音的音频（未生成）
                String background = getRandomBackgroundMusic();     //背景音
                String srtFilePath = "./srt/" + UUID.randomUUID() + ".srt"; //字幕文件（未生成）
                String radioPath = "./video/" + UUID.randomUUID() + ".mp4"; //无字幕视频（未生成）
                String voice = "./music/" + taskId + ".lame";   //讯飞生成的lame文件
                String imageUrl = UUID.randomUUID() + ".txt";   //放置图片URL的txt
                String imageDir = UUID.randomUUID().toString(); //放置图片的文件夹
                String tempVideo = "./video/" + UUID.randomUUID() + ".mp4"; //放置无音频的视频
                //生成图片：
                startTime = Instant.now();
                List<String> picture = getPicture(text);
                log.info("图片生成成功：{}", picture.toString());
                log.info("图片生成耗时：{}", Duration.between(startTime, Instant.now()));
                //断句
                List<String> sentences = splitSentencesFromFile(text);
                //配背景音乐
                mergeBackground("./music/" + taskId + ".wav", background, audioPath);
                //将图片结果放到txt文件中
                writeListToFile(picture, "./image/image_urls/" + imageUrl);
                //生成srt文件
                long durationInMillis = getWavDuration(new File(audioPath));
                log.info("获取音频文件总时长成功：{}", durationInMillis);
                List<Integer> durations = calculateDurations(sentences, (int) durationInMillis);
                generateSrtFile(sentences, durations, srtFilePath);


                excutorSH(voice, background, audioPath, imageUrl, imageDir, srtFilePath, tempVideo, radioPath, "./" + name);
                // 任务完成，关闭调度器
                // SCHEDULER.shutdown();
                break;
            }
            Thread.sleep(5 * 1000);
        }
    }

    /**
     * 获取图片（coze）
     *
     * @param content
     * @return
     */
    public List<String> getPicture(String content) {
        //设置超时时间
        log.info("开始生成图片---{}", startTime);

        // 检查是否超过8分钟
        log.info("重试图片---{}", Instant.now());
        log.info("时间差{}", String.valueOf(Duration.between(startTime, Instant.now())));
//        log.info("与10分钟比===>{}", String.valueOf(Duration.between(startTime, Instant.now()).compareTo(timeout)));
        if (Duration.between(startTime, Instant.now()).compareTo(timeout) > 0) {
            throw new RuntimeException("图片生成超时，请重试。");
        }

        log.info("开始生成图片");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // Replace with your Personal_Access_Token, Bot_Id, and UserId
        String personalAccessToken = "pat_GBSt86m4MdawUCdecLg4Z00crnLzY8U0bhTBFoMCZK6WkMGAA6f30W1CYh95l9fD";
        String botId = "7389169608911241254";
        String userId = "7345861690204536843";
        String yourQuery = content;

        // Create JSON body
        cn.hutool.json.JSONObject message = new cn.hutool.json.JSONObject()
                .set("role", "user")
                .set("content", yourQuery)
                .set("content_type", "text");

        JSONArray messagesArray = new JSONArray().put(message);

        cn.hutool.json.JSONObject requestBody = new cn.hutool.json.JSONObject()
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
            cn.hutool.json.JSONObject jsonResponse = new cn.hutool.json.JSONObject(response.body());
            int responseCode = jsonResponse.getInt("code");
            cn.hutool.json.JSONObject data = jsonResponse.getJSONObject("data");

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

        pollForImages(conversationId, chatId, personalAccessToken, content, pictureList);

        stopWatch.stop();
        log.info("结束生成图片");
        log.info("生成图片总耗时：{}", stopWatch.getTotalTimeMillis());
        return pictureList;
    }

    /**
     * 轮询查询内容生成
     *
     * @param conversationId
     * @param chatId
     * @param personalAccessToken
     * @param pictureList
     */
    private void pollForImages(String conversationId, String chatId, String personalAccessToken, String text, List pictureList) {
        String url = "https://api.coze.cn/v3/chat/message/list?conversation_id=" + conversationId + "&chat_id=" + chatId;
        HttpRequest getRequest = HttpRequest.get(url)
                .header("Authorization", "Bearer " + personalAccessToken)
                .header("Content-Type", "application/json");

        boolean finished = false;

        while (!finished) {
            if (Duration.between(startTime, Instant.now()).compareTo(timeout) > 0) {
                throw new RuntimeException("图片生成超时，请重试。");
            }

            log.info("========图片轮询一次=========");
            HttpResponse getResponse = getRequest.execute();
            getResponse.charset("UTF-8");
            String responseBody = getResponse.body();
            System.out.println(responseBody);
            // 解析响应内容
            cn.hutool.json.JSONObject jsonResponse = new cn.hutool.json.JSONObject(responseBody);
            if (jsonResponse.getInt("code") == 0) {
                JSONArray data = jsonResponse.getJSONArray("data");
                if (data != null) {
                    cn.hutool.json.JSONObject message = data.getJSONObject(data.size() - 1);
                    if (message.containsKey("content")) {
                        String content = message.getStr("content");
                        // 检查内容中是否包含图片地址
                        if (message.containsKey("type")) {
                            String answerType = message.getStr("type");
                            if (answerType.equals("answer") && content.contains("[Image")) {
                                // 提取图片地址
                                extractImageUrls(content, pictureList);
                                finished = true; //轮询结束标识
                            } else if (!"function_call".equals(answerType)) {
                                // 如果不是，则模型未生成图片,重新调用
                                getPicture(text);
                                break;
                            }
                        }
                    }
                }
            } else {
                System.out.println("Error occurred: " + jsonResponse.getStr("msg"));
                finished = true; // 如果发生错误，结束轮询
            }
            if (!finished) {
                try {
                    // 等待一段时间后重试，例如等待10秒
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finished = true;
                }
            }
        }
    }

    /**
     * 从给定的 JSON 字符串中提取图片 URL。
     *
     * @param jsonString 包含 content 的 JSON 字符串
     * @return 提取出的图片 URL 列表
     */
    public void extractImageUrls(String jsonString, List pictureList) {
        // 正则表达式，用于匹配 [Image] 标识符后的 URL
        Pattern pattern = Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(jsonString);

        // 提取匹配的 URL
        while (matcher.find()) {
            pictureList.add(matcher.group(1));
        }
    }

    /**
     * 获取wav文件的时长
     *
     * @param file
     * @return
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public long getWavDuration(File file) throws UnsupportedAudioFileException, IOException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = file.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        long frames = audioFileLength / frameSize;
        return (long) ((frames / frameRate) * 1000);
    }


    /**
     * 对用户输入的文本进行断句
     *
     * @param text
     * @return
     */
    public List<String> splitSentencesFromFile(String text) {
        List<String> sentences = new ArrayList<>();

        // Split line into sentences using delimiters
        String[] lineSentences = text.split("[,.!?;，。！？；]");

        // Add each sentence to the list
        for (String sentence : lineSentences) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        return sentences;
    }


    /**
     * 获取随机背景音乐
     *
     * @return
     */
    public String getRandomBackgroundMusic() {


        String filePath = "./background/";
        File directory = new File(filePath);
        List<String> backgroundMusic = null;
        if (directory.isDirectory()) {
            //获取所有子文件
            File[] files = directory.listFiles();
            if (files == null || files.length == 0) {
                throw new IllegalArgumentException("文件夹下无背景音乐，请假检查！");
            } else {
                backgroundMusic = Arrays.stream(files).map(File::getName).toList();
            }

        } else {
            throw new IllegalArgumentException("背景音乐文件夹打开错误！");
        }

        // 创建 Random 对象
        Random random = new Random();

        // 获取随机索引
        int index = random.nextInt(backgroundMusic.size());

        // 获取随机值
        return filePath + backgroundMusic.get(index);
    }


    /**
     * 给字幕配背景音，合并为一个音频文件
     *
     * @param voice  字幕朗读的音频文件路径
     * @param output 输出文件的音频文件路径
     */
    public void mergeBackground(String voice, String background, String output) throws InterruptedException, IOException {
        log.info("字幕配背景音开始===");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "./merge.sh", voice, background, output);
        Process process = processBuilder.start();

// 获取进程的输入流和错误流
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();

// 创建线程来读取输入流和错误流
        Thread inputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Output: " + line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("字幕音频配音失败！");
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Error: " + line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("字幕音频配音失败！");
            }
        });

// 启动输入流和错误流读取线程
        inputThread.start();
        errorThread.start();

// 等待外部进程执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalArgumentException("字幕音配背景音脚本执行失败！");
        }
        System.out.println("Exit code: " + exitCode);
        log.info("字幕配背景音结束===");
        stopWatch.stop();
        log.info("讯飞语音配背景音耗时：{}", stopWatch.getTotalTimeMillis());
    }


    /**
     * 计算每句的时长
     *
     * @param sentences
     * @param totalDuration
     * @return
     */
    private static List<Integer> calculateDurations(List<String> sentences, int totalDuration) {
        List<Integer> durations = new ArrayList<>();
        int totalLength = sentences.stream().mapToInt(String::length).sum();

        for (String sentence : sentences) {
            int duration = (int) ((double) sentence.length() / totalLength * totalDuration);
            durations.add(duration);
        }

        return durations;
    }


    /**
     * 将图片写入txt文件
     *
     * @param list
     * @param filePath
     */
    public void writeListToFile(List<String> list, String filePath) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        for (String line : list) {
            writer.write(line);
            writer.write("\n"); // Ensure LF (Line Feed) is used
        }
        writer.close();
    }


    public void excutorSH(String voice, String background, String audioPath, String imageTxt, String imageDir, String srtFilePath, String tempVideo, String radioPath, String outputPath) throws IOException, InterruptedException {
        String name = UUID.randomUUID().toString();
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "./finally.sh", voice, background, audioPath, imageTxt, imageDir, srtFilePath, tempVideo, radioPath, outputPath);
        Process process = processBuilder.start();

        // 获取进程的输入流和错误流
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();

        // 创建线程来读取输入流和错误流
        Thread inputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Output: " + line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Error: " + line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());

            }
        });

        // 启动输入流和错误流读取线程
        inputThread.start();
        errorThread.start();

        // 等待外部进程执行完成
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        if (exitCode != 0) {
            throw new IllegalArgumentException("脚本执行失败");
        }
    }


    /**
     * 生成字幕文件
     *
     * @param subtitles
     * @param durations
     * @param fileName
     * @throws IOException
     */
    private void generateSrtFile(List<String> subtitles, List<Integer> durations, String fileName) throws IOException {
        log.info("开始生成srt字幕文件");
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        int currentTime = 0;

        for (int i = 0; i < subtitles.size(); i++) {
            String subtitle = subtitles.get(i);
            int duration = durations.get(i);

            // 如果是第一句字幕，减少显示时间0.1秒（100毫秒）
            if (i == 0) {
                duration -= 100;
                if (duration < 0) {
                    duration = 0; // 确保时长不会变成负数
                }
            }

            writer.write((i + 1) + "\n");
            writer.write(formatTime(currentTime) + " --> " + formatTime(currentTime + duration) + "\n");
            writer.write(subtitle + "\n\n");

            currentTime += duration;
        }

        writer.close();
        log.info("结束生成srt字幕文件");
    }

    /**
     * 格式化时间，写入srt文件
     *
     * @param milliseconds
     * @return
     */
    private String formatTime(int milliseconds) {
        int hours = milliseconds / 3600000;
        milliseconds %= 3600000;
        int minutes = milliseconds / 60000;
        milliseconds %= 60000;
        int seconds = milliseconds / 1000;
        milliseconds %= 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }

    /**
     * 构造请求数据
     * query
     */
    private String buildQueryRequestData(JSONObject requestData, String taskId) {
        // 参数重写
        JSONPath.set(requestData, "$.header.app_id", APP_ID);
        JSONPath.set(requestData, "$.header.task_id", taskId);
        return requestData.toString();
    }

    /**
     * 构造请求数据
     * create
     */
    private String buildCreateRequestData(JSONObject requestData) throws IOException {
        // 重写app_id和request_id
        JSONPath.set(requestData, "$.header.app_id", APP_ID);
        JSONPath.set(requestData, "$.header.request_id", UUID.randomUUID());
        // 请按照需要对其他参数进行重写，本例中选择把结果音频编码格式指定为lame，即可通过主流的音频解码器播放试听
        JSONPath.set(requestData, "$.parameter.dts.audio.encoding", "lame");
//        JSONPath.set(requestData, "$.payload.text.encoding","base64");

        File file = new File("./text/" + textFileName);
//        File file = new File("C:\\Users\\67121\\video\\text\\" + textFileName);
        JSONPath.set(requestData, "$.payload.text.text", Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file)));
        return requestData.toString();
    }

    /**
     * 将lame文件转为wav
     *
     * @param name
     */
    public void toWav(String name) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "./toWav.sh", "./music/" + name + ".lame", "./music/" + name + ".wav");
        Process process = processBuilder.start();

// 获取进程的输入流和错误流
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();

// 创建线程来读取输入流和错误流
        Thread inputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Output: " + line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("音频文件lame->wav转换失败");
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Error: " + line);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("音频文件lame->wav转换失败");
            }
        });

// 启动输入流和错误流读取线程
        inputThread.start();
        errorThread.start();

// 等待外部进程执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalArgumentException("音频转换脚本执行失败");
        }
        System.out.println("Exit code: " + exitCode);
    }


    /**
     * 将text文本写为txt文件
     *
     * @param text
     */
    public void stringToText(String text) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String name = UUID.randomUUID().toString();
        textFileName = name + ".txt";
        String filePath = textFilePath + textFileName;

        try (FileWriter fileWriter = new FileWriter(filePath);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopWatch.stop();
        log.info("生成txt文件结束, 用时：{}", stopWatch.getTotalTimeMillis());
    }


    private User getLoginUser(int userId) throws JsonProcessingException {
        String token = redisCache.getCacheObject("token:" + userId);
        String loginUserInfo = JwtUtils.getUserInfoFromToken(token);
        return objectMapper.readValue(loginUserInfo, User.class);
    }

}
