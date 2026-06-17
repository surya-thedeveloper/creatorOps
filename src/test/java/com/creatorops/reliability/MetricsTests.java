package com.creatorops.reliability;

import com.creatorops.common.event.ContentCreatedEvent;
import com.creatorops.common.event.TaskStatusChangedEvent;
import com.creatorops.common.metrics.MetricsEventListener;
import com.creatorops.common.metrics.MetricsService;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class MetricsTests {

    @Test
    public void testMetricsListenerIncrementOnContentCreated() {
        MetricsService metricsService = mock(MetricsService.class);
        MetricsEventListener listener = new MetricsEventListener(metricsService);

        ContentCreatedEvent event = new ContentCreatedEvent(1L, 1L, 1L, "Content Card", "IDEA");
        listener.onContentCreated(event);

        verify(metricsService, times(1)).incrementContentCreated();
    }

    @Test
    public void testMetricsListenerIncrementOnTaskCompleted() {
        MetricsService metricsService = mock(MetricsService.class);
        MetricsEventListener listener = new MetricsEventListener(metricsService);

        // Not completed task status event
        TaskStatusChangedEvent eventTodo = new TaskStatusChangedEvent(1L, 1L, 1L, 1L, "TODO", "IN_PROGRESS");
        listener.onTaskStatusChanged(eventTodo);
        verify(metricsService, never()).incrementTasksCompleted();

        // Completed task status event
        TaskStatusChangedEvent eventDone = new TaskStatusChangedEvent(1L, 1L, 1L, 1L, "IN_PROGRESS", "DONE");
        listener.onTaskStatusChanged(eventDone);
        verify(metricsService, times(1)).incrementTasksCompleted();
    }
}
