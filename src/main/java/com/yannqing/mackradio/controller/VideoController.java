package com.yannqing.mackradio.controller;

import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.service.VideoService;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.*;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    private VideoService videoService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));


    @PostMapping("/mp4")
    public CompletableFuture<BaseResponse<String>> getMp4(String text, HttpServletRequest request) throws Exception {
//        videoService.getMp4(text);
//        String mp4Name = videoService.getMp4(text, request);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String mp4Name = videoService.getMp4(text, request);
                return mp4Name;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executorService)
                .thenApply(mp4Name -> ResultUtils.success(Code.SUCCESS, mp4Name, "生成成功！"))
                .exceptionally(ex -> ResultUtils.failure(ex.getMessage()));
//        return ResultUtils.success(Code.SUCCESS, null, "生成成功！");
    }
}
