FROM maven:3.8-openjdk-17 as build
WORKDIR /usr/src
COPY . .

ARG GITHUB_USER
ARG GITHUB_TOKEN

RUN mvn --batch-mode --update-snapshots --settings m2-settings.xml verify

FROM openjdk:17
WORKDIR /srv
COPY --from=build /usr/src/agrirouter-middleware-application/target/agrirouter-middleware.jar .

CMD java -jar /srv/agrirouter-middleware.jar
