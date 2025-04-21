FROM openjdk:21
EXPOSE 8080
ADD target/chatApp.jar chatApp.jar
ENTRYPOINT ["java","-jar","chatApp.jar"]