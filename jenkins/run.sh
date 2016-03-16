#!/bin/bash
docker stop jenkins
docker rm -v jenkins
docker run -d -p 8080:8080 -p 50000:50000 \
              --name jenkins praqma/submod-env-jenkins
echo Following logs, Ctrl+C to quit
docker logs -f jenkins
