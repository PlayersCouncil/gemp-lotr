FROM alpine

RUN apk update; \
	apk update --repository=http://dl-cdn.alpinelinux.org/alpine/edge/testing; \
	apk add --no-cache procps; \
	apk add --no-cache net-tools; \
	apk add --no-cache iputils; \
	apk add --no-cache bash; \
	apk add --no-cache util-linux; \
	apk add --no-cache dpkg; \
	apk add --no-cache gzip; \
	apk add --no-cache curl; \
	apk add --no-cache tar; \
	apk add --no-cache binutils; \
	apk add --no-cache freetype; \
	apk add --no-cache fontconfig; \
	apk add --no-cache openjdk14-jdk --repository=http://dl-cdn.alpinelinux.org/alpine/edge/testing; 
		
		
		
#####################################################################
# The following is pulled from the official maven dockerfile:
# https://github.com/carlossg/docker-maven/blob/26ba49149787c85b9c51222b47c00879b2a0afde/openjdk-14/Dockerfile
#####################################################################

ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/root"
ARG SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSkL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# Enables the JRE remote debugging; perhaps comment this out in a production build
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=*:8000,server=y,suspend=n


#####################################################################


RUN apk add --no-cache openrc; \
	apk add --no-cache apache2
	
#RUN nohup java -jar /etc/gemp-lotr/gemp-lotr-async/target/web.jar &


