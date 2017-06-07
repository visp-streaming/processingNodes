FROM vispstreaming/processingbase
MAINTAINER Christoph Hochreiner <ch.hochreiner@gmail.com>
ADD target/processingNode-0.2.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
