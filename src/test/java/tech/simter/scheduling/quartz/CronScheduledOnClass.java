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
@CronScheduled("0/1 * * * * ? *") // for config the quartz CronTrigger
public class CronScheduledOnClass {
  private static Logger logger = LoggerFactory.getLogger(CronScheduledOnClass.class);
  private int count = 0;

  /**
   * The method name must be 'execute'.
   */
  public void execute() {
    logger.debug("{}({})#execute {} - need to disable me in production", getClass().getName(), this.hashCode(), ++count);
  }

  int getCount() {
    return count;
  }
}