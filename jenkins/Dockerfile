FROM jenkins:1.642.2
MAINTAINER Praqma <info@praqma.com>

USER root
# Install gcc
RUN apt-get update && apt-get install -y build-essential

USER jenkins

# Install Jenkins plugins
COPY plugins.txt /usr/share/jenkins/plugins.txt
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/plugins.txt

# Prepare for creation of seed job:
# The job is created by running a custom script in /usr/share/jenkins/init.groovy.d
COPY seed/createSeedJob.groovy /usr/share/jenkins/ref/init.groovy.d/
