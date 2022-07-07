package fr.gplassard.saw;

import fr.gplassard.saw.commands.Watch;
import picocli.CommandLine;

@CommandLine.Command(
        name = "saw",
        subcommands = {
                Watch.class,
                CommandLine.HelpCommand.class
        }
)
public class Saw {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Saw()).execute(args);
        System.exit(exitCode);
    }
}
