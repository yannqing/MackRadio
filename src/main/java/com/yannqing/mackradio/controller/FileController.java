package com.yannqing.mackradio.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
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

}