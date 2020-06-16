# bookmarksDemo
Jan's Event Streaming Microservices Demo

Requirements
-Kubernetes
-Helm

Install Components

1) Confluent Platform with Helm
helm repo add confluentinc https://confluentinc.github.io/cp-helm-charts/ 
helm repo update 
helm install my-confluent-oss confluentinc/cp-helm-charts

TEST IT: kubectl port-forward deploy/my-confluent-oss-cp-control-center 9021:9021

2) Install Nginx Ingress service
helm install my-nginx stable/nginx-ingress

TEST IT: kubectl --namespace ingress get all

Configure Kafka

3) Create Bookmarks topic

kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash /usr/bin/kafka-topics --zookeeper my-confluent-oss-cp-zookeeper:2181 --topic bookmarks --create --partitions 4 --replication-factor 3
TEST IT: kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash /usr/bin/kafka-topics --zookeeper my-confluent-oss-cp-zookeeper:2181 --list

Deploy Microservices to Kubernetes

4) Install Bookmarks microservices

mvn install -Dmaven.test.skip=true
docker build -t griga/bookmarksproducer .
docker image push griga/bookmarksproducer
kubectl apply -f bookmarksProducer_deploy.yml

TEST IT: http://producer.localhost/bookmarksProducer/jan

mvn install -Dmaven.test.skip=true
docker build -t griga/bookmarksconsumer .
docker image push griga/bookmarksconsumer
kubectl apply -f bookmarksConsumer_deploy.yml

mvn install -Dmaven.test.skip=true
docker build -t griga/bookmarksproxy .
docker image push griga/bookmarksproxy
kubectl apply -f bookmarksProxy_deploy.yml

5) Test URLs
REST URL
http://consumer.localhost/currentHost
http://consumer.localhost/keyHost/janGoogle

http://consumer.localhost/bookmarks/jan
http://consumer.localhost/getOneBookmark/janGoogle
http://proxy.localhost/getOneBookmark/janGoogle

WEB URL
http://consumer.localhost/processors
http://consumer.localhost/bookmarksConsumer/jan
http://consumer.localhost/bookmarksConsumerAll/jan


6) Inspect
kubectl exec -it bookmarksconsumer-0 -- ls -lrt /data/count-store/counts-app
kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash

curl bookmarksConsumer:8888/currentHost
curl 10.1.0.249:8888/currentHost

kubectl port-forward deploy/bookmarksconsumer 8888:8888

7) Delete deployments and topics
kubectl delete deployment bookmarksconsumer
kubectl delete deployment bookmarksproducer

kubectl exec -c cp-kafka-broker -it my-confluent-oss-cp-kafka-0 -- /bin/bash /usr/bin/kafka-topics --zookeeper my-confluent-oss-cp-zookeeper:2181 --topic bookmarks --delete

