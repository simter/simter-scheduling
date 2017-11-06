package tech.simter.scheduling.quartz;

import org.quartz.CronTrigger;

import java.lang.annotation.*;

/**
 * The Annotation for config the quartz {@link CronTrigger}.
 * <p>
 * The {@link CronScheduled#cron()} must be a valid cron expression.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CronScheduled {
  /**
   * The quartz trigger cron expression.
   *
   * @return The Cron expression
   */
  String cron();

  /**
   * The job name.
   *
   * @return The job name
   */
  String name() default "";

  /**
   * The job group.
   *
   * @return The job group
   */
  String group() default "SIMTER";
}