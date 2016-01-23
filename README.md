# Basic MS o365 graph REST API tester "scratch" (throwaway) useful for development and discovery phases

## Configuration

Externalize `src/main/resources/application.yml` config file to the local filesystem
into a location of choice. Configure necessary azure property values. Pass `spring.config.location` property to Spring Boot or set `SPRING_CONFIG_LOCATION` environment variable.
See the [relevant documentation here](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-properties-and-configuration.html#howto-change-the-location-of-external-properties)

## To run it locally

After cloning from git remote, and externalizing and configuring `application.yml`, run from the command line:

    ./gradlew clean bootRun

This is a simple "command runner" app which outputs information of interest to the console. Examine console output for details.
