package models.annotation;
import models.enums.EnumConstraintsWorm;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FieldWorm {
	String Name();
	EnumConstraintsWorm[] constraints();
}
