### Monolithic Spring Boot chat application (using websocket)

#### Microservices version
https://github.com/kubAretip/spring-micro-websocket-chat

#### Front-end
Branch <b>master</b> https://github.com/kubAretip/chat-me-angular

#### Technologies
* Spring
* MySql
* FlyWay
* Docker

#### Requirements:
* Install Maven, see https://maven.apache.org/download.cgi
* Install Docker, see https://docs.docker.com/get-docker

#### Steps to run
##### 1. Clone repo
``git clone git@github.com:kubAretip/chat-me.git``
##### 2. Build application
In main folder run 
``mvn clean install``
##### 3. Run
In main folder run
``docker-compose up -d``

Application will be available on http://localhost:8600 