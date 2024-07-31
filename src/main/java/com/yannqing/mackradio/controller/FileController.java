package com.yannqing.mackradio.controller;

import com.yannqing.mackradio.common.Code;
import com.yannqing.mackradio.utils.ResultUtils;
import com.yannqing.mackradio.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
public class FileController {



    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> getVideo(@PathVariable("filename") String fileName) {
        try {
            Path videoPath = Paths.get("./" + fileName);
            Resource video = new UrlResource(videoPath.toUri());
            if (video.exists() || video.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM) // Or MediaType as needed
                        .body(video);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get/complete")
    public BaseResponse<Object> getCompletion(String fileName) {
        File file = new File("./" + fileName);
        boolean exists = file.exists();
        if (exists) {
            log.info("查询文件成功，文件{}存在", fileName);
            return ResultUtils.success(Code.SUCCESS, true, "文件存在");
        } else {
            log.info("查询文件成功，文件{}不存在", fileName);
            return ResultUtils.failure(Code.FAILURE, false, "文件不存在");
        }
    }

}