package fr.gplassard.jaw.infra;

import java.time.Instant;

public class RealClock implements Clock {
    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
