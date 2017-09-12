package tech.simter.scheduling.quartz;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("scheduler")
@ContextConfiguration(classes = {CronSampleScheduler.class, SchedulerConfiguration.class})
public class CronSampleSchedulerTest {
  private static Logger logger = LoggerFactory.getLogger(CronSampleSchedulerTest.class);

  @Inject
  private CronSampleScheduler cronSampleScheduler;

  @Test
  public void doExecute() throws InterruptedException {
    int seconds = 2;
    Thread.sleep(seconds * 1000);
    assertThat(cronSampleScheduler.getCount(), greaterThanOrEqualTo(seconds));
  }

  @Test
  public void change() {
    // to test if a cron expression runs only from Monday to Friday
    org.springframework.scheduling.support.CronTrigger trigger =
      new CronTrigger("0 0 1 L * ?");
    //new CronTrigger("0 0 1 * * MON-FRI");
    Calendar today = Calendar.getInstance();
    today.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss EEEE");
    final Date yesterday = today.getTime();
    logger.info("Yesterday was : {}", df.format(yesterday));
    Date nextExecutionTime = trigger.nextExecutionTime(
      new TriggerContext() {
        @Override
        public Date lastScheduledExecutionTime() {
          return yesterday;
        }

        @Override
        public Date lastActualExecutionTime() {
          return yesterday;
        }

        @Override
        public Date lastCompletionTime() {
          return yesterday;
        }
      });

    logger.info("Next Execution date: {}", df.format(nextExecutionTime));
  }
}