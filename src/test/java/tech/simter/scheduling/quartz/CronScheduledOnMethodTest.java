package tech.simter.scheduling.quartz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("scheduler")
@ContextConfiguration(classes = {CronScheduledOnMethod.class, SchedulerConfiguration.class})
public class CronScheduledOnMethodTest {
  @Inject
  private CronScheduledOnMethod scheduler;

  @Test
  public void test() throws InterruptedException {
    int seconds = 2;
    Thread.sleep(seconds * 1000);
    assertThat(scheduler.getCount(), greaterThanOrEqualTo(seconds));
  }
}