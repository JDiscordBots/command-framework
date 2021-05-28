package io.github.jdiscordbots.command_framework.command;

import java.util.List;

import org.jetbrains.annotations.Contract;

import io.github.jdiscordbots.command_framework.CommandFramework;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

/**
 * Commands should implement this interface.
 * 
 * Commands can be executed as slash commands or as text messages.
 * @see Command
 * @see ICommand#action(CommandEvent)
 * @see CommandEvent
 * @see CommandFramework
 */
public interface ICommand
{
	/**
	 * Executes the command.
	 * @param event A {@link CommandEvent} representing from the invoked command and allowing to respond to the command
	 */
	void action(CommandEvent event);
	
	/**
	 * This method is executed on every button click where the button id equals the name of the command.
	 * @param event A {@link ButtonClickEvent} representing the clicked button.
	 */
	default void onButtonClick(ButtonClickEvent event) {
		event.deferEdit().queue();
	}

	/**
	 * Checks if a command should be executed.
	 * @param event A {@link CommandEvent} representing from the invoked command and allowing to respond to the command
	 * @return <code>true</code> if the command should be executed
	 */
	default boolean allowExecute(CommandEvent event)
	{
		return true;
	}

	/**
	 * Returns a help message of this command.
	 * @return a help message
	 */
	@Contract(pure = true)
	String help();
	
	/**
	 * gets a list of all parameters the command expects.
	 * The expected parameters should not change.
	 * The order of arguments is preserved.
	 * @return A {@link List} containing all argument the command expects
	 */
	@Contract(pure = true)
	List<ArgumentTemplate> getExpectedArguments();
}