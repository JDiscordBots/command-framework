package io.github.jdiscordbots.command_framework.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.jdiscordbots.command_framework.CommandFramework;

/**
 * Annotation used for discovering commands.
 * A command that should be registered should be annotated with {@link Command}.
 * Only instantiable classes that implement {@link ICommand} should be annotated with {@link Command}.
 * @see ICommand
 * @see CommandFramework
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface Command
{
	/**
	 * Gets the aliases that can be used in order to execute this command
	 *
	 * @return command name and optional aliases
	 */
	String[] value();
}
