package io.github.jdiscordbots.command_framework;

public interface ICommand
{
	void action(CommandEvent event);

	default boolean allowExecute(CommandEvent event)
	{
		return true;
	}
}
