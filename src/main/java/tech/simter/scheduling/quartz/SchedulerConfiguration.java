package tech.simter.scheduling.quartz;

import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.quartz.CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
import static org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;

@Profile("scheduler")
@Configuration("schedulerConfiguration")
public class SchedulerConfiguration implements ApplicationContextAware, EmbeddedValueResolverAware {
  private static Logger logger = LoggerFactory.getLogger(SchedulerConfiguration.class);
  private ApplicationContext applicationContext;
  private StringValueResolver resolver;
  private List<Trigger> triggers;

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
    triggers = new ArrayList<>();
    logger.info("Start initial simter-scheduler...");
    int totalCount = 0;
    // Get all bean with @CronScheduled annotation, then schedule it
    Collection<Object> schedulers = applicationContext.getBeansWithAnnotation(CronScheduled.class).values();
    for (Object v : schedulers) {
      CronScheduled cfg = v.getClass().getAnnotation(CronScheduled.class);
      String cron = resolver.resolveStringValue(cfg.cron());
      String name = resolver.resolveStringValue(cfg.name());
      String group = resolver.resolveStringValue(cfg.group());
      logger.info("Initial cron='{}', name='{}', group='{}', method='{}({})#execute'", cron, name, group,
        v.getClass().getName(), v.hashCode());

      // Fixed to invoke the 'execute' method name
      CronTrigger trigger = createCronTrigger(v, "execute", cron, name, group);

      // keep it for SchedulerFactoryBean initial
      triggers.add(trigger);
      totalCount++;
    }

    // Get all beanMethod with @CronScheduled annotation, then schedule it
    for (String beanName : applicationContext.getBeanDefinitionNames()) {
      // make sure schedulerFactoryBean init after this method run
      if (beanName.equalsIgnoreCase("schedulerConfiguration") || beanName.equalsIgnoreCase("schedulerFactoryBean")) {
        continue;
      }

      Object bean = applicationContext.getBean(beanName);
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      Method[] methods = targetClass.getDeclaredMethods();
      for (Method m : methods) {
        CronScheduled cfg = m.getAnnotation(CronScheduled.class);
        if (cfg != null) {
          String cron = resolver.resolveStringValue(cfg.cron());
          String name = resolver.resolveStringValue(cfg.name());
          String group = resolver.resolveStringValue(cfg.group());
          logger.info("Initial cron='{}', name='{}', group='{}', method='{}({})#{}'", cron, name, group,
            targetClass.getName(), bean.hashCode(), m.getName());

          CronTrigger trigger = createCronTrigger(bean, m.getName(), cron, name, group);

          // keep it for SchedulerFactoryBean initial
          triggers.add(trigger);
          totalCount++;
        }
      }
    }

    logger.info("Finished initial all @CronScheduled scheduler. totalCount={}", totalCount);
  }

  private int jobId = 0;
  private int triggerId = 0;

  private CronTrigger createCronTrigger(Object bean, String methodName, String cron, String name, String group)
    throws ClassNotFoundException, NoSuchMethodException, ParseException {
    // create job
    MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
    jobDetail.setTargetObject(bean);
    jobDetail.setTargetMethod(methodName);
    jobDetail.setGroup(group);
    jobDetail.setName(name == null || name.isEmpty() ? "simter-job-" + (++jobId) : name);
    jobDetail.afterPropertiesSet();

    // create job's trigger
    CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
    trigger.setJobDetail(jobDetail.getObject());
    trigger.setCronExpression(cron);
    trigger.setGroup(group);
    trigger.setName("simter-trigger-" + (++triggerId));
    trigger.setStartDelay(500); //  delay 500ms
    trigger.setMisfireInstruction(MISFIRE_INSTRUCTION_DO_NOTHING); // avoid twice started
    trigger.afterPropertiesSet();
    return trigger.getObject();
  }

  /**
   * Create a {@link SchedulerFactoryBean} bean.
   * <p>
   * Must make sure this bean init after {@link SchedulerConfiguration#init()} method.
   * Otherwise no task will be scheduled.
   *
   * @return the schedulerFactory bean
   */
  @Bean(name = "schedulerFactoryBean")
  @DependsOn("jobFactory")
  public SchedulerFactoryBean schedulerFactoryBean() {
    SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
    schedulerFactory.setApplicationContext(this.applicationContext);
    schedulerFactory.setJobFactory(jobFactory());
    schedulerFactory.setOverwriteExistingJobs(true);

    schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
    schedulerFactory.setAutoStartup(true);
    schedulerFactory.setSchedulerName("simter-quartz-scheduler");

    // important
    if (triggers.isEmpty()) logger.warn("#### Nothing to schedule ####");
    schedulerFactory.setTriggers(triggers.toArray(new Trigger[0]));

    return schedulerFactory;
  }

  /**
   * For auto inject spring bean to {@link MethodInvokingJob}
   *
   * @return the jobFactory bean
   */
  @Bean(name = "jobFactory")
  public JobFactory jobFactory() {
    return new AutowiringSpringBeanJobFactory();
  }
}