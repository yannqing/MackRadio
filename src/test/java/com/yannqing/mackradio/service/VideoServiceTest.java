package com.yannqing.mackradio.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VideoServiceTest {

    @Resource
    private VideoService videoService;

    @Test
    void getRandomBackgroundMusic() {
        System.out.println(videoService.getRandomBackgroundMusic());
    }


    @Test
    void mergeBackground() {
//        videoService.mergeBackground("C:\\Users\\67121\\video\\music\\240715134948202114208918.wav", "C:\\Users\\67121\\video\\background\\m3.mp3", "C:\\Users\\67121\\video\\test.wav");
    }
}