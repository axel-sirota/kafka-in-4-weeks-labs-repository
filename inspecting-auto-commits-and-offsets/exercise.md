# Inspecting Offset commits

0- In this folder run:

```
docker-compose up -d
```


1- Run `mvn clean compile`

2- Go to kafka folder and run kafka-topics

```
kafka-topics --create --bootstrap-server localhost:9092 \
--replication-factor 1 --partitions 1 --topic auto

kafka-topics --create --bootstrap-server localhost:9092 \
--replication-factor 1 --partitions 1 --topic sync

kafka-topics --create --bootstrap-server localhost:9092 \
--replication-factor 1 --partitions 1 --topic async
```

## Fist we will test AutoCommit

3- Run the producer that will create messages of disasters and intensity.

```
mvn exec:java -Dexec.mainClass="com.offsets.Producer" -Dexec.cleanupDaemonThreads=false -Dexec.args="auto"
```

4- Run the AutoConsumer that will read from the auto topic and will auto commit

```
mvn exec:java -Dexec.mainClass="com.offsets.AutoConsumer" -Dexec.cleanupDaemonThreads=false
```

5- Kill the consumer after some time. Verify the offsets and compare with the logs:

```
kafka-consumer-groups --bootstrap-server localhost:9092 --all-groups --all-topics --describe
```

6 - Rerun the AutoConsumer simulating it came back up, the logs should show some record offset wasnt processed, meaning data loss.

## Now we test Sync commit

7 - Proceeed similarly (run consumer, run producer, kil consumer, check consumer groups, rerun consumer).
Verify that if a consumer dies within a loop, all the records now will be reprocessed.

## Finally, we test Async commit

8 - Proceeed similarly (run consumer, run producer, kil consumer, check consumer groups, rerun consumer).
Verify that if a consumer dies within a loop, all the records in a poll that have not been processed, will not be processed when the consumer rejoins.

9- Therefore the best strategy if we  is a trade off between polling a lot and commit asynchronously often if we can hanlde data loss, and otherwise sacrifice performance and commit synchronously.

10- Shut down everything:

```
docker-compose down
```

