package tech.simter.scheduling.quartz;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig({CronScheduledOnMethod.class, SchedulerConfiguration.class})
@ActiveProfiles("scheduler")
@Disabled
class CronNextTimeTest {
  @Autowired
  private Scheduler scheduler;

  @Test
  void test() throws SchedulerException {
    assertFalse(CronExpression.isValidExpression(""));
    assertFalse(CronExpression.isValidExpression("0 0 0"));
    assertFalse(CronExpression.isValidExpression("0 0 0 * *"));

    assertTrue(CronExpression.isValidExpression("0 0 0 * * ?"));
    assertTrue(CronExpression.isValidExpression("0 0 0 * * ? *"));

    for (String groupName : scheduler.getJobGroupNames()) {
      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();

        // get job's trigger
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        Date prevFireTime = triggers.get(0).getPreviousFireTime();
        Date nextFireTime = triggers.get(0).getNextFireTime();
        assertEquals("testName", jobName);
        assertEquals("SIMTER", jobGroup);
        assertEquals(1000L, nextFireTime.getTime() - prevFireTime.getTime());
        //System.out.println("groupName=" + jobGroup + ", jobName=" + jobName + " - " + nextFireTime);
      }
    }
  }
}