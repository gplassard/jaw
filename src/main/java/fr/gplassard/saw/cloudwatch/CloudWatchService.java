package fr.gplassard.saw.cloudwatch;


import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class CloudWatchService {
    private final CloudWatchLogsClient client;

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
        Set<String> seenEvents = new HashSet<>();
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
                    .filter(event -> !seenEvents.contains(event.eventId()))
                    .collect(Collectors.toList());

            log.debug("{} : retrieved {} events, from which {} are new", logGroup, response.events().size(), newEvents.size());
            newEvents
                    .forEach(consumer);

            // prepare next request
            start = newEvents
                    .stream()
                    .max(Comparator.comparing(FilteredLogEvent::timestamp))
                    .map(FilteredLogEvent::timestamp)
                    .map(Instant::ofEpochMilli)
                    .orElse(before);

            seenEvents.clear();
            seenEvents.addAll(response.events().stream().map(FilteredLogEvent::eventId).collect(Collectors.toList()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
