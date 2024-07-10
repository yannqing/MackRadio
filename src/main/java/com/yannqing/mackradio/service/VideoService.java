package com.yannqing.mackradio.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface VideoService {

    Object getMp4(String text) throws Exception;

    String getMp42(String text) throws IOException, InterruptedException;

    String change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath);

    public List<String> getPicture(String content);
}
