package com.revature.models.annotation;
import com.revature.models.enums.EnumConstraintsWorm;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldWorm {
	String Name();
	EnumConstraintsWorm[] constraints();
}
