# 使用 OpenJDK 17 slim 版本作为基础镜像
FROM openjdk:17-slim

# 设置维护者标签
LABEL maintainer="yannqing <yannqing.com>"
LABEL version="1.0"
LABEL description="MackRadio Simple AI"

# 更新包列表并安装 bash
RUN apt-get update && \
    apt-get install -y bash wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /yannqing/MackRadio/java

# 创建一个挂载点
VOLUME /yannqing/MackRadio/logs

# 复制应用程序
COPY ./target/MackRadio-0.0.1-SNAPSHOT.jar /tmp/app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
CMD ["java", "-jar", "/tmp/app.jar"]
