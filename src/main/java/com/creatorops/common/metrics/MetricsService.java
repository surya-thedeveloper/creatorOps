package com.creatorops.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter aiRequestsCounter;
    private final Counter aiSuccessCounter;
    private final Counter aiFailuresCounter;
    private final Counter brainstormsGeneratedCounter;
    private final Counter scriptsGeneratedCounter;
    private final Counter contentCreatedCounter;
    private final Counter assignmentsCreatedCounter;
    private final Counter tasksCompletedCounter;

    private final Counter circuitOpenCounter;
    private final Counter retryCounter;

    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.aiRequestsCounter = Counter.builder("creatorops.ai.requests")
                .description("Total number of AI requests made")
                .register(meterRegistry);

        this.aiSuccessCounter = Counter.builder("creatorops.ai.success")
                .description("Total number of successful AI requests")
                .register(meterRegistry);

        this.aiFailuresCounter = Counter.builder("creatorops.ai.failures")
                .description("Total number of failed AI requests")
                .register(meterRegistry);

        this.brainstormsGeneratedCounter = Counter.builder("creatorops.brainstorms.generated")
                .description("Total number of brainstorms generated")
                .register(meterRegistry);

        this.scriptsGeneratedCounter = Counter.builder("creatorops.scripts.generated")
                .description("Total number of scripts generated")
                .register(meterRegistry);

        this.contentCreatedCounter = Counter.builder("creatorops.content.created")
                .description("Total number of content items created")
                .register(meterRegistry);

        this.assignmentsCreatedCounter = Counter.builder("creatorops.assignments.created")
                .description("Total number of assignments created")
                .register(meterRegistry);

        this.tasksCompletedCounter = Counter.builder("creatorops.tasks.completed")
                .description("Total number of tasks completed")
                .register(meterRegistry);

        this.circuitOpenCounter = Counter.builder("creatorops.ai.circuit.open")
                .description("Total number of times the AI circuit breaker opened")
                .register(meterRegistry);

        this.retryCounter = Counter.builder("creatorops.ai.retry.count")
                .description("Total number of AI request retry attempts")
                .register(meterRegistry);
    }

    public void incrementAiRequests() {
        aiRequestsCounter.increment();
    }

    public void incrementAiSuccess() {
        aiSuccessCounter.increment();
    }

    public void incrementAiFailures() {
        aiFailuresCounter.increment();
    }

    public void incrementBrainstormsGenerated() {
        brainstormsGeneratedCounter.increment();
    }

    public void incrementScriptsGenerated() {
        scriptsGeneratedCounter.increment();
    }

    public void incrementContentCreated() {
        contentCreatedCounter.increment();
    }

    public void incrementAssignmentsCreated() {
        assignmentsCreatedCounter.increment();
    }

    public void incrementTasksCompleted() {
        tasksCompletedCounter.increment();
    }

    public void incrementCircuitOpen() {
        circuitOpenCounter.increment();
    }

    public void incrementRetryCount() {
        retryCounter.increment();
    }
}
