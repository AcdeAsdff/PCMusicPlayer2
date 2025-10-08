package com.linearity.pcmusicplayer2.annotations;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface DoubleRange {
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
}