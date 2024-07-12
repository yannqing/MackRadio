package com.yannqing.mackradio.service;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

public interface VideoService {

    String getMp4(String text, HttpServletRequest request) throws IOException, InterruptedException;

    void change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath);

    List<String> getPicture(String content);

    String getRandomBackgroundMusic();
}
