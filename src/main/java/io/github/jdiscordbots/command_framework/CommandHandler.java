package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.command.slash.SlashCommandFrameworkEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

import java.util.function.Consumer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class CommandHandler
{
	private final Map<String, ICommand> commands = new ConcurrentHashMap<>();
	private static final Logger LOG=LoggerFactory.getLogger(CommandHandler.class);

	/**
	 * Get registered commands
	 *
	 * @return {@link java.util.Map Map}
	 */
	Map<String, ICommand> getCommands()
	{
		return Collections.unmodifiableMap(commands);
	}

	/**
	 * Add command to commands map
	 *
	 * @param name    invoke of command
	 * @param command {@link io.github.jdiscordbots.command_framework.command.ICommand Command}
	 */
	void addCommand(String name, ICommand command)
	{
		commands.put(name, command);
	}
	
	/**
	 * Add command from commands map
	 *
	 * @param name    invoke of command
	 */
	void removeCommand(String name)
	{
		commands.remove(name);
	}

	/**
	 * handle a command
	 *
	 * @param commandContainer {@link io.github.jdiscordbots.command_framework.CommandContainer CommandContainer}
	 */
	public void handle(final CommandContainer commandContainer)
	{
		CommandEvent event = commandContainer.event;
		String cmdIdentifier=commandContainer.invoke.toLowerCase();
		
		if (commands.containsKey(cmdIdentifier))
		{
			final ICommand command = commands.get(cmdIdentifier);

			boolean canExecute=true;
			
			if(event instanceof SlashCommandFrameworkEvent)
			{
				event=new SlashCommandFrameworkEvent(event.getFramework(),((SlashCommandFrameworkEvent) event).getEvent(),command.getExpectedArguments());
			}
			else
			{
				canExecute=hasExecutePrivileges(event.getMember(), command);
			}
			
			canExecute &= command.allowExecute(event);
			
			/* Check permission and allow all commands to Owners */
			if (canExecute || event.getFramework().getOwners().contains(event.getAuthor().getId()))
			{
				try
				{
					command.action(event);
				}
				catch (RuntimeException e)
				{
					LOG.error("The command {} was executed but an error occurred.", commandContainer.invoke, e);
					event.reply("Error:\n```" + e.getMessage() + "\n```");
				}
			}
			else
			{
				event.reply("You're not allowed to use this command!").queue();
			}
		}
		else if (event.getFramework().isUnknownCommand())
		{
			Consumer<CommandEvent> unknownCommandConsumer = event.getFramework().getUnknownCommandConsumer();
			if(unknownCommandConsumer == null)
			{
				final EmbedBuilder eb = new EmbedBuilder()
						.setColor(Color.red)
						.setTitle("Unknown command")
						.setDescription("See `" + event.getFramework().getPrefix() + "help` for more information!");
					
					event.reply(eb.build());
			}
			else
			{
				unknownCommandConsumer.accept(event);
			}
		}
	}

	/**
	 * checks if the user executing the command is permitted to do this
	 * @param member
	 * @param command
	 * @return
	 */
	private boolean hasExecutePrivileges(Member member,ICommand command)
	{
		Collection<CommandPrivilege> privileges = command.getPrivileges(member.getGuild());
		boolean allowed=command.isAvailableToEveryone();
		
		for (CommandPrivilege priv : privileges)
		{
			switch (priv.getType())
			{
			case ROLE:
				for (Role role : member.getRoles())
				{
					if(role.getId().equals(priv.getId()))
					{
						allowed=priv.isEnabled();
					}
				}
				break;
			case USER:
				if(member.getId().equals(priv.getId()))
				{
					return priv.isEnabled();
				}
				break;
			default:
				//ignore
				break;
			}
		}
		
		return allowed;
	}

	/**
	 * handle a button press
	 *
	 * @param framework {@link CommandFramework CommandFramework}
	 * @param event {@link ButtonInteractionEvent ButtonInteractionEvent}
	 */
	public void handleButtonClick(CommandFramework framework, ButtonInteractionEvent event)
	{
		String btnId=event.getButton().getId();
		if(btnId!=null)
		{
			String btnIdPrefix=CommandParser.SPACE_PATTERN.split(btnId)[0];
			ICommand cmd = commands.get(btnIdPrefix);
			if(cmd!=null)
			{
				cmd.onButtonClick(event);
			}
			else
			{
				Consumer<ButtonInteractionEvent> unknownButtonConsumer = framework.getUnknownButtonAction();
				if(unknownButtonConsumer==null)
				{
					event.deferEdit().queue();
				}
				else
				{
					unknownButtonConsumer.accept(event);
				}
			}
		}
	}
}
