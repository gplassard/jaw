package fr.gplassard.jaw.converters;

import ch.qos.logback.classic.Level;
import picocli.CommandLine;

public class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

    @Override
    public Level convert(String value) {
        return Level.valueOf(value);
    }
}
