package fr.gplassard.jaw;

import fr.gplassard.jaw.commands.Version;
import fr.gplassard.jaw.commands.Watch;
import picocli.CommandLine;

@CommandLine.Command(
        name = "jaw",
        subcommands = {
                Version.class,
                Watch.class,
                CommandLine.HelpCommand.class
        }
)
public class Jaw {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Jaw()).execute(args);
        System.exit(exitCode);
    }
}
