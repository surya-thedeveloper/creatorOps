package com.creatorops.common.metrics;

import com.creatorops.common.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MetricsEventListener {

    private final MetricsService metricsService;

    @Autowired
    public MetricsEventListener(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @EventListener
    public void onContentCreated(ContentCreatedEvent event) {
        metricsService.incrementContentCreated();
    }

    @EventListener
    public void onAssignmentCreated(AssignmentCreatedEvent event) {
        metricsService.incrementAssignmentsCreated();
    }

    @EventListener
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        String meta = event.getMetadataJson();
        if (meta != null && meta.contains("\"newStatus\":\"DONE\"")) {
            metricsService.incrementTasksCompleted();
        }
    }

    @EventListener
    public void onAiBrainstormGenerated(AiBrainstormGeneratedEvent event) {
        metricsService.incrementBrainstormsGenerated();
    }

    @EventListener
    public void onAiScriptGenerated(AiScriptGeneratedEvent event) {
        metricsService.incrementScriptsGenerated();
    }
}
