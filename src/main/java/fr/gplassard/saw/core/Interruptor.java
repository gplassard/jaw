package fr.gplassard.saw.core;

@FunctionalInterface
public interface Interruptor {
    boolean shouldContinue();
}
