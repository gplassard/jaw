package fr.gplassard.jaw;

import ch.qos.logback.classic.Level;
import fr.gplassard.jaw.commands.Watch;
import fr.gplassard.jaw.converters.LogLevelConverter;
import fr.gplassard.jaw.infra.VersionProvider;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jaw",
        subcommands = {
                Watch.class,
                CommandLine.HelpCommand.class
        },
        versionProvider = VersionProvider.class
)
public class Jaw {
    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true,
            description = "print version information and exit")
    boolean versionRequested;

    @CommandLine.Option(names = {"-L", "--log-level"}, scope = CommandLine.ScopeType.INHERIT)
    public void setVerbose(Level logLevel) {
        Logger appLogger = (Logger) LoggerFactory.getLogger("fr.gplassard");
        appLogger.setLevel(logLevel);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Jaw())
                .registerConverter(Level.class, new LogLevelConverter())
                .execute(args);
        System.exit(exitCode);
    }
}
