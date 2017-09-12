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
@ContextConfiguration(classes = {CronSampleScheduler.class, SchedulerConfiguration.class})
public class CronSampleSchedulerTest {
  @Inject
  private CronSampleScheduler cronSampleScheduler;

  @Test
  public void doExecute() throws InterruptedException {
    int seconds = 2;
    Thread.sleep(seconds * 1000);
    assertThat(cronSampleScheduler.getCount(), greaterThanOrEqualTo(seconds));
  }
}