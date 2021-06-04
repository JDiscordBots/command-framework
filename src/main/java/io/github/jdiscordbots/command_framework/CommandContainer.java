package io.github.jdiscordbots.command_framework;

import java.util.List;

import io.github.jdiscordbots.command_framework.command.Argument;
import io.github.jdiscordbots.command_framework.command.CommandEvent;

final class CommandContainer
{
	public final String invoke;
	public final List<Argument> args;
	public final CommandEvent event;

	public CommandContainer(String invoke, CommandEvent event)
	{
		this.invoke = invoke;
		this.args = event.getArgs();
		this.event = event;
	}
}