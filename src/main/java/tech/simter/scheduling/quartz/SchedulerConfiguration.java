package tech.simter.scheduling.quartz;

import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.quartz.CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
import static org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;

@Profile("scheduler")
@Configuration
public class SchedulerConfiguration implements ApplicationContextAware, EmbeddedValueResolverAware {
  private static Logger logger = LoggerFactory.getLogger(SchedulerConfiguration.class);
  private ApplicationContext applicationContext;
  private SchedulerFactoryBean schedulerFactory;
  private final List<Trigger> triggers = new ArrayList<>();
  private int jobId = 0;
  private int triggerId = 0;
  private StringValueResolver resolver;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    this.resolver = resolver;
  }

  @PostConstruct
  public void init() throws Exception {
    logger.info("Start initial simter-scheduler...");
    // Get all bean with @CronScheduled annotation, then schedule it
    Collection<Object> schedulers = applicationContext.getBeansWithAnnotation(CronScheduled.class).values();
    for (Object v : schedulers) {
      CronScheduled cfg = v.getClass().getAnnotation(CronScheduled.class);
      String cron = resolver.resolveStringValue(cfg.value());
      logger.info("Initial scheduler '{}' with cron '{}'", v.getClass().getName(), cron);

      // create job
      MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
      jobDetail.setTargetObject(v);
      jobDetail.setTargetMethod("execute"); // Fixed to invoke this method name
      jobDetail.setName("job-" + (++jobId));
      jobDetail.afterPropertiesSet();

      // create job's trigger
      CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
      trigger.setJobDetail(jobDetail.getObject());
      trigger.setCronExpression(cron);
      trigger.setName("trigger-" + (++triggerId));
      trigger.setStartDelay(500); //  delay 0.5s
      trigger.setMisfireInstruction(MISFIRE_INSTRUCTION_DO_NOTHING); // avoid twice started
      trigger.afterPropertiesSet();

      // keep it for SchedulerFactoryBean initial
      triggers.add(trigger.getObject());
    }
    logger.info("Finished initial simter-scheduler. totalCount={}", schedulers.size());
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