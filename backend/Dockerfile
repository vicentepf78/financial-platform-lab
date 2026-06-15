FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /build

COPY pom.xml .
COPY shared-kernel/pom.xml shared-kernel/
COPY customer-module/pom.xml customer-module/
COPY account-module/pom.xml account-module/
COPY ledger-module/pom.xml ledger-module/
COPY pix-module/pom.xml pix-module/
COPY billing-module/pom.xml billing-module/
COPY reconciliation-module/pom.xml reconciliation-module/
COPY audit-module/pom.xml audit-module/
COPY monitoring-module/pom.xml monitoring-module/
COPY application/pom.xml application/

RUN mvn -B dependency:go-offline -pl application -am

COPY . .
RUN mvn -B -pl application -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=build /build/application/target/application-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
