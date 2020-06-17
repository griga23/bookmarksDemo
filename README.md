# Bookmarks Demo
Jan's Event Streaming Microservices Demo

![Application Architecture](/arch.png)

![Application View](/app.png)

## Requirements
  * Kubernetes
  * Helm
  * Java 11 with Maven
  * Docker

## Install Components

### Confluent Platform with Helm
Install Kafka to Kubernetes using Helm charts
```
helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/ 
helm repo update 
helm install my-confluent-oss confluentinc/cp-helm-charts
```
TEST IT: kubectl port-forward deploy/my-confluent-oss-cp-control-center 9021:9021
Now Control Center should be here http://localhost:9021/

### Install Nginx Ingress service
```
helm install my-nginx stable/nginx-ingress
```
TEST IT: kubectl --namespace ingress get all
This should show you all deployed ingress services.

### Configure Kafka

#### Create Bookmarks topic
Create a topic by executing command in kafka docker container
```
kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash /usr/bin/kafka-topics --zookeeper my-confluent-oss-cp-zookeeper:2181 --topic bookmarks --create --partitions 4 --replication-factor 3
```
TEST IT: kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash /usr/bin/kafka-topics --zookeeper my-confluent-oss-cp-zookeeper:2181 --list
You should see list of topics

### Deploy Microservices to Kubernetes
There are three microservices:
  * Bookmarks Producer
  * Bookmarks Consumer
  * Bookmarks Gateway (optional)


#### Install Bookmarks microservices

Compile and deploy Bookmarks Producer from its directory
```
mvn install -Dmaven.test.skip=true
docker build -t griga/bookmarksproducer .
docker image push griga/bookmarksproducer
kubectl apply -f bookmarksProducer_deploy.yml
```
TEST IT: http://producer.localhost/bookmarksProducer/jan

Compile and deploy Bookmarks Consumer from its directory
```
mvn install -Dmaven.test.skip=true
docker build -t griga/bookmarksconsumer .
docker image push griga/bookmarksconsumer
kubectl apply -f bookmarksConsumer_deploy.yml
```

Compile and deploy Bookmarks Proxy Gateway from its directory
```
mvn install -Dmaven.test.skip=true
docker build -t griga/bookmarksproxy .
docker image push griga/bookmarksproxy
kubectl apply -f bookmarksProxy_deploy.yml
```

## Run URLs in Browser

### REST URL
  * Get host IP of the current Pod
http://consumer.localhost/currentHost
  * Get host IP of the key store for some key
http://consumer.localhost/keyHost/janGoogle
  * Get all values for some user from the current Pod
http://consumer.localhost/bookmarks/jan

  * Get value of some key from the current Pod
http://consumer.localhost/getOneBookmark/janGoogle
  * Get value of some key. Proxy will redirect to the correct Pod automatically
http://proxy.localhost/getOneBookmark/janGoogle

### WEB URL
  * Get all currently available State Store IPs
http://consumer.localhost/processors

  * Get bookmarks for some user from the current Pod
http://consumer.localhost/bookmarksConsumer/jan
  * Get bookmarks for some user from all Pods
http://consumer.localhost/bookmarksConsumerAll/jan


### Inspect
Inspect containers for debugging
```
kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash
```
Run curl inside the container
```
curl bookmarksConsumer:8888/currentHost
curl 10.1.0.249:8888/currentHost
```

## Delete deployments and topics
Delete deployments from Kubernetes
```
kubectl delete deployment bookmarksconsumer
kubectl delete deployment bookmarksproducer
```
Delete topic from Kafka
```
kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash /usr/bin/kafka-topics --zookeeper my-confluent-oss-cp-zookeeper:2181 --topic bookmarks --delete
```
