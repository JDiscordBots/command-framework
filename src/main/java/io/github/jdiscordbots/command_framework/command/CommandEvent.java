package io.github.jdiscordbots.command_framework.command;

import io.github.jdiscordbots.command_framework.CommandFramework;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.List;

public interface CommandEvent {

	CommandFramework getFramework();

	List<Argument> getArgs();
	
	Guild getGuild();

	JDA getJDA();

	User getAuthor();

	Member getMember();

	MessageChannel getChannel();
	
	User getSelfUser();

	Member getSelfMember();
	
	String getId();
	
	PrivateChannel getPrivateChannel();
	
	Message getMessage();//slash commands: create new SystemMessage
	
	long getIdLong();

	RestAction<Message> reply(String message);

	RestAction<Message> reply(MessageEmbed message);
	
	RestAction<Message> reply(Message message);
	
	RestAction<Void> deleteOriginalMessage();
	
}
