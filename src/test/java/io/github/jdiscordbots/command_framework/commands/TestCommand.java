package io.github.jdiscordbots.command_framework.commands;

import java.util.Arrays;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Command.OptionType;

@Command("test")
public class TestCommand implements ICommand {
	@Override
	public void action(CommandEvent event) {
		event.reply(new EmbedBuilder()
				.setDescription(event.getArgs().get(2).getAsString())
				.addField("subcommand group",event.getArgs().get(0).getAsString(),false)
				.addField("subcommand name",event.getArgs().get(1).getAsString(),false)
				.build()).queue();
		
	}

	@Override
	public String help() {
		return "Testing purposes";
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Arrays.asList(
				new ArgumentTemplate(OptionType.SUB_COMMAND_GROUP, "entities", "get the ID of an entity", false),
				new ArgumentTemplate(OptionType.SUB_COMMAND, "channel", "get the ID of a channel", false),
				new ArgumentTemplate(OptionType.CHANNEL, "channel", "the channel to get the ID", true),
				new ArgumentTemplate(OptionType.SUB_COMMAND, "role", "get the ID of a role", false),
				new ArgumentTemplate(OptionType.ROLE, "role", "the role to get the ID", false),
				new ArgumentTemplate(OptionType.SUB_COMMAND_GROUP, "print", "print something", false),
				new ArgumentTemplate(OptionType.SUB_COMMAND, "number", "print a number", false),
				new ArgumentTemplate(OptionType.INTEGER, "num", "the number to print", true),
				new ArgumentTemplate(OptionType.SUB_COMMAND, "string", "print a string", false),
				new ArgumentTemplate(OptionType.STRING, "str", "the string to print", true)
				);
	}
}
