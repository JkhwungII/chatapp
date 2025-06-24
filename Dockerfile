FROM openjdk:23
EXPOSE 8080
ADD target/chatApp.jar chatApp.jar
ENTRYPOINT ["java","-jar","chatApp.jar"]