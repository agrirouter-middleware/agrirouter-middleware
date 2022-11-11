FROM maven:3.8-openjdk-17 as build
WORKDIR /usr/src
COPY . .

ARG GITHUB_TOKEN

RUN git config --global url."https://${GITHUB_TOKEN}@github.com/".insteadOf "https://github.com/"
RUN mvn --batch-mode --update-snapshots --settings ci/github-settings.xml verify
RUN git config --global --unset url."https://${GITHUB_TOKEN}@github.com/".insteadOf

FROM openjdk:17
WORKDIR /srv
COPY --from=build /usr/src/agrirouter-middleware-application/target/agrirouter-middleware.jar .

CMD java -jar /srv/agrirouter-middleware.jar