package tech.simter.scheduling.quartz;

import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.quartz.CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
import static org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;

@Profile("scheduler")
@Configuration
public class SchedulerConfiguration implements ApplicationContextAware {
  private ApplicationContext applicationContext;
  private SchedulerFactoryBean schedulerFactory;
  private final List<Trigger> triggers = new ArrayList<>();
  private int jobId = 0;
  private int triggerId = 0;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void init() throws Exception {
    // Get all bean with @CronScheduled annotation, then schedule it
    for (Object v : applicationContext.getBeansWithAnnotation(CronScheduled.class).values()) {
      CronScheduled cfg = v.getClass().getAnnotation(CronScheduled.class);

      // create job
      MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
      jobDetail.setTargetObject(v);
      jobDetail.setTargetMethod("execute"); // Fixed to invoke this method name
      jobDetail.setName("job-" + (++jobId));
      jobDetail.afterPropertiesSet();

      // create job's trigger
      CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
      trigger.setJobDetail(jobDetail.getObject());
      trigger.setCronExpression(cfg.value());
      trigger.setName("trigger-" + (++triggerId));
      trigger.setStartDelay(500); //  delay 0.5s
      trigger.setMisfireInstruction(MISFIRE_INSTRUCTION_DO_NOTHING); // avoid twice started
      trigger.afterPropertiesSet();

      // keep it for SchedulerFactoryBean initial
      triggers.add(trigger.getObject());
    }
  }

  /**
   * Register a {@link SchedulerFactoryBean} bean.
   * <p>
   * This method must be invoke after {@link SchedulerConfiguration#init()}, or it will throw
   * {@link IllegalArgumentException} to notice no usable schedule job to start.
   *
   * @return the schedulerFactory bean
   */
  @Bean
  public SchedulerFactoryBean schedulerFactoryBean() {
    schedulerFactory = new SchedulerFactoryBean();
    schedulerFactory.setJobFactory(jobFactory());
    schedulerFactory.setOverwriteExistingJobs(true);

    if (!triggers.isEmpty()) schedulerFactory.setTriggers(triggers.toArray(new Trigger[0]));
    else throw new IllegalArgumentException("No usable schedule job to start.");
    return schedulerFactory;
  }

  /**
   * For auto inject spring bean to {@link MethodInvokingJob}
   *
   * @return the jobFactory bean
   */
  @Bean
  public JobFactory jobFactory() {
    return new AutowiringSpringBeanJobFactory();
  }
}