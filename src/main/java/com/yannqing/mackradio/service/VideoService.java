package com.yannqing.mackradio.service;

import jakarta.servlet.http.HttpServletRequest;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public interface VideoService {

    String getMp4(String text, HttpServletRequest request) throws IOException, InterruptedException, UnsupportedAudioFileException;

    String getMp4Byjava(String text, HttpServletRequest request) throws IOException, InterruptedException, IllegalArgumentException, UnsupportedAudioFileException;

    void change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath, String taskId, String background, List<String> picture) throws IOException, UnsupportedAudioFileException, InterruptedException;

    List<String> getPicture(String content);

    String getRandomBackgroundMusic();

    void mergeBackground(String voice, String background, String output) throws InterruptedException, IOException;

    void writeListToFile(List<String> list, String filePath) throws IOException;
}
