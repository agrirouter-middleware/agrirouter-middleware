FROM maven:3.8-openjdk-17 as build
WORKDIR /usr/src
COPY . .

ARG MY_GITHUB_USER
ARG MY_GITHUB_TOKEN

RUN mvn --batch-mode --update-snapshots --settings ci/settings.xml verify

FROM openjdk:17
WORKDIR /srv
COPY --from=build /usr/src/agrirouter-middleware-application/target/agrirouter-middleware.jar .

CMD java -jar /srv/agrirouter-middleware.jar
