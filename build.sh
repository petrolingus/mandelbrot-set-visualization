./mvnw clean install

docker build --tag petrolingus/process-service:latest ./process-service
docker build --tag petrolingus/ui-service:latest ./ui-service
docker image prune -f

docker push petrolingus/process-service:latest
docker push petrolingus/ui-service:latest