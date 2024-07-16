package com.yannqing.mackradio.service;

import jakarta.servlet.http.HttpServletRequest;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;

public interface VideoService {

    String getMp4(String text, HttpServletRequest request) throws IOException, InterruptedException, UnsupportedAudioFileException;

    void change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath) throws IOException, UnsupportedAudioFileException, InterruptedException;

    List<String> getPicture(String content);

    String getRandomBackgroundMusic();

    void mergeBackground(String voice, String background, String output) throws InterruptedException, IOException;
}
