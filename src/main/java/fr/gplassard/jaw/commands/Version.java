package fr.gplassard.jaw.commands;

import picocli.CommandLine;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "version"
)
public class Version implements Callable<Integer> {

    @Override
    public Integer call() throws IOException {
        Properties props = new Properties();
        try (var resource = this.getClass().getClassLoader().getResourceAsStream("gradle.properties")) {
            props.load(resource);
        }
        System.out.println("jaw v" + props.get("version"));
        return 0;
    }
}
