# =========================================================================
# CreatorOps Backend — Multi-Stage Dockerfile
# =========================================================================
# Stage 1 (builder): Full Maven + JDK image compiles and packages the app.
# Stage 2 (runtime): Minimal JRE-only Alpine image runs the JAR.
# This approach produces a final image ~200MB vs ~500MB for a single-stage.
# =========================================================================

# --------------------------------------------------------------------------
# Stage 1: Build
# --------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy dependency descriptor first for Docker layer caching.
# Maven dependencies are only re-downloaded when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B --quiet

# Copy full source and build the production JAR (skip tests — CI runs tests separately).
COPY src ./src
RUN mvn package -DskipTests -B --quiet

# --------------------------------------------------------------------------
# Stage 2: Runtime
# --------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root system user for security.
RUN addgroup --system --gid 1001 creatorops && \
    adduser --system --uid 1001 --ingroup creatorops creatorops

# Copy the built JAR from the builder stage.
COPY --from=builder /build/target/creatorops-backend-*.jar creatorops-backend.jar

# Set file ownership to non-root user.
RUN chown creatorops:creatorops creatorops-backend.jar

USER creatorops

EXPOSE 8080

# SPRING_PROFILES_ACTIVE defaults to 'postgres' (production mode).
# Override at runtime: docker run -e SPRING_PROFILES_ACTIVE=dev ...
ENV SPRING_PROFILES_ACTIVE=postgres

ENTRYPOINT ["java", "-jar", "/app/creatorops-backend.jar"]
