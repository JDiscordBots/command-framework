package io.github.jdiscordbots.command_framework.commands;

import java.util.Arrays;
import java.util.List;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.entities.Command.OptionType;

@Command({ "test" })
public class TestCommand implements ICommand {
	@Override
	public void action(CommandEvent event) {
//		event.reply(new EmbedBuilder()
//				.addField("Channel", event.getArgs().get(0).getAsChannel().getName(), false)
//				.addField("int", String.valueOf(event.getArgs().get(1).getAsInt()), false).build()).queue();
//		event.getChannel().sendMessage("Role: " + event.getArgs().get(2).getAsRole()).queue();
		event.reply(String.valueOf(event.getArgs().get(0).getAsRole())).queue();

	}

	@Override
	public String help() {
		return "Testing purposes";
	}

	@Override
	public List<ArgumentTemplate> getExpectedArguments() {
		return Arrays.asList(
//				new ArgumentTemplate(OptionType.CHANNEL, "channel", "the channel to get the ID", true),
//				new ArgumentTemplate(OptionType.INTEGER, "num", "some number", true),
//				new ArgumentTemplate(OptionType.ROLE, "role", "role description", false),
				new ArgumentTemplate(OptionType.STRING, "test", "test description", true)
				);
	}
}
