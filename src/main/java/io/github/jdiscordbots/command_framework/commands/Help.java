package io.github.jdiscordbots.command_framework.commands;

import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Help implements ICommand
{
	@Override
	public void action(CommandEvent event) {
		final Map<String, ICommand> commands = event.getFramework().getCommands();
		final List<String> commandNames = commands.keySet().stream().sorted().collect(Collectors.toList());

		final StringBuilder builder = new StringBuilder();

		for (String name : commandNames)
		{
			 builder.append('`').append(name).append('`').append(" - ").append('`').append(commands.get(name).help()).append('`').append("\n");
		}

		event.getChannel().sendMessage(new EmbedBuilder().setTitle("Help").setDescription(builder.toString().trim()).build()).queue();
	}

	@Override
	public String help() {
		return "View all registered commands.";
	}
}
