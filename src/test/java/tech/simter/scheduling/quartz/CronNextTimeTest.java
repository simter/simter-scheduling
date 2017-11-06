package tech.simter.scheduling.quartz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("scheduler")
@ContextConfiguration(classes = {CronScheduledOnMethod.class, SchedulerConfiguration.class})
public class CronNextTimeTest {
  @Autowired
  private Scheduler scheduler;

  @Test
  public void test() throws InterruptedException, SchedulerException {
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
        assertThat(jobName, is("testName"));
        assertThat(jobGroup, is("SIMTER"));
        assertThat(nextFireTime.getTime() - prevFireTime.getTime(), is(1000L));
        //System.out.println("groupName=" + jobGroup + ", jobName=" + jobName + " - " + nextFireTime);
      }
    }
  }
}