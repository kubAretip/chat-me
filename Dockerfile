FROM openjdk:11.0.2-jre-slim
COPY target/chat-me-0.0.1-SNAPSHOT.jar .
CMD /usr/bin/java -Dlogging.path=/log/ -Xmx400m -Xms400m -jar chat-me-0.0.1-SNAPSHOT.jar
EXPOSE 8080