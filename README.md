## Biller Management

Provide API for Biller Management

### Requirement :

1. Java JDK 8
2. MySQL
3. Maven

### How to run :

1. Run using maven `mvn spring-boot:run` at root directory


### How to deploy :
1. Run using maven `mvn clean package -D skipTests` at root directory
2. Run using docker `docker build --tag [imageName:tag] [Dockerfile directory] ` e.g `docker build --tag es2b/bm-dev:1.28 .`
3. Run using docker `docker push  [imageName:tag]` e.g `docker push es2b/bm-dev:1.28`
4. Login to docker server 
5. Run using docker `docker pull  [imageName:tag]` e.g `docker pull es2b/bm-dev:1.28`
6. Run using docker `docker container run -d --env "MY_IPS=$(hostname -I)" -p 8881:8881 [imageName:tag]` e.g `docker container run -d --env "MY_IPS=$(hostname -I)" -p 8881:8881 es2b/bm-dev:1.28`


### How to create docker network :
#### To Connect Between Docker Container
help : `docker network --help`
1. Run using docker `docker network create [networkName]` e.g `docker network create bm_network`
2. Run using docker `docker network connect [networkName] [containerName]`