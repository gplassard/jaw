package fr.gplassard.jaw.infra;

import picocli.CommandLine;

import java.util.Properties;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        Properties props = new Properties();
        try (var resource = this.getClass().getClassLoader().getResourceAsStream("gradle.properties")) {
            props.load(resource);
        }
        return new String[]{props.getProperty("version")};
    }
}
