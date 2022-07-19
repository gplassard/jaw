package fr.gplassard.saw.commands;

import fr.gplassard.saw.cloudwatch.CloudWatchService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;


import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
        name = "watch"
)
@Slf4j
public class Watch implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "Prefix of log groups to watch")
    private String logGroupPrefix;
    private final CloudWatchService cloudWatchService = new CloudWatchService(CloudWatchLogsClient.create());
    private static final List<String> COLORS = Arrays.asList("green", "red", "blue", "white", "yellow", "cyan");

    @Override
    public Integer call() throws Exception {
        log.debug("Watching log groups {}", this.logGroupPrefix);
        List<String> logGroups = this.cloudWatchService.listLogGroups(this.logGroupPrefix);
        ExecutorService executor = Executors.newFixedThreadPool(logGroups.size());
        String commonPrefix = StringUtils.getCommonPrefix(logGroups.toArray(new String[0]));
        for (int i = 0; i < logGroups.size(); i++) {
            String logGroup = logGroups.get(i);
            String withoutPrefix = logGroup.replace(commonPrefix, "");
            // TODO replace with async and don't create too many threads
            final int index = i;
            executor.submit(() -> this.cloudWatchService.watchLogs(logGroup, event -> {
                String date = Instant.ofEpochMilli(event.timestamp()).toString();
                System.out.println(format(withoutPrefix, event, date, index));
            }));
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        return 0;
    }

    private CommandLine.Help.Ansi.Text format(String withoutPrefix, FilteredLogEvent event, String date, int index) {

        return CommandLine.Help.Ansi.AUTO.text(
                "@|bold," +
                        COLORS.get(index % COLORS.size()) +
                        String.format("[%s][%s] %s", withoutPrefix, date, event.message()) +
                        "|@"
        );
    }

}
