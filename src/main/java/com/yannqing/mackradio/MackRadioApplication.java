package com.yannqing.mackradio;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;

@SpringBootApplication(exclude = {
        GroovyTemplateAutoConfiguration.class})
@MapperScan("com.yannqing.mackradio.mapper")
public class MackRadioApplication {

    public static void main(String[] args) {
        SpringApplication.run(MackRadioApplication.class, args);
    }

}
