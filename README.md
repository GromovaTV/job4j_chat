# Chat

**The goal of this project is to create a messenger for communication.**

You can create rooms with other persons. Rooms can be designed for two persons or for many.

## Installation Instructions

### Installing Docker Compose
*1. Download the package:*
````
sudo curl -L "https://github.com/docker/compose/releases/download/1.28.6/docker-compose-$(uname -s)-$(uname -m)" -o
/usr/local/bin/docker-compose
````

*2. Set permissions:*
````
sudo chmod +x /usr/local/bin/docker-compose
````


### Project Setup
*0. Install Maven:*
````
sudo apt-get update
sudo apt-get install maven
````

*1. Clone the project:*
````
git clone https://github.com/GromovaTV/job4j_chat
````

*2. Build the project:*

````
mvn package
````

*3. Build Docker image:*

````
docker build -t job4j_chat .
````

*4. Run the application:*

````
docker-compose up
````

