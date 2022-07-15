package io.github.jdiscordbots.command_framework.command;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Contract;

import io.github.jdiscordbots.command_framework.CommandFramework;
import io.github.jdiscordbots.command_framework.command.slash.SlashCommandFrameworkEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

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
	 * @param event A {@link ButtonInteractionEvent} representing the clicked button.
	 */
	default void onButtonClick(ButtonInteractionEvent event)
	{
		event.deferEdit().queue();
	}

	/**
	 * Return whether command can be executed or not
	 *
	 * @param event A {@link CommandEvent} representing the invoked command and allowing to respond to the command
	 * @return <code>true</code> (default) if command can be executed, otherwise <code>false</code>
	 */
	@Contract(pure = true)
	default boolean allowExecute(CommandEvent event)
	{
		if(isAvailableToEveryone()) {
			return true;
		}
		if(event instanceof SlashCommandFrameworkEvent) {
			return true;
		}
		for(Permission perm : getRequiredPermissions()){
			if(event.getMember().hasPermission(perm)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the permissions required for executing a command.
	 * Everyone with at least one of those permissons can use the command.
	 * Server administrators can overwrite this.
	 * This overwrites {@link ICommand#isAvailableToEveryone()}
	 * 
	 * @return a {@link Set} containing the required permissions or <code>null</code> if everyone should be able to use the command
	 */
	@Contract(pure = true)
	default Set<Permission> getRequiredPermissions()
	{
		return isAvailableToEveryone()?null:Collections.emptySet();
	}
	
	/**
	 * checks weather this command can be used by anyone without special permissions.
	 * @return <code>true</code> if anyone can use the command by default
	 * @see ICommand#getPrivileges(Guild)
	 */
	@Contract(pure = true)
	default boolean isAvailableToEveryone()
	{
		return true;
	}

	/**
	 * Returns a help message of this command.
	 *
	 * @return usage/help of command
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
