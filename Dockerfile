# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 🔑 COPY THE SPRING BOOT EXECUTABLE JAR ONLY
COPY --from=builder /app/target/order-service-1.0.0.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
