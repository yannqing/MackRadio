package com.yannqing.mackradio.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface VideoService {

    Object getMp4(String text) throws Exception;

    String getMp42(String text) throws IOException;

    String change(String text, String srtFilePath, String audioPath, String radioPath, String shPath, String outputPath, String imagePath);
}
