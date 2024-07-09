package com.yannqing.mackradio.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.yannqing.mackradio.handler.ResultHandler;
import com.yannqing.mackradio.service.VideoService;
import com.yannqing.mackradio.tool.AppClient;
import com.yannqing.mackradio.tool.RequestDataTool;
import com.yannqing.mackradio.vo.Subtitle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_cudacodec.VideoWriter;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {


    // 关键参数
    private static final String APP_ID = "d905bce2";
    private static final String API_KEY = "41ba89296a766cf4ade99a43141717ec";
    private static final String API_SECRET = "OGJmNTY2MjFiZmEzMGU4MDdlNTc4MWVm";

    String sourceMp4 = "./video/source.mp4";
    String musicMp4 = "./video/musicRadio.mp4";
    String outPutMp4 = "./video/output.mp4";
    String srtFilePath = "./srt/dialog.srt";
    String textFilePath = "./text/";
    String textFileName = "";
    String images = "./image";

    List<String> backgroundMusic = Arrays.asList("./background/m2.mp3", "./background/m3.mp3");


    private Java2DFrameConverter converter;

    private Java2DFrameConverter getConverter() {
        if (converter == null) {
            converter = new Java2DFrameConverter();
        }
        return converter;
    }

    @Override
    public Object getMp4(String text) throws Exception {
//        //1. 根据文字生成mp3
//        log.info("=======生成MP3开始========");
//        getMp3(text);
//        log.info("=======生成MP3结束========");
//        //2. 根据文字生成图片
//        log.info("=======生成图片开始========");
//        log.info("=======生成图片结束========");
//        //...
//        //3. 根据图片，mp3生成视频
//        log.info("=======生成MP4开始========");
//        // 图片集合的目录
//        List<File> list = readFile(images);
//        int width = 1849;
//        int height = 932;
//        createMp4(sourceMp4, list, width, height, getMp3Duration(audioPath));
//        mergeAudioAndVideo(sourceMp4, audioPath, musicMp4);
//        log.info("=======生成MP4结束========");
//
//        //4. 根据文字生成字幕文件srt
//        log.info("=======生成字幕开始========");
//
//        List<String> sentences = splitSentencesFromFile(text);
//        generateSrtFromList(sentences, srtFilePath);
//        log.info("=======生成字幕结束========");
//        //5. 根据字幕文件srt，视频文件mp4合成有字幕的视频。
//        log.info("=======字幕合成开始========");
//        try {
//            String name = UUID.randomUUID().toString();
//            ProcessBuilder processBuilder = new ProcessBuilder("bash", "./main.sh", "./srt/dialog.srt", "./video/musicRadio.mp4", "./" + name + ".mp4");
//            Process process = processBuilder.start();
//
//// 获取进程的输入流和错误流
//            InputStream inputStream = process.getInputStream();
//            InputStream errorStream = process.getErrorStream();
//
//// 创建线程来读取输入流和错误流
//            Thread inputThread = new Thread(() -> {
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        System.out.println("Output: " + line);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//
//            Thread errorThread = new Thread(() -> {
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        System.err.println("Error: " + line);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//
//// 启动输入流和错误流读取线程
//            inputThread.start();
//            errorThread.start();
//
//// 等待外部进程执行完成
//            int exitCode = process.waitFor();
//            System.out.println("Exit code: " + exitCode);
//        } catch (Exception e) {
//            log.error("字幕合成失败！");
//            log.error(e.getMessage());
//            e.printStackTrace();
//        }
//        log.info("=======字幕合成结束========");
        return null;
    }


    // 定时调度
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static String task_id = "";

//    public void getMp3(String text) {
//        String fileUrl = "http://212.64.18.207:7021/down?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
//
//        String savePath = audioPath;
//
//        try {
//            URL url = new URL(fileUrl);
//            Path saveFilePath = Path.of(savePath);
//
//            // Download the file using BufferedInputStream
//            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
//                 FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath.toFile())) {
//
//                byte[] dataBuffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
//                    fileOutputStream.write(dataBuffer, 0, bytesRead);
//                }
//            }
//
//            System.out.println("File downloaded successfully!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 讯飞模型语音生成
     * @param text
     * @param name
     * @throws IOException
     */
    public void getMp32(String text, String name) throws IOException {
        AppClient appClient = new AppClient(API_KEY, API_SECRET);

        // 1.创建任务
        String createUrl = RequestDataTool.getCreateUrl();
        String createRequestJsonStr = RequestDataTool.getCreateRequestJsonStr();
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
                return ;
            }
        }

        // 取 task id
        String taskId = (String) JSONPath.eval(createRespObj, "$.header.task_id");
        task_id = taskId;
        // 2.查询任务
        String queryUrl = RequestDataTool.getQueryUrl();
        String rawQueryRequestJsonStr = RequestDataTool.getQueryRequestJsonStr();
        // 重构requestData内容,对具体参数重写
        String queryRequestData = buildQueryRequestData(JSONObject.parseObject(rawQueryRequestJsonStr), taskId);

        // 定时调度查询任务
        SCHEDULER.scheduleWithFixedDelay(() -> {
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
                }
            }
            // 判断任务状态
            taskStatus = Integer.parseInt((String) JSONPath.eval(queryRespObj, "$.header.task_status"));
            if (taskStatus == 5) {
                // 处理结果数据
                ResultHandler.respDataPostProcess(queryRespObj);
//                nameToWav();
                //将生成的lame文件转为wav
                toWav(task_id);
                //给wav文件配背景音乐
                String randomBackgroundMusic = getRandomBackgroundMusic();
                String audioPath = "./music/" + UUID.randomUUID().toString() + ".wav";
                mergeBackground("./music/" + task_id + ".wav", randomBackgroundMusic, audioPath);
                //合成视频
                change(text,
                        "./srt/dialog.srt",
                        audioPath,
                        "./video/output.mp4",
                        "./main.sh",
                        "./" + name,
                        "./image/"
                );
                // 任务完成，关闭调度器
                SCHEDULER.shutdown();
            }
        }, 0, 5, TimeUnit.SECONDS);

    }

    /**
     * 获取随机背景音乐
     * @return
     */
    public String getRandomBackgroundMusic(){
        // 创建 Random 对象
        Random random = new Random();

        // 获取随机索引
        int index = random.nextInt(backgroundMusic.size());

        // 获取随机值
        return backgroundMusic.get(index);
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

        File file = new File("./text/" + textFileName);
//        File file = new File("C:\\Users\\67121\\video\\text\\" + textFileName);
        JSONPath.set(requestData, "$.payload.text.text", Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file)));
        return requestData.toString();
    }

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

    public void generateSrtFromList(List<String> sentences, String srtFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(srtFilePath))) {
            int index = 1;
            int startTime = 0;

            for (String sentence : sentences) {
                // Skip empty sentences
                if (sentence.trim().isEmpty()) {
                    continue;
                }

                // Write subtitle index
                writer.write(Integer.toString(index));
                writer.newLine();

                int duration = calculateDuration(sentence);
                // Write subtitle time duration
                writer.write(formatTime(startTime) + " --> " + formatTime(startTime + duration));
                writer.newLine();

                writer.write(sentence);
                writer.newLine();
                writer.newLine();

                index++;
                startTime += duration;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateDuration(String subtitle) {
        // Calculate duration based on the length of the subtitle
        // You can adjust the duration calculation logic according to your requirements
        int length = subtitle.length();
        int baseDuration = 1000; // 1 second
        int charactersPerSecond = 200; // Adjust this value according to your preference
        return subtitle.length() * charactersPerSecond;
    }

    private String formatTime(int milliseconds) {
        int hours = milliseconds / 3600000;
        milliseconds %= 3600000;
        int minutes = milliseconds / 60000;
        milliseconds %= 60000;
        int seconds = milliseconds / 1000;
        milliseconds %= 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
    }


    @Override
    public String getMp42(String text) throws IOException {
        log.info("生成txt文件开始");
        stringToText(text);
        log.info("生成txt文件结束");
        String name = UUID.randomUUID() + ".mp4";
        getMp32(text, name);
//        change(text,
//                "./srt/dialog.srt",
//                "./music/" + "outputm3" + ".wav",
//                "./video/output.mp4",
//                "./main.sh",
//                "./" + name,
//                "./image/"
//        );



        return name;
    }

    @Override
    public String change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath) {

//        log.info("开始生成字幕");
        List<String> sentences = splitSentencesFromFile(text);
//        generateSrtFromList(sentences, srtFilePath);
//        log.info("结束生成字幕");
        log.info("开始生成图片");
        List<String> picture = getPicture(text);
        log.info("结束生成图片");
        try {
//            List<BufferedImage> images = loadImages("./image/");
            List<BufferedImage> images = loadImages(picture);
            
            int frameWidth = 1088; // 设置帧宽度
            int frameHeight = 900; // 设置帧高度

            // 加载音频文件
            File audioFile = new File(audioPath);
            if (!audioFile.exists()) {
                throw new FileNotFoundException("音频文件不存在：" + audioFile.getAbsolutePath());
            }

            AudioInputStream audioInputStream;
            try {
                audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            } catch (UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
                throw new IOException("无法加载音频文件：" + e.getMessage());
            }

//            获取wav文件的总时长
            long durationInMillis = getWavDuration(new File(audioPath));

            // 计算每句的时长
            List<Integer> durations = calculateDurations(sentences, (int) durationInMillis);

            //生成字幕文件
            try {
                generateSrtFile(sentences, durations, srtFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //初始化视频
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

            int changeImage = (int) (durationInMillis / images.size()) + 1;



            int framesPerImage = 30 * changeImage; // 每2秒切换一个图片，帧率为30
//            log.error("每个图片播放的时间", framesPerImage);
            int totalFrames = framesPerImage * images.size();

            for (int i = 0; i < totalFrames; i++) {
                int imageIndex = i / framesPerImage;
                BufferedImage image = images.get(imageIndex);
                // 定义路径
                int[] path = definePath(image.getWidth(), frameWidth, changeImage);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
        //给视频添加字幕
        mergeSRT(radioPath, outputPath);

        return null;
    }

    /**
     * mp4合成字幕
     * @param radioPath 原mp4路径
     * @param outputPath 生成的mp4路径
     */
    public void mergeSRT(String radioPath, String outputPath) {
        log.info("=======字幕合成开始========");
        String name = "";
        try {
            name = UUID.randomUUID().toString();
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
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("Error: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

// 启动输入流和错误流读取线程
            inputThread.start();
            errorThread.start();

// 等待外部进程执行完成
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
        } catch (Exception e) {
            log.error("字幕合成失败！");
            log.error(e.getMessage());
            e.printStackTrace();
        }
        log.info("=======字幕合成结束========");
    }

    // 计算每句的时长
    private static List<Integer> calculateDurations(List<String> sentences, int totalDuration) {
        List<Integer> durations = new ArrayList<>();
        int totalLength = sentences.stream().mapToInt(String::length).sum();

        for (String sentence : sentences) {
            int duration = (int) ((double) sentence.length() / totalLength * totalDuration);
            durations.add(duration);
        }

        return durations;
    }

    // 生成.srt字幕文件
    private void generateSrtFile(List<String> subtitles, List<Integer> durations, String fileName) throws IOException {
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
    }

    // 格式化时间
    private String formatTime2(int milliseconds) {
        int hours = milliseconds / 3600000;
        int minutes = (milliseconds % 3600000) / 60000;
        int seconds = (milliseconds % 60000) / 1000;
        int millis = milliseconds % 1000;

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }
    /**
     * 获取图片（coze）
     * @param content
     * @return
     */
    private List<String> getPicture(String content) {
        // Replace with your Personal_Access_Token, Bot_Id, and UserId
        String personalAccessToken = "pat_GBSt86m4MdawUCdecLg4Z00crnLzY8U0bhTBFoMCZK6WkMGAA6f30W1CYh95l9fD";
        String botId = "7366443219188170804";
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
    private void pollForImages(String conversationId, String chatId, String personalAccessToken,List pictureList)  {
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
                cn.hutool.json.JSONObject jsonResponse = new cn.hutool.json.JSONObject(responseBody);
                if (jsonResponse.getInt("code") == 0) {
                    JSONArray data = jsonResponse.getJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.size(); i++) {
                            cn.hutool.json.JSONObject message = data.getJSONObject(i);
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
    public void extractImageUrls(String jsonString,List pictureList)  {
        // 正则表达式，用于匹配 [Image] 标识符后的 URL
        Pattern pattern = Pattern.compile("\\[Image.*?\\]\\((.*?)\\)");
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
        String name = UUID.randomUUID().toString();
        textFileName = name + ".txt";
        String filePath = textFilePath + textFileName;

        try (FileWriter fileWriter = new FileWriter(filePath);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将lame文件转为wav
     * @param name
     */
    public void toWav(String name) {
        try {
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
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("Error: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

// 启动输入流和错误流读取线程
            inputThread.start();
            errorThread.start();

// 等待外部进程执行完成
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
        } catch (Exception e) {
            log.error("字幕合成失败！");
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 给字幕配背景音，合并为一个音频文件
     * @param voice 字幕朗读的音频文件路径
     * @param background 背景音乐的音频文件路径
     * @param output 输出文件的音频文件路径
     */
    public void mergeBackground(String voice, String background, String output) {
        log.info("字幕配背景音开始===");
        try {
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
                    e.printStackTrace();
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("Error: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

// 启动输入流和错误流读取线程
            inputThread.start();
            errorThread.start();

// 等待外部进程执行完成
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            log.info("字幕配背景音结束===");

        } catch (Exception e) {
            log.error("字幕背景音合成失败！");
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private List<BufferedImage> loadImages(List<String> directoryPath) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        for (int i = 0; i < directoryPath.size(); i++) {
            URL imageUrl = new URL(directoryPath.get(i));
            images.add(ImageIO.read(imageUrl));
        }
        return images;
    }

    private int[] definePath(int imageWidth, int frameWidth, int per) {
        // 这里定义路径，返回一个表示x坐标的数组
        // 示例：简单的从左到右移动
        int pathLength = 30 * 2 * per; // 增加路径数组的长度以减慢移动速度
        int[] path = new int[pathLength];
//        for (int i = 0; i < pathLength; i++) {
//            path[i] = i * (imageWidth - frameWidth) / (pathLength - 1);
//        }
        return path;
    }

    private void convertMp3ToWav(String mp3FilePath, String wavFilePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", mp3FilePath, wavFilePath);
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
                throw new IOException("FFmpeg 转换失败，退出码：" + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg 转换过程被中断", e);
        }
    }

    private void addAudioSegment(AudioInputStream audioInputStream, FFmpegFrameRecorder recorder, double frameDuration) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
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
}
