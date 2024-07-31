FROM amazoncorretto:17-alpine-jdk
LABEL maintainer="yannqing <yannqing.com>"
LABEL version="1.0"
LABEL description="MackRadio Simple AI"
WORKDIR /yannqing/MackRadio/java
VOLUME /yannqing/MackRadio/logs
COPY ./target/MackRadio-0.0.1-SNAPSHOT.jar /tmp/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/tmp/app.jar"]