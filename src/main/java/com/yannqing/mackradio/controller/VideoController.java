package com.yannqing.mackradio.controller;

import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.service.VideoService;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.concurrent.*;

@RestController
@RequestMapping("/video")
@Slf4j
public class VideoController {

    @Resource
    private VideoService videoService;

    private ExecutorService executorService = new ThreadPoolExecutor(2, 10, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(50));


    @PostMapping("/mp4")
    public BaseResponse<String> getMp4(String text, HttpServletRequest request) throws IOException, InterruptedException, UnsupportedAudioFileException {
//        log.info("======text:{}======", text);
//        videoService.getMp4(text);
        String mp4Name = videoService.getMp4(text, request);
//        CompletableFuture<BaseResponse<String>> future = CompletableFuture.supplyAsync(() -> {
//                    try {
//                        String mp4Name = videoService.getMp4(text, request);
//                        return mp4Name;
//                    } catch (IOException | InterruptedException e) {
//                        log.error("Failed to get");
//                        throw new RuntimeException(e.getMessage());
//                    }
//                }, executorService)
//                .thenApply(mp4Name -> ResultUtils.success(Code.SUCCESS, mp4Name, "生成成功！"))
//                .exceptionally(ex -> {
//                    throw new RuntimeException(ex.getMessage());
//                });
//        future.join();
//        return future;
        return ResultUtils.success(Code.SUCCESS, mp4Name, "生成成功！");
    }
}
