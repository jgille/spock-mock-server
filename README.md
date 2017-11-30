Demonstrates how you can use Spock and MockServer to black box test a service that makes http requests to external dependencies.

## Running locally

### Start Mock-Server as a Docker container
docker run -p 8383:1080 -d --name mocker-server jamesdbloom/mockserver

### Run the service under test
./gradlew bootRun

## Run the tests

./gradlew blackbox-test