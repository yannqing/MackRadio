package com.yannqing.mackradio.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.yannqing.mackradio.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Objects;

/**
 * 处理结果数据的类
 */
@Slf4j
public class ResultHandler {
    private static String resourcePath;

    static {
        try {
            resourcePath = Objects.requireNonNull(ResultHandler.class.getResource("/")).toURI().getPath();
            if (resourcePath != null) {
                resourcePath = resourcePath.replaceAll("target/classes", "src/main/resources");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void clear(){
        File resourceRootPath = new File(resourcePath + "output");
        if (resourceRootPath.exists() && resourceRootPath.isDirectory()) {
            try {
                FileUtils.cleanDirectory(resourceRootPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void respDataPostProcess(JSONObject respData){
        clear();
        if (respData == null || respData.isEmpty()) {
            return;
        }

        String taskId = (String) JSONPath.eval(respData, "$.header.task_id");
        String audioBase64 = (String) JSONPath.eval(respData, "$.payload.audio.audio");
        String encoding = (String) JSONPath.eval(respData, "$.payload.audio.encoding");

        byte[] decode = Base64.getDecoder().decode(audioBase64);
        String audioUrl = new String(decode);
        log.info("audio download url = {}", audioUrl);

        // 下载结果数据并写成文件，本例中选择用 task id 作为文件名称
        byte[] bytes = HttpUtil.getBytes(audioUrl);
        String filePath = "./music" + File.separator + taskId + "." + encoding;
        log.info("audio save path = {}", filePath);
        try {
            FileUtils.writeByteArrayToFile(new File(filePath), bytes, false);
        } catch (IOException e) {
            log.error("write file failed", e);
        }

    }

}
