package io.github.jdiscordbots.command_framework;

import java.util.List;

import io.github.jdiscordbots.command_framework.command.Argument;
import io.github.jdiscordbots.command_framework.command.CommandEvent;

/**
 * A class containing all relevant information required for handling a command
 */
final class CommandContainer
{
	public final String invoke;
	public final List<Argument> args;
	public final CommandEvent event;

	/**
	 * Construct a new Container by given command invoke and -event
	 *
	 * @param invoke name/invoke of command
	 * @param event  {@link io.github.jdiscordbots.command_framework.command.CommandEvent CommandEvent}
	 */
	public CommandContainer(String invoke, CommandEvent event)
	{
		this.invoke = invoke;
		this.args = event.getArgs();
		this.event = event;
	}
}