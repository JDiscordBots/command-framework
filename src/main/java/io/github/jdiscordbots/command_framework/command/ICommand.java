package io.github.jdiscordbots.command_framework.command;

import java.util.List;

public interface ICommand
{
	void action(CommandEvent event);

	default boolean allowExecute(CommandEvent event)
	{
		return true;
	}

	String help();
	
	List<ArgumentTemplate> getExpectedArguments();
}