package tech.simter.scheduling.quartz;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({CronScheduledOnClass.class, SchedulerConfiguration.class})
@ActiveProfiles("scheduler")
@Disabled
class CronScheduledOnClassTest {
  @Inject
  private CronScheduledOnClass scheduler;

  @Test
  void test() throws InterruptedException {
    int seconds = 2;
    Thread.sleep(seconds * 1000);
    assertThat(scheduler.getCount()).isGreaterThanOrEqualTo(seconds);
  }
}