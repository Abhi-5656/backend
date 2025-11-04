package com.wfm.experts.modules.wfm.employee.location.tracking.aop;

import java.lang.annotation.*;

/**
 * Optional marker you can put on any consumer/service method that requires a visible TrackingSession.
 * The aspect already targets common method names, so you don't HAVE to use this.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnsureSessionVisible {
}
