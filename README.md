# ThinkGearReader
===========================================

The purpose of this project is to provide a reader interface for ThinkGear Adapter


Technologies
------------

+ java
+ Spring
+ docker
+ gradle

Prerequisites
--------------


How To Compile
--------------

The service can be compiled with:

```
gradle clean build
```


How To Run
--------------

The service can be executed with:

```
gradle bootRun
```


Successful compilation conditions
--------------
This project uses pmd, findbugs, jacoco to guaranty the quality of the code.

In addition there is a jacoco task that is attached to the build lifecycle that prevents the successful compilation of the project if there is no enought unit test code coverage.

The current minimun coverage percentage is: 80 %


Simulating BrainWaves
---------------------

Thought Kafka console consumer/producer is it possible to capture and replicate the waves Flow, republishing samples 
to the kafka topic [think_gear_reader]

**List Topics from Broker**
```
./bin/kafka-topics --zookeeper localhost:2181 --list
```

**Create a Kafka Consumer to Capture some samples**
```
./bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic think_gear_reader
```

**Saving samples to file**
```
./bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic think_gear_reader > waves.samples
```

**Publishing samples into the topic**
```
./bin/kafka-console-producer --broker-list localhost:9092 --topic think_gear_reader < waves.samples
```

