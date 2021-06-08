package io.github.jdiscordbots.command_framework;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.jdiscordbots.command_framework.command.slash.SlashCommandFrameworkEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

final class CommandListener extends ListenerAdapter
{
	private final CommandFramework framework;
	private final CommandHandler handler;

	/**
	 * Construct a new CommandListener with the given CommandFramework
	 *
	 * @param framework {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework}
	 */
	public CommandListener(CommandFramework framework)
	{
		this.framework=framework;
		this.handler=framework.getCommandHandler();
	}

	@Override
	public void onReady(ReadyEvent event)
	{
		initializeSlashCommands(event.getJDA());
	}
	
	private void initializeSlashCommands(JDA jda)
	{
		Collection<CommandData> slashCommands = getSlashCommands();
		initializeSlashCommands(jda,slashCommands);
	}
	
	private void initializeSlashCommands(JDA jda, Collection<CommandData> slashCommands)
	{
		if (framework.isSlashCommandsPerGuild())
		{
			for (Guild guild : jda.getGuilds())
			{
				initializeSlashCommands(slashCommands, guild::updateCommands, guild::retrieveCommands)
						.queue(cmds -> setupSlashCommandPermissions(guild, cmds));
			}
		}
		else
		{
			initializeSlashCommands(slashCommands, jda::updateCommands, jda::retrieveCommands).queue(cmds ->
			{
				for (Guild guild : jda.getGuilds())
				{
					setupSlashCommandPermissions(guild, cmds);
				}
			});
		}
	}
	
	private void setupSlashCommandPermissions(Guild g,Map<String, String> commandIds)
	{
		Map<String, Collection<? extends CommandPrivilege>> privileges = framework.getCommands().entrySet().stream().map(
				cmd -> new AbstractMap.SimpleEntry<>(commandIds.get(cmd.getKey()), addOwnerPrivileges(cmd.getValue().getPrivileges(g))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		g.updateCommandPrivileges(privileges).queue();
	}
	
	private Collection<? extends CommandPrivilege> addOwnerPrivileges(Collection<? extends CommandPrivilege> privileges)
	{
		return Stream.concat(framework.getOwners().stream().map(CommandPrivilege::enableUser), privileges.stream()).collect(Collectors.toSet());
	}
	
	private RestAction<Map<String, String>> initializeSlashCommands(Collection<CommandData> slashCommands,Supplier<CommandListUpdateAction> commandUpdater,Supplier<RestAction<List<net.dv8tion.jda.api.interactions.commands.Command>>> commandRetriever)
	{
		CommandListUpdateAction commandsAction = commandUpdater.get().addCommands(slashCommands);
		if(framework.isRemoveUnknownSlashCommands())
		{
			commandRetriever.get()
					.queue(commands -> commands.stream().filter(
							cmd -> slashCommands.stream().noneMatch(sCmd -> sCmd.getName().equals(cmd.getName())))
							.forEach(cmd -> cmd.delete().queue()));
		}
		
		return commandsAction.map(commands->commands.stream().collect(Collectors.toMap(Command::getName,Command::getId)));
	}
	
	private Collection<CommandData> getSlashCommands()
	{
		Collection<CommandData> slashCommands = new ArrayList<>();
		framework.getCommands()
				.forEach((name, cmd) -> slashCommands.add(SlashCommandBuilder.buildSlashCommand(name, cmd)));
		return slashCommands;
	}
	
	
	/**
	 * Handle incomming messages
	 *
	 * @param event {@link net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
	 */
	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		final Message message = event.getMessage();
		final String contentRaw = message.getContentRaw().trim();
		final String selfUserId = message.getJDA().getSelfUser().getId();
		final boolean containsMention = contentRaw.startsWith("<!@" + selfUserId + "> ")
			|| contentRaw.startsWith("<@" + selfUserId + "> ");

		if (message.getAuthor().isBot())
			return;

		if (framework.isMentionPrefix() && containsMention)
		{
			handler.handle(CommandParser.parse(framework, event, CommandParser.SPACE_PATTERN.split(contentRaw)[0] + " "));
			return;
		}

		if (message.getContentDisplay().startsWith(framework.getPrefix()))
			handler.handle(CommandParser.parse(framework, event, framework.getPrefix()));
	}
	
	@Override
	public void onSlashCommand(SlashCommandEvent event)
	{
		event.deferReply().queue();
		SlashCommandFrameworkEvent frameworkEvent = new SlashCommandFrameworkEvent(framework,event);
		handler.handle(new CommandContainer(event.getName(), frameworkEvent));
	}
	
	@Override
	public void onButtonClick(ButtonClickEvent event)
	{
		handler.handleButtonClick(framework,event);
	}
}