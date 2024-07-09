# 文字，图片生成视频


## 字幕
```shell
ffmpeg -i C:\Users\67121\Desktop\data\radio\musicVideo.mp4 -vf "subtitles=dialog.srt:force_style='Fontsize=12,PrimaryColour
=&H00FF0000,MarginV=50'" -c:a copy C:\Users\67121\Desktop\data\radio\output.mp4
```

