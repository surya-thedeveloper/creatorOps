package com.creatorops.reliability;

import com.creatorops.scheduler.DailyHealthSummaryJob;
import com.creatorops.scheduler.OverdueContentScannerJob;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTests {

    @Test
    public void testDailyHealthSummaryJobExecutes() {
        DailyHealthSummaryJob job = new DailyHealthSummaryJob();
        // Simply invoke execute() to verify it runs without throwing any exception
        assertDoesNotThrow(job::execute);
    }

    @Test
    public void testOverdueContentScannerJobExecutes() {
        OverdueContentScannerJob job = new OverdueContentScannerJob();
        // Simply invoke execute() to verify it runs without throwing any exception
        assertDoesNotThrow(job::execute);
    }
}
