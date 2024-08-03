FROM amazoncorretto:17-alpine-jdk
LABEL maintainer="yannqing <yannqing.com>"
LABEL version="1.0"
LABEL description="MackRadio Simple AI"

RUN wget -O /etc/apk/keys/alpine-devel@lists.alpinelinux.org-5e69f175.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
RUN echo "https://alpine-pkgs.sgerrand.com" >> /etc/apk/repositories
RUN apk update
RUN apk add --no-cache bash
RUN apk add --no-cache bash
WORKDIR /yannqing/MackRadio/java
VOLUME /yannqing/MackRadio/logs
COPY ./target/MackRadio-0.0.1-SNAPSHOT.jar /tmp/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/tmp/app.jar"]