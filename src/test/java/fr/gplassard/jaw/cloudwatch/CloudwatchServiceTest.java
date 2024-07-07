package fr.gplassard.jaw.cloudwatch;

import fr.gplassard.jaw.core.Interruptor;
import fr.gplassard.jaw.infra.Clock;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;

import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudwatchServiceTest {

    @Mock
    private Clock clock;
    @Mock
    private CloudWatchLogsClient client;
    @Mock
    private Interruptor interruptor;
    @Captor
    private ArgumentCaptor<FilterLogEventsRequest> logsRequestCaptor;
    @InjectMocks
    private CloudWatchService cloudWatchService;

    private Integer counter = 0;

    @BeforeEach
    public void beforeEach() {
        this.counter = 0;
    }

    @Test
    public void shouldMakeNoCallsIfInterruptorReturnsFalse() {
        when(clock.now()).thenReturn(Instant.parse("2011-10-05T14:48:00.000Z"));

        cloudWatchService.watchLogs("loggroup", () -> false, (event) -> {
        });

        verifyNoInteractions(client);
    }

    @Test
    public void shouldSendEventsToConsumer() {
        var now = Instant.parse("2011-10-05T14:48:00.000Z");

        when(interruptor.shouldContinue()).thenReturn(true, true, true, false);
        when(clock.now()).thenReturn(now);
        when(client.filterLogEvents(any(FilterLogEventsRequest.class))).thenReturn(
                logsResponse(logEvent(now, "hello")),
                logsResponse(logEvent(now, "world")),
                logsResponse(logEvent(now, "!"))
        );

        var messages = new ArrayList<String>();
        cloudWatchService.watchLogs("loggroup", interruptor, event -> messages.add(event.message()));

        verify(client, times(3)).filterLogEvents(any(FilterLogEventsRequest.class));
        assertThat(messages).contains("hello", "world", "!");
    }

    @Test
    public void shouldDeduplicatesSameLogsFromMultipleResponses() {
        var now = Instant.parse("2011-10-05T14:48:00.000Z");
        var hello = logEvent(now, "hello");
        var world = logEvent(now, "world");

        when(interruptor.shouldContinue()).thenReturn(true, true, true, false);
        when(clock.now()).thenReturn(now);
        when(client.filterLogEvents(any(FilterLogEventsRequest.class))).thenReturn(
                logsResponse(hello), logsResponse(world), logsResponse(hello)
        );

        var messages = new ArrayList<String>();
        cloudWatchService.watchLogs("loggroup", interruptor, event -> messages.add(event.message()));

        verify(client, times(3)).filterLogEvents(any(FilterLogEventsRequest.class));
        assertThat(messages).contains("hello", "world");
    }

    @Test
    public void shouldUseTheLatestTimestampFromAReponseAsStartDateForNextRequest() {
        var now = Instant.parse("2011-10-05T14:48:00.000Z");
        var m0 = logEvent(now.plusMillis(0), "m1");
        var m1 = logEvent(now.plusMillis(1), "m2");
        var m2 = logEvent(now.plusMillis(2), "m3");
        var m3 = logEvent(now.plusMillis(3), "m4");

        when(interruptor.shouldContinue()).thenReturn(true, true, true, true, false);
        when(clock.now()).thenReturn(now);
        when(client.filterLogEvents(any(FilterLogEventsRequest.class))).thenReturn(
                logsResponse(m1, m0), logsResponse(), logsResponse(m2, m3), logsResponse()
        );

        cloudWatchService.watchLogs("loggroup", interruptor, (_event) -> {
        });

        verify(client, times(4)).filterLogEvents(logsRequestCaptor.capture());

        var requests = logsRequestCaptor.getAllValues();
        assertThat(requests).hasSize(4);
        assertThat(requests.get(0).startTime()).isEqualTo(now.toEpochMilli());
        assertThat(requests.get(1).startTime()).isEqualTo(now.toEpochMilli() + 1);
        assertThat(requests.get(2).startTime()).isEqualTo(now.toEpochMilli() + 1);
        assertThat(requests.get(3).startTime()).isEqualTo(now.toEpochMilli() + 3);
    }

    private FilterLogEventsResponse logsResponse(FilteredLogEvent... events) {
        return FilterLogEventsResponse.builder()
                .events(events)
                .build();
    }

    private FilteredLogEvent logEvent(Instant logTime, String message) {
        this.counter++;
        return FilteredLogEvent.builder()
                .message(message)
                .ingestionTime(logTime.toEpochMilli())
                .timestamp(logTime.toEpochMilli())
                .eventId("event_" + StringUtils.leftPad(counter.toString(), 3, "0"))
                .build();
    }

}
