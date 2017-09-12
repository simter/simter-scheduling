package tech.simter.scheduling.quartz;

import org.quartz.CronTrigger;

import java.lang.annotation.*;

/**
 * The Annotation for config the quartz {@link CronTrigger}.
 * <p>
 * The {@link CronScheduled#value()} must be a valid cron expression.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CronScheduled {
  String value() default "";
}