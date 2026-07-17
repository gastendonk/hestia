docker rm -f hestia
docker run -d -p 4317:4317 -p 4318:4318 -p 8888:8888 -p 8080:8080 -e INFO=Docker-Modus -e RUN=1 -e LANGUAGE=de -v C:\dev\2024\GitRepos\hestia\hestia\config.yaml:/work/config.yaml -v C:\dev\2024\GitRepos\hestia\hestia\AppConfig.properties:/AppConfig.properties:ro --name hestia mwvb.de:5000/hestia
