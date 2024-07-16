package com.yannqing.mackradio.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yannqing.mackradio.domain.User;
import com.yannqing.mackradio.exception.BusinessException;
import com.yannqing.mackradio.handler.ResultHandler;
import com.yannqing.mackradio.mapper.UserMapper;
import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.service.VideoService;
import com.yannqing.mackradio.tool.AppClient;
import com.yannqing.mackradio.tool.RequestDataTool;
import com.yannqing.mackradio.utils.JwtUtils;
import com.yannqing.mackradio.utils.RedisCache;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    // 关键参数
    private static final String APP_ID = "d197c215";
    private static final String API_KEY = "ae92bd51bbce5d485e29c00d814bb2a1";
    private static final String API_SECRET = "ZDA2ZWQyYzdiNjE2NjQ5NjY0NzE5YTQ1";

    String textFilePath = "./text/";
    String textFileName = "";

    private Java2DFrameConverter converter;
    @Autowired
    private RedisCache redisCache;

    public VideoServiceImpl(UserMapper userMapper) {
    }

    private Java2DFrameConverter getConverter() {
        if (converter == null) {
            converter = new Java2DFrameConverter();
        }
        return converter;
    }

    /**
     * 讯飞模型语音生成
     * @param text
     * @param name
     * @throws IOException
     */
    public void getMp3(String text, String name) throws IOException, InterruptedException, UnsupportedAudioFileException {
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
        if(code != null) {
            if((int)code != 0) {
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
                //给wav文件配背景音乐
                String audioPath = "./music/" + UUID.randomUUID() + ".wav";
                String background = getRandomBackgroundMusic();
                mergeBackground("./music/" + taskId + ".wav", background, audioPath);
                //合成视频
                String srtFilePath = "./srt/" + UUID.randomUUID() + ".srt";
                String radioPath = "./video/" + UUID.randomUUID() + ".mp4";
                change(text,
                        srtFilePath,
                        audioPath,
                        radioPath,
                        "./main.sh",
                        "./" + name,
                        "./image/"
                );
                // 任务完成，关闭调度器
                // SCHEDULER.shutdown();
                break;
            }
            Thread.sleep(5 * 1000);
        }

        long l = System.currentTimeMillis();
    }

    /**
     * 获取随机背景音乐
     * @return
     */
    @Override
    public String getRandomBackgroundMusic(){



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

        }else{
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
     * 对用户输入的文本进行断句
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
     * 格式化时间，写入srt文件
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
     * 获取mp4视频文件
     * @param text
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public String getMp4(String text, HttpServletRequest request) throws IOException, InterruptedException, IllegalArgumentException, UnsupportedAudioFileException {
        StopWatch totalWatch = new StopWatch();
        totalWatch.start();
        if (text.length() > 3000) {
            //异常抛出，如需修改文字，注意修改全局异常！
            throw new RuntimeException( "输入文本不能超过3000字，请重试！");
        }
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new IllegalStateException("未登录，请重新登录！");
        }
        if (loginUser.getAccessTimes() <= 0) {
            throw new IllegalArgumentException("您的可访问次数不足，请重试！");
        }
        log.info("生成txt文件开始");
        stringToText(text);
        String name = UUID.randomUUID() + ".mp4";
        getMp3(text, name);

        //用户可访问次数-1
        userService.update(new UpdateWrapper<User>().eq("id", loginUser.getId()).set("accessTimes", loginUser.getAccessTimes() - 1));
        log.info("返回成功！");
        totalWatch.stop();
        log.info("总耗时：{}", totalWatch.getTotalTimeMillis());
        return name;
    }

    @Override
    public void change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath) throws IOException, UnsupportedAudioFileException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        //断句
        List<String> sentences = splitSentencesFromFile(text);
        List<String> picture = getPicture(text);
        log.info(picture.toString());
        stopWatch.start();
            List<BufferedImage> images = loadImages(picture);
            
            int frameWidth = 1088; // 设置帧宽度
            int frameHeight = 900; // 设置帧高度

            // 加载音频文件
            log.info("加载音频文件开始");
            File audioFile = new File(audioPath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            log.info("加载音频文件结束");
            //获取wav文件的总时长
            long durationInMillis = getWavDuration(new File(audioPath));
            log.info("获取音频文件总时长成功：{}", durationInMillis);
            List<Integer> durations = calculateDurations(sentences, (int) durationInMillis);
            log.info("计算每句字幕的时长：{}", durations);
            generateSrtFile(sentences, durations, srtFilePath);
            log.info("生成字幕文件srt成功！");

            //初始化视频
            log.info("开始初始化视频！");
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(radioPath, frameWidth, frameHeight);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(30);
            recorder.setVideoBitrate(1000000); // 设置比特率，单位为bps
            recorder.setVideoQuality(0); // 设置编码器质量，0表示最高质量
            recorder.setAudioChannels(audioInputStream.getFormat().getChannels());
            recorder.setSampleRate((int) audioInputStream.getFormat().getSampleRate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // 或其他适当的音频编解码器

            recorder.start();


            //srt文件的总时长（单位：秒）
            long srtTime = getSRTTotalDurationInSeconds(srtFilePath);
            int changeImage = (int) (srtTime / images.size()) + 1;  //切换一张图片需要的时间（单位：s）
            int framesPerImage = 30 * changeImage; // 每2秒切换一个图片，帧率为30
            int totalFrames = framesPerImage * images.size();

            for (int i = 0; i < totalFrames; i++) {
                int imageIndex = i / framesPerImage;
                if (imageIndex >= images.size()) {
                    break; // 确保不会超出图片数量
                }
                BufferedImage image = images.get(imageIndex);
                // 定义路径
                int[] path = definePath(image.getWidth(), changeImage);
                // 计算Y坐标（线性上下移动）
                int maxY = image.getHeight() - frameHeight;
                int cycleFrames = totalFrames / 6; // 完成一次上下移动所需的帧数
                int currentCycleFrame = i % (cycleFrames * 2);

                int y;
                if (currentCycleFrame < cycleFrames) {
                    // 向下移动
                    y = (int) ((double) currentCycleFrame / cycleFrames * maxY);
                } else {
                    // 向上移动
                    y = (int) ((double) (cycleFrames * 2 - currentCycleFrame) / cycleFrames * maxY);
                }
                int x = path[i % framesPerImage]; // 使用path数组中的x坐标
                // 从原图中截取当前帧
                BufferedImage frame = image.getSubimage(x, y, frameWidth, frameHeight);

                // 获取当前时间（毫秒）
                long currentTime = i * 1000L / 30L;
                // 将帧添加到视频
                recorder.record(getConverter().convert(frame));

                // 添加对应的音频片段
                addAudioSegment(audioInputStream, recorder, 1.0 / recorder.getFrameRate());
            }

            // 完成视频编码
        recorder.stop(); recorder.release();
        log.info("视频生成成功！");
        stopWatch.stop();
        log.info("无字幕视频生成耗时：{}", stopWatch.getTotalTimeMillis());

        //给视频添加字幕
        stopWatch.start();
        mergeSRT(radioPath, outputPath, srtFilePath);
        stopWatch.stop();
        log.info("视频添加字幕耗时：{}", stopWatch.getTotalTimeMillis());
    }

    /**
     * mp4合成字幕
     * @param radioPath 原mp4路径
     * @param outputPath 生成的mp4路径
     */
    public void mergeSRT(String radioPath, String outputPath, String srtFilePath) throws IOException, InterruptedException {
        log.info("=======字幕合成开始========");
        String name =  UUID.randomUUID().toString();
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "./main.sh", srtFilePath, radioPath, outputPath);
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
                throw new IllegalArgumentException("字幕合成视频失败");
            }
        log.info("=======字幕合成结束========");
    }

    /**
     * 计算每句的时长
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
     * 生成字幕文件
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
     * 获取图片（coze）
     * @param content
     * @return
     */
    public List<String> getPicture(String content) {
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


        pollForImages(conversationId, chatId, personalAccessToken, content,pictureList);
        stopWatch.stop();
        log.info("结束生成图片");
        log.info("生成图片总耗时：{}", stopWatch.getTotalTimeMillis());
        return pictureList;
    }

    /**
     * 轮询查询内容生成
     * @param conversationId
     * @param chatId
     * @param personalAccessToken
     * @param pictureList
     */
    private void pollForImages(String conversationId, String chatId, String personalAccessToken,String text,List pictureList)  {
        String url = "https://api.coze.cn/v3/chat/message/list?conversation_id=" + conversationId + "&chat_id=" + chatId;
        HttpRequest getRequest = HttpRequest.get(url)
                .header("Authorization", "Bearer " + personalAccessToken)
                .header("Content-Type", "application/json");

        boolean finished = false;

        //设置超时时间
        Instant startTime = Instant.now(); // 记录循环开始时间
        Duration timeout = Duration.ofMinutes(8); // 设置超时时间为8分钟
        while (!finished) {
            // 检查是否超过10分钟
            if (Duration.between(startTime, Instant.now()).compareTo(timeout) > 0) {
                throw new RuntimeException("图片生成超时，请重试。");
            }
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
    public void extractImageUrls(String jsonString,List pictureList)  {
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
     * 将text文本写为txt文件
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

    /**
     * 将lame文件转为wav
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
     * 给字幕配背景音，合并为一个音频文件
     * @param voice 字幕朗读的音频文件路径
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

    private List<BufferedImage> loadImages(List<String> directoryPath) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        for (int i = 0; i < directoryPath.size(); i++) {
            URL imageUrl = new URL(directoryPath.get(i));
            images.add(ImageIO.read(imageUrl));
        }
        return images;
    }

    private int[] definePath(int imageWidth, int per) {
        // 这里定义路径，返回一个表示x坐标的数组
        // 示例：简单的从左到右移动
        int pathLength = 30 * 2 * per; // 增加路径数组的长度以减慢移动速度
        int[] path = new int[pathLength];
//        for (int i = 0; i < pathLength; i++) {
//            path[i] = i * (imageWidth - frameWidth) / (pathLength - 1);
//        }
        return path;
    }

    /**
     * 按帧添加音频
     * @param audioInputStream
     * @param recorder
     * @param frameDuration
     */
    private void addAudioSegment(AudioInputStream audioInputStream, FFmpegFrameRecorder recorder, double frameDuration) throws IOException {
            // 计算每帧的采样数
            int sampleRate = (int) audioInputStream.getFormat().getSampleRate();
            int numSamples = (int) (sampleRate * frameDuration);

            // 创建缓冲区
            int bytesPerSample = 2; // 假设每个样本占用2个字节（16位音频）
            int numChannels = audioInputStream.getFormat().getChannels();
            byte[] buffer = new byte[numSamples * bytesPerSample * numChannels];

            int bytesRead = 0;
            int offset = 0;
            while (offset < buffer.length && (bytesRead = audioInputStream.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += bytesRead;
            }

            if (offset > 0) {
                // 将字节数组转换为短整型数组
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.nativeOrder()).asShortBuffer();
                short[] samples = new short[shortBuffer.capacity()];
                shortBuffer.get(samples);

                // 创建音频帧
                Frame audioFrame = new Frame();
                audioFrame.samples = new ShortBuffer[] { ShortBuffer.wrap(samples) };
                audioFrame.sampleRate = sampleRate;
                audioFrame.audioChannels = numChannels;

                // 记录音频帧
                recorder.record(audioFrame);
            }
    }

    /**
     * 获取srt文件的总时长（单位：秒）
     * @param filePath
     * @return
     * @throws IOException
     */
    public long getSRTTotalDurationInSeconds(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        LocalTime maxEndTime = LocalTime.MIN;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");

        while ((line = reader.readLine()) != null) {
            if (line.contains("-->")) {
                String[] times = line.split(" --> ");
                try {
                    LocalTime endTime = LocalTime.parse(times[1].trim(), timeFormatter);
                    if (endTime.isAfter(maxEndTime)) {
                        maxEndTime = endTime;
                    }
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                }
            }
        }
        reader.close();

        Duration totalDuration = Duration.between(LocalTime.MIN, maxEndTime);
        return totalDuration.getSeconds();
    }

    private User getLoginUser(HttpServletRequest request) throws JsonProcessingException {
        int userId = Integer.parseInt(request.getHeader("userId"));
        String token = redisCache.getCacheObject("token:" + userId);
        String loginUserInfo = JwtUtils.getUserInfoFromToken(token);
        return objectMapper.readValue(loginUserInfo, User.class);
    }
}
