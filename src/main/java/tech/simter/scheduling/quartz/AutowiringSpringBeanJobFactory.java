package tech.simter.scheduling.quartz;

import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * A quartz {@link JobFactory} for initial {@link SchedulerConfiguration}.
 * <p>
 * Target to auto inject spring bean to {@link MethodInvokingJobDetailFactoryBean.MethodInvokingJob}.
 * <p>
 * It is initialed by {@link SchedulerConfiguration#jobFactory}.
 *
 * @author RJ
 */
public final class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {
  private transient AutowireCapableBeanFactory beanFactory;

  @Override
  public void setApplicationContext(final ApplicationContext context) {
    beanFactory = context.getAutowireCapableBeanFactory();
  }

  @Override
  protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
    final Object job = super.createJobInstance(bundle);
    beanFactory.autowireBean(job);
    return job;
  }
}