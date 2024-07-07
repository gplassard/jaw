package fr.gplassard.jaw.cloudwatch;


import fr.gplassard.jaw.core.Interruptor;
import fr.gplassard.jaw.infra.Clock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CloudWatchService {
    private final Clock clock;
    private final CloudWatchLogsClient client;

    public List<String> listLogGroups(String prefix) {
        return this.client.describeLogGroups(DescribeLogGroupsRequest.builder()
                        .logGroupNamePrefix(prefix)
                        .build()
                )
                .logGroups()
                .stream().map(LogGroup::logGroupName)
                .toList();
    }

    public void watchLogs(String logGroup, Interruptor interruptor, Consumer<FilteredLogEvent> consumer) {
        Map<String, Long> seenEvents = new HashMap<>();
        Instant start = this.clock.now();
        while (interruptor.shouldContinue()) {
            var response = this.client.filterLogEvents(FilterLogEventsRequest.builder()
                    .startTime(start.toEpochMilli())
                    .logGroupName(logGroup)
                    .build()
            );
            var newEvents = response.events()
                    .stream()
                    .filter(event -> !seenEvents.containsKey(event.eventId()))
                    .toList();

            log.debug("{} : retrieved {} event(s), {} new, start {}", logGroup, response.events().size(), newEvents.size(), start);
            newEvents
                    .forEach(consumer);

            // prepare next request
            var nextStart = newEvents
                    .stream()
                    .max(Comparator.comparing(FilteredLogEvent::timestamp))
                    .map(FilteredLogEvent::timestamp)
                    .map(Instant::ofEpochMilli)
                    .orElse(start);

            start = nextStart;

            seenEvents.entrySet().removeIf(entry -> nextStart.toEpochMilli() > entry.getValue());
            seenEvents.putAll(response.events().stream().collect(Collectors.toMap(FilteredLogEvent::eventId, FilteredLogEvent::timestamp)));
            this.clock.sleep(1_000L);
        }
    }

}
