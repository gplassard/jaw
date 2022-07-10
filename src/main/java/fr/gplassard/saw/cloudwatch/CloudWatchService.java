package fr.gplassard.saw.cloudwatch;


import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class CloudWatchService {
    private final CloudWatchLogsClient client;
    private static final Integer MAX_INGESTION_DELTA_SECONDS = 5;

    public CloudWatchService(CloudWatchLogsClient client) {
        this.client = client;
    }

    public List<String> listLogGroups(String prefix) {
        return this.client.describeLogGroups(DescribeLogGroupsRequest.builder()
                        .logGroupNamePrefix(prefix)
                        .build()
                )
                .logGroups()
                .stream().map(LogGroup::logGroupName)
                .collect(Collectors.toList());
    }

    public void watchLogs(String logGroup, Consumer<FilteredLogEvent> consumer) {
        Map<String, Long> seenEvents = new HashMap<>();
        Instant start = Instant.now();
        while (true){
            Instant before = Instant.now();
            var response = this.client.filterLogEvents(FilterLogEventsRequest.builder()
                    .startTime(start.toEpochMilli())
                    .logGroupName(logGroup)
                    .build()
            );
            var newEvents = response.events()
                    .stream()
                    .filter(event -> !seenEvents.containsKey(event.eventId()))
                    .collect(Collectors.toList());

            log.debug("{} : retrieved {} events, from which {} are new", logGroup, response.events().size(), newEvents.size());
            newEvents
                    .forEach(consumer);

            // prepare next request
            var nextStart = newEvents
                    .stream()
                    .max(Comparator.comparing(FilteredLogEvent::timestamp))
                    .map(FilteredLogEvent::timestamp)
                    .map(Instant::ofEpochMilli)
                    .orElse(before)
                    .minus(MAX_INGESTION_DELTA_SECONDS, ChronoUnit.SECONDS);
            start = nextStart;

            seenEvents.entrySet().removeIf(entry -> nextStart.toEpochMilli() > entry.getValue());
            seenEvents.putAll(response.events().stream().collect(Collectors.toMap(FilteredLogEvent::eventId, FilteredLogEvent::timestamp)));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
