# Agrirouter Middleware

## Documentation

### Swagger

The project provides a Swagger documentation and brings its own Swagger-UI that can be accessed using the following url:

http://your-path-to-the-middleware/swagger-ui/index.html

When running the project locally, the Swagger-UI can be found [here](http://localhost:8080/swagger-ui/index.html).

## Development

### Prerequisites for building and running the application

* Java 17
* Maven

### Environment variables

To run the application, the following environment variables have to be set.

| Name               | Description                                 |
|--------------------|---------------------------------------------|
| `MONGODB_HOST`     | Host for the MongoDB.                       |
| `MONGODB_PASSWORD` | Password for the MongoDB.                   |	
| `MONGODB_PORT`     | Port for the MongoDB.                       |	
| `MONGODB_SCHEMA`   | Schema / Database for the MongoDB.          |
| `MONGODB_USER`     | User for the MongoDB.                       |	
| `MYSQL_HOST`       | Host for the Maria DB / MySQL.              |
| `MYSQL_PASSWORD`   | Password for the Maria DB / MySQL.          |	
| `MYSQL_PORT`       | Port for the Maria DB / MySQL.              |	
| `MYSQL_SCHEMA`     | Schema / Database for the Maria DB / MySQL. |
| `MYSQL_USER`       | User for the Maria DB / MySQL.              |

### Authentication for the GitHub packages

To build the project from scratch you need to authenticate for GitHub packages. Please see the
following [website](https://docs.github.com/en/packages/guides/configuring-apache-maven-for-use-with-github-packages)
for more details.

### Run database

Within the `env/database` folder there is a shell script to build and run the database. Just run `build.sh` to create
and run a docker container.

### Create and run the docker image

Creating the docker image is straight-forward.

* Build and install all the dependencies via `mvn clean install`.
* Run `spring-boot:build-image` to create the docker image within the module `agrirouter-middleware-application`.
* Run `docker run -it -p8080:8080 agrirouter-middleware-application:1.0-SNAPSHOT` to run the container locally.
