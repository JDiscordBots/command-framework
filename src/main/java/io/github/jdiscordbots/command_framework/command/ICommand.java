package io.github.jdiscordbots.command_framework.command;

/**
 * Command interface for creating commands easily
 */
public interface ICommand
{
	/**
	 * Handle command event
	 *
	 * @param event {@link io.github.jdiscordbots.command_framework.command.CommandEvent CommandEvent}
	 */
	void action(CommandEvent event);

	/**
	 * Return whether command can be executed or not
	 *
	 * @param event {@link io.github.jdiscordbots.command_framework.command.CommandEvent CommandEvent}
	 * @return <code>true</code> (default) if command can be executed, otherwise <code>false</code>
	 */
	default boolean allowExecute(CommandEvent event)
	{
		return true;
	}

	/**
	 * Get help of command
	 *
	 * @return usage/help of command
	 */
	String help();
}
