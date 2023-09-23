package de.neo.remote.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface WebParam {
	
	enum Type{ GetParameter, ReplaceUrl, Header, Payload};
	
	public String name();

	public boolean required() default true;

	public String defaultvalue() default "";

	public Type type() default Type.GetParameter;
}
