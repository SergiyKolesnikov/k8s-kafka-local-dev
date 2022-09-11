FROM amazoncorretto:11

ADD target/scala-2.13/k8s-local-dev-assembly-1.0.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
