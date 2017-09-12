package tech.simter.scheduling.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * For quartz cron job test.
 *
 * @author RJ
 */
@Named
@Singleton
@Profile("scheduler")
@CronScheduled("0/1 * * * * ? *") // for config the quartz CronTrigger
public class CronSampleScheduler {
  private static Logger logger = LoggerFactory.getLogger(CronSampleScheduler.class);
  private int count = 0;

  /**
   * The method name must be 'execute'.
   */
  public void execute() {
    logger.debug("--invoke {} execute {}", this.hashCode(), ++count);
  }

  int getCount() {
    return count;
  }
}