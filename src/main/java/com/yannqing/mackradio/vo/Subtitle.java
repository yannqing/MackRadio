package com.yannqing.mackradio.vo;


import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subtitle {
    private int index;
    private long startTime;
    private long endTime;
    private String text;
}