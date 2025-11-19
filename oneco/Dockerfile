# ---------- Build stage ----------
FROM gradle:8.9-jdk17-alpine AS build
WORKDIR /workspace
COPY . .
# 테스트는 생략해 빠르게 이미지 확인
RUN ./gradlew clean bootJar -x test

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre-alpine
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
WORKDIR /app
# 위에서 만들어진 jar를 app.jar로 복사
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]