package io.github.jdiscordbots.command_framework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to identify commands
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Command {
	/**
	 * Get command name and optional aliases
	 *
	 * @return command name and optional aliases
	 */
	String[] value();
}
