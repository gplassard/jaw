package fr.gplassard.jaw.infra;

import java.time.Instant;

public interface Clock {
    Instant now();

    void sleep(long millis);
}
