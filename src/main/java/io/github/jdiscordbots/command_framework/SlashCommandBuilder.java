package io.github.jdiscordbots.command_framework;

import java.util.HashSet;
import java.util.Set;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

final class SlashCommandBuilder
{
	private final SlashCommandData commandData;
	private SubcommandGroupData group;
	private SubcommandData subcommand;
	
	public SlashCommandBuilder(SlashCommandData commandData)
	{
		this.commandData = commandData;
	}

	public static CommandData buildSlashCommand(String name, ICommand cmd)
	{
		SlashCommandData commandData=Commands.slash(name, cmd.help());
		SlashCommandBuilder subCommandInfo=new SlashCommandBuilder(commandData);
		for (ArgumentTemplate arg : cmd.getExpectedArguments())
		{
			subCommandInfo.setupSlashArgument(arg);
		}
		Set<Permission> requiredPermissions = cmd.getRequiredPermissions();
		DefaultMemberPermissions privileges;
		if (requiredPermissions == null) {
			privileges = DefaultMemberPermissions.ENABLED;
		}else {
			requiredPermissions = new HashSet<>(requiredPermissions);
			requiredPermissions.add(Permission.ADMINISTRATOR);
			privileges = DefaultMemberPermissions.enabledFor(requiredPermissions);
		}
		commandData.setDefaultPermissions(privileges);
		return commandData;
	}
	
	private void setupSlashArgument(ArgumentTemplate arg)
	{
		switch(arg.getType())
		{
		case SUB_COMMAND:
			addSubCommand(arg);
			break;
		case SUB_COMMAND_GROUP:
			addSubCommandGroup(arg);
			break;
		default:
			addNormalArgument(arg);
		}
	}
	
	private void addSubCommand(ArgumentTemplate arg)
	{
		subcommand=new SubcommandData(arg.getName(), arg.getDescription());
		if(group==null)
		{
			commandData.addSubcommands(subcommand);
		}
		else
		{
			group.addSubcommands(subcommand);
		}
	}
	private void addSubCommandGroup(ArgumentTemplate arg)
	{
		group=new SubcommandGroupData(arg.getName(), arg.getDescription());
		commandData.addSubcommandGroups(group);
	}

	private void addNormalArgument(ArgumentTemplate arg)
	{
		OptionData option=new OptionData(arg.getType(), arg.getName(), arg.getDescription());
		option.setRequired(arg.isRequired());
		if(arg.hasChoices())
		{
			for (String choice : arg.getChoices())
			{
				if(arg.getType()==OptionType.INTEGER)
				{
					option.addChoice(choice, Integer.parseInt(choice));
				}
				else
				{
					option.addChoice(choice, choice);
				}
			}
		}
		if(subcommand==null)
		{
			commandData.addOptions(option);
		}
		else
		{
			subcommand.addOptions(option);
		}
	}
}
