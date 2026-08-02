package com.bullla.pix.application.port;

import java.time.Duration;

public interface IPixMetricsRecorder {

    void recordCompleted();

    void recordFailed();

    void recordRetryScheduled();

    void recordPartnerCall(Duration latency, boolean success);
}
