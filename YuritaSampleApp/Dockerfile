FROM maven:3.6.1-jdk-8-alpine 
EXPOSE 8080
#spark driver demands specific memory to be reserved
ENV JAVA_OPTS="-Xms1g -Xmx1g"

RUN apk update && apk add --no-cache git

COPY . /YuritaSampleApp
RUN git clone https://github.com/paypal/yurita
WORKDIR /yurita

RUN chmod +x ./gradlew && \
./gradlew clean build && \
./gradlew fatJar && ./gradlew publishToMavenLocal

WORKDIR /YuritaSampleApp
RUN mvn clean package
WORKDIR /YuritaSampleApp/target
ENTRYPOINT exec java $JAVA_OPTS -jar YuritaSampleApp-1.0.0-jar-with-dependencies.jar





