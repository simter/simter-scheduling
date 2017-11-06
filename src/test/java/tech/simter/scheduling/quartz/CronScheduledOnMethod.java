package tech.simter.scheduling.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * For quartz cron job test.
 *
 * @author RJ
 */
@Component
@Profile("scheduler")
public class CronScheduledOnMethod {
  private static Logger logger = LoggerFactory.getLogger(CronScheduledOnMethod.class);
  private int count = 0;

  @CronScheduled(cron = "0/1 * * * * ? *", name = "${app.name1:testName}") // for config the quartz CronTrigger
  public void execute() {
    logger.debug("{}({})#execute {} - need to disable me in production", getClass().getName(), this.hashCode(), ++count);
  }

  int getCount() {
    return count;
  }
}