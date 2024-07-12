
# 文字生成视频 TTV（Text to Video）

## v1.0

### 用户
1. session校验，存储到本地浏览器

### 视频处理

1. 输入文字不能超过`1000字符`
2. 将String文本转为txt文件（供讯飞接口使用）
3. 调用讯飞模型，读取txt文件，生成`lame`音频
4. 调用`toWav脚本`将`lame`转为`wav`格式的音频
5. 调用`merge脚本`将文字朗读的音频搭配随机音乐生成新的`wav`音频文件
6. 使用`coze`根据输入的文本生成图片
7. 对输入的文本进行处理，将其进行断句组合为`List<String>`
8. 加载上述生成的音频文件
9. 根据音频文件的总时长以及每一句文字的长短，设置每一句字幕的显示时间，生成srt字幕文件
10. 使用ffmpeg初始化视频（30帧，1000000比特率，mp4格式等）
11. 根据srt文件（或音频文件）的总时长，以及图片的数量来确定一张图片出现的时间
12. 一张图片出现30次，按帧将图片以及音频写入视频，并设置镜头上下移动6帧切换一个图片
13. 完成视频编码，输出mp4
14. 调用`main脚本`将字幕文件与上述mp4合成，生成最终mp4

**注意：一定要先输出mp4再合成字幕生成新的mp4，不然字幕会随着镜头移动而出现混乱**

### 脚本解释

#### toWav脚本
``` shell
#!/bin/bash
# 使用 $1 和 $2 来接收命令行参数
lame_path="$1"
wav_path="$2"
# 调试输出
echo "输入文件: ${lame_path}"
echo "输出文件: ${wav_path}"
# 执行ffmpeg命令
./ffmpeg -i "${lame_path}" "${wav_path}"
```
#### merge脚本
``` shell
voice="$1"
background="$2"
output="$3"

./ffmpeg -i "${voice}" -i "${background}" -filter_complex "[1]volume=0.3[a];[0][a]amix=inputs=2:duration=first:dropout_transition=3" "${output}"

```
#### main脚本
``` shell
#!/bin/bash

# 使用 $1 和 $2 来接收命令行参数
srt_path="$1"
video_path="$2"
output_path="$3"

./ffmpeg -i "${video_path}" -vf "subtitles=${srt_path}:force_style='Fontsize=18,PrimaryColour=&H0000b8e6,MarginV=20,Fontname=Arial,OutlineColour=&H00000000,Outline=1'" -c:a copy -f mp4 -metadata charset=utf-8 "${output_path}"
```

### 待优化事项
1. session设置过期时间
2. 实现单用户会话策略
3. 设置访问限制：12小时内限制50次
4. 并发调用生成视频的接口

## v1.1


### 新增接口
1. `/user/getAccessTimes`获取用户可访问次数，无参

### 用户权限控制
> + 单用户登录：同一账号，同时只能在一个设备上登录。后面登录会把前面的状态挤掉
> + session过期时间：3天
> + `简单AI` 访问次数限制：12小时限制50次


### redis存储
1. session存储格式：`spring:session:sessions:{sessionId}`
2. session存储类型：Hash


### 响应码
> + 参数错误：500
> + 认证失败：50000   （泛指session出现问题，权限校验失败，不包括登录时的参数校验失败）
> + 正常：200



## 其他

1. 服务器启动jar命令
```shell
nohup java -jar xxx.jar &
```
2. 上传文件到服务器（jar过大）
```shell
scp -r -i "\path\to\dog.pem" "\path\to\MackRadio-0.0.1-SNAPSHOT.jar"  root@xxx.xx.xx.xxx:~/
```
