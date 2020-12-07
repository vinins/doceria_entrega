#!/bin/bash

mvn package
java -cp /tmp/zookeeper-3.4.14/zookeeper-3.4.14.jar:/tmp/zookeeper-3.4.14/lib/slf4j-log4j12-1.7.25.jar:/tmp/zookeeper-3.4.14/lib/slf4j-api-1.7.25.jar:/tmp/zookeeper-3.4.14/lib/log4j-1.2.17.jar:target/doceria_entrega-1.0-SNAPSHOT.jar sist.distribuidos.App