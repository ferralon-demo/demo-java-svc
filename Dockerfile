# Builds demo-java-svc into a WAR and deploys it on a standalone Tomcat 9 running under
# JDK 11, so a proof sandbox can drive it. The JDK-9+ / standalone-Tomcat-WAR combination is
# a required precondition for the CVE-2022-22965 (Spring4Shell) data-binding path — an
# embedded Spring Boot fat-jar is NOT the affected configuration, which is why this fixture
# ships as a WAR on real Tomcat.
#
# The proof sandbox plants a per-assessment seed at /tmp/tegron_canary and injects
# TEGRON_OOB_URL; a live fire drives the data-binding path to arbitrary execution, which
# reads that seed and beacons it to TEGRON_OOB_URL. A static scan needs none of this — it is
# load-bearing only when the service is detonated in a sandbox (see FIRING.md).
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /src
# Resolve dependencies first so the dependency layer caches independently of source edits.
COPY pom.xml ./
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

FROM tomcat:9-jre11
# curl is installed in the trigger runtime so a model-generated fire can beacon the seed.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
# CMD (not ENTRYPOINT) so the sandbox's long-lived override cleanly REPLACES the start
# command; the trigger argv is the sole thing that drives the ingress.
CMD ["catalina.sh", "run"]
