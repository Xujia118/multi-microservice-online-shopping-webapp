# Stage 1: Build all modules
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy the maven wrapper and pom files first to cache dependencies
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY pom.xml .
COPY Common/pom.xml Common/
COPY AuthService/pom.xml AuthService/
COPY AccountService/pom.xml AccountService/
COPY ItemService/pom.xml ItemService/
COPY OrderService/pom.xml OrderService/
COPY PaymentService/pom.xml PaymentService/
COPY GatewayService/pom.xml GatewayService/

# Download dependencies (this layer is cached if poms don't change)
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY Common/ Common/
COPY AuthService/ AuthService/
COPY AccountService/ AccountService/
COPY ItemService/ ItemService/
COPY OrderService/ OrderService/
COPY PaymentService/ PaymentService/
COPY GatewayService/ GatewayService/


# Build the project. This installs 'Common' to the local repo (/root/.m2)
# so the other services can find it during their build.
RUN ./mvnw clean install -DskipTests

# Stage 2: Create the final runtime image for a specific service
# (You can repeat this Stage 2 for each service or use different target names)
FROM eclipse-temurin:17-jre AS auth-service
WORKDIR /app
COPY --from=build /app/AuthService/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS account-service
WORKDIR /app
COPY --from=build /app/AccountService/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS item-service
WORKDIR /app
COPY --from=build /app/ItemService/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS order-service
WORKDIR /app
COPY --from=build /app/OrderService/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS payment-service
WORKDIR /app
COPY --from=build /app/PaymentService/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre AS gateway-service
WORKDIR /app
COPY --from=build /app/GatewayService/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]