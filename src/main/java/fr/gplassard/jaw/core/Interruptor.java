package fr.gplassard.jaw.core;

@FunctionalInterface
public interface Interruptor {
    boolean shouldContinue();
}
