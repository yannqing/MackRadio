package com.yannqing.mackradio.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    }

    @Test
    void writeListToFile() throws IOException {
//        List<String> list = Arrays.asList("line1", "line2", "line3", "line4");
//        videoService.writeListToFile(list, "./background/" + UUID.randomUUID() + ".txt");
    }
}