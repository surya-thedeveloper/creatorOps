package com.creatorops.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import java.util.Map;

/**
 * TaskDecorator that copies the MDC context map from the parent submitting thread 
 * to the execution thread pool worker executing the task.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture context of the submitting parent thread
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                // Populate the captured context in the child thread
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // Wipe clean child context upon completion (prevent leaks back to pool)
                MDC.clear();
            }
        };
    }
}
