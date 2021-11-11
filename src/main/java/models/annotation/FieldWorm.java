package models.annotation;
import models.ConstraintsWorm;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FieldWorm {
	String Name();
	ConstraintsWorm[] constraints();
}
