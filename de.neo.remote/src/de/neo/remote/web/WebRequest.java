package de.neo.remote.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebRequest {
	
	enum Type{ Get, Post};
	
	public String path();
	public String content() default "";
	public String description();
	public Class<?> genericClass() default Object.class;
	
	public Type type() default Type.Get;
}
