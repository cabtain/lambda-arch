# Lambda architecture

![Alt text](diagram.png?raw=true "Lambda architecture")

Read about the project [here](https://dzone.com/articles/lambda-architecture-how-to-build-a-big-data-pipeli)

Watch the videos demonstrating the project <a href="https://dzone.com/articles/lambda-architecturehow-to-build-a-big-data-pipelin" target="_blank">here</a>

Our Lambda project receives real-time IoT Data Events coming from Connected Vehicles, 
then ingested to Spark through Kafka. Using the Spark streaming API, we processed and analysed 
IoT data events and transformed them into vehicle information.
While simultaneously the data is also stored into HDFS for Batch processing. 
We performed a series of stateless and stateful transformation using Spark streaming API on 
streams and persisted them to Cassandra database tables. In order to get accurate views, 
we also perform a batch processing and generating a batch view into Cassandra.
We developed responsive web traffic monitoring dashboard using Spring Boot, 
SockJs and Bootstrap which get the views from the Cassandra database and push to the UI using web socket.


All component parts are dynamically managed using Docker, which means you don't need to worry 
about setting up your local environment, the only thing you need is to have Docker installed.

System stack:
- Java 8
- Maven
- ZooKeeper
- Kafka
- Cassandra
- Spark
- Docker
- HDFS


The streaming part of the project was done from iot-traffic-project [InfoQ](https://www.infoq.com/articles/traffic-data-monitoring-iot-kafka-and-spark-streaming)

## How to use
*  Set the KAFKA_ADVERTISED_LISTENERS with your IP in the docker-compose.yml
* `mvn package`
* `docker-compose -p lambda up`
*  Wait all services be up and running, then...
* `./project-orchestrate.sh`
* Run realtime job `docker exec spark-master /spark/bin/spark-submit --class com.apssouza.iot.processor.StreamingProcessor  --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar`
* Run the traffic producer `java -jar iot-kafka-producer/target/iot-kafka-producer-1.0.0.jar`
* Run the service layer (Web app) `java -jar iot-springboot-dashboard/target/iot-springboot-dashboard-1.0.0.jar` 
* Access the dashboard with the data http://localhost:3000/
* Run batch job `docker exec spark-master /spark/bin/spark-submit --class com.apssouza.iot.processor.BatchProcessor --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar`

### Miscellaneous

#### Spark
spark-submit --class StreamingProcessor --packages org.apache.kafka:kafka-clients:0.10.2.2 --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar
Add spark-master to /etc/hosts pointing to localhost
/spark/bin/spark-submit 

#### Submit a job to master
- mvn package
- `spark-submit --class com.apssouza.iot.processor.StreamingProcessor --master spark://spark-master:7077 iot-spark-processor/target/iot-spark-processor-1.0.0.jar`


#### GUI
http://localhost:8080 Master
http://localhost:8081 Slave


### HDFS

Comands https://hortonworks.com/tutorial/manage-files-on-hdfs-via-cli-ambari-files-view/section/1/

Open a file - http://localhost:50070/webhdfs/v1/path/to/file/file.csv?op=open

Web file handle - https://hadoop.apache.org/docs/r1.0.4/webhdfs.html

#### Commands :
* `hdfs dfs -mkdir /user`
* `hdfs dfs -mkdir /user/lambda`
* `hdfs dfs -put localhost.csv /user/lambda/`
* Access the file http://localhost:50075/webhdfs/v1/user/lambda/localhost.csv?op=OPEN&namenoderpcaddress=namenode:8020&offset=0

#### Gui
http://localhost:50070
http://localhost:50075


### Kafka
* kafka-topics --create --topic iot-data-event --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181
* kafka-console-producer --request-required-acks 1 --broker-list kafka:9092 --topic iot-data-event
* kafka-console-consumer --bootstrap-server kafka:9092 --topic iot-data-event
* kafka-topics --list --zookeeper zookeeper:2181


### Cassandra
- Log in `cqlsh --username cassandra --password cassandra`
- Access the keyspace `use TrafficKeySpace;`
- List data `SELECT * FROM TrafficKeySpace.Total_Traffic;`

## That's all. Leave a star if this project has helped you!

### git clone

`$ sudo su -`

`$ git clone https://github.com/cabtain/lambda-arch.git`

`$ cd lambda-arch/`

Set the KAFKA_ADVERTISED_LISTENERS with your IP(ex. Google GCP internal IP) in the docker-compose.yml

### install maven & build

`$ apt install maven`

`$ mvn package`


### install docker-compose & run

`$ snap install docker`

`$ docker-compose -p lambda up --build`

Wait all services be up and running, then launch another terminal and run below

`$ ./project-orchestrate.sh`

### Run realtime job

`$ docker exec spark-master /spark/bin/spark-submit --class com.apssouza.iot.processor.StreamingProcessor --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar`

### Run the traffic producer

`$ java -jar iot-kafka-producer/target/iot-kafka-producer-1.0.0.jar`

### Run the service layer (Web app)

`$ java -jar iot-springboot-dashboard/target/iot-springboot-dashboard-1.0.0.jar`

Access the dashboard with the data http://34.145.64.35:3000/

### Run batch job

`$ docker exec spark-master /spark/bin/spark-submit --class com.apssouza.iot.processor.BatchProcessor --master spark://spark-master:7077 /opt/spark-data/iot-spark-processor-1.0.0.jar`

http://34.145.64.35:3000/

![image](https://user-images.githubusercontent.com/1121859/119249928-9bbab780-bbd7-11eb-8680-98a1fae08cee.png)



http://34.145.64.35:8080/


![image2021-4-6_12-42-20](https://user-images.githubusercontent.com/1121859/118780764-56d90d00-b8c7-11eb-840e-54743d910e05.png)

http://34.145.64.35:8081/

![image2021-4-6_12-43-4](https://user-images.githubusercontent.com/1121859/118780839-6eb09100-b8c7-11eb-888e-82c641393951.png)

http://34.145.64.35:4040/jobs/

![image2021-4-6_12-44-13](https://user-images.githubusercontent.com/1121859/118780952-89830580-b8c7-11eb-9fe2-648ce2624849.png)

http://34.145.64.35:4040/streaming/batch/?id=1617680605000

![image2021-4-6_12-45-1](https://user-images.githubusercontent.com/1121859/118781094-a6b7d400-b8c7-11eb-96c7-a1de5cdfee99.png)

http://34.145.64.35:4040/jobs/job/?id=485

![image2021-4-6_12-46-23](https://user-images.githubusercontent.com/1121859/118781203-c0f1b200-b8c7-11eb-92de-8631c8cecbe0.png)

http://34.145.64.35:50070/

![image2021-4-6_12-50-19](https://user-images.githubusercontent.com/1121859/118781268-d8309f80-b8c7-11eb-97a8-305c2fb902e1.png)

http://34.145.64.35:50070/explorer.html#/lambda-arch/iot-data-parque

![image2021-4-6_12-51-17](https://user-images.githubusercontent.com/1121859/118781346-eda5c980-b8c7-11eb-8e96-145ac512b9f5.png)

http://34.145.64.35:50070/dfshealth.html#tab-datanode

![image2021-4-6_12-51-55](https://user-images.githubusercontent.com/1121859/118781408-01513000-b8c8-11eb-9076-902671c15892.png)

http://34.145.64.35:50075/datanode.html

![image2021-4-6_12-52-26](https://user-images.githubusercontent.com/1121859/118781502-175ef080-b8c8-11eb-89ca-e8d12ac087c4.png)




