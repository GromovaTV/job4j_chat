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
cd job4j_chat
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
### Installing K8s
*1. Download:*
````
curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
````

*2. Install:*
````
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
````

### Installing minikube
*1. Download:*
````
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
````

*2. Install:*
````
sudo install minikube-linux-amd64 /usr/local/bin/minikube
````

*3. Set Docker as the default driver for minikube:*
````
minikube config set driver docker
````

### Launching an application on K8s
*1. Start the cluster:*
````
minikube start
````

*2. Create a secret from the file postgresdb-secret.yml:*
````
kubectl apply -f postgresdb-secret.yml
````

*3. Introduce a ConfigMap into the cluster:*
````
kubectl apply -f postgresdb-configmap.yml
````

*4. Start the deployment:*
````
kubectl apply -f postgresdb-deployment.yml
````

*5. Initiate the deployment of the Spring Boot application:*
````
kubectl apply -f spring-boot-deployment.yml
````

*6. Obtain the URL to connect to the service externally:*
````
minikube service spring-boot-service
````
