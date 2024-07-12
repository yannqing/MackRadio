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

@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    private VideoService videoService;

    @PostMapping("/mp4")
    public BaseResponse<Object> getMp4(String text, HttpServletRequest request) throws Exception {
//        videoService.getMp4(text);
        String mp4Name = videoService.getMp4(text, request);
        return ResultUtils.success(Code.SUCCESS, mp4Name, "生成成功！");
    }
}
