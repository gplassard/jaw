package fr.gplassard.jaw;

import fr.gplassard.jaw.commands.Watch;
import fr.gplassard.jaw.infra.VersionProvider;
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
    @CommandLine.Option(names = { "-V", "--version" }, versionHelp = true,
            description = "print version information and exit")
    boolean versionRequested;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Jaw()).execute(args);
        System.exit(exitCode);
    }
}
