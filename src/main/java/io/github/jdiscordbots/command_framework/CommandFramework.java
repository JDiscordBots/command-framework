package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.command.slash.SlashCommandFrameworkEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import java.awt.Event;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommandFramework
{
	private static final Logger LOG=LoggerFactory.getLogger(CommandFramework.class);
	
	private static CommandFramework instance;

	private Consumer<CommandEvent> unknownCommandConsumer = null;
	private Consumer<ButtonClickEvent> unknownButtonConsumer = null;
	private String prefix = "!";
	private String[] owners = {};
	private boolean mentionPrefix = true;
	private boolean unknownCommand = true;
	private boolean slashCommandsPerGuild=false;
	private boolean removeUnknownSlashCommands=true;
	
	public CommandFramework()
	{
		this(getCallerPackageName());
	}

	private static String getCallerPackageName()
	{
		try
		{
			StackTraceElement trace = new Throwable().getStackTrace()[2];
			String clName = trace.getClassName();
			return Thread.currentThread().getContextClassLoader().loadClass(clName).getPackage().getName();
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalStateException("caller class not available");
		}
	}
	
	public CommandFramework(String commandsPackagePath)
	{
		instance = this;

		final Reflections reflections = new Reflections(commandsPackagePath);

		addCommands(reflections);
	}

	private static void addCommands(Reflections reflections)
	{
		addAction(reflections, (cmdAsAnnotation, annotatedAsObject) ->
		{
			final Command cmdAsBotCommand = (Command) cmdAsAnnotation;
			final ICommand cmd = (ICommand) annotatedAsObject;
			
			for (String alias : cmdAsBotCommand.value())
				CommandHandler.addCommand(alias.toLowerCase(), cmd);
		});
	}
	private static void addAction(Reflections reflections, BiConsumer<Annotation, Object> function)
	{
		for (Class<?> cl : reflections.getTypesAnnotatedWith(Command.class,true))
		{
			try
			{
				final Object annotatedAsObject = cl.getDeclaredConstructor().newInstance();
				final Annotation cmdAsAnnotation = cl.getAnnotation((Class<? extends Annotation>) Command.class);

				function.accept(cmdAsAnnotation, annotatedAsObject);
			}
			catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				if(LOG.isErrorEnabled())
					LOG.error("An exception occurred trying to create and register an instance of the class {}.", cl.getCanonicalName(), e);
			}
		}
	}

	public CommandFramework setPrefix(String prefix)
	{
		this.prefix = prefix;
		return this;
	}

	public CommandFramework setOwners(String owner, String... owners)
	{

		final List<String> ownersList = new ArrayList<>(Arrays.asList(owners));

		ownersList.add(owner);

		this.owners = ownersList.toArray(new String[0]);
		return this;
	}

	public CommandFramework setMentionPrefix(boolean mentionPrefix) {
		this.mentionPrefix = mentionPrefix;
		return this;
	}

	public CommandFramework setUnknownCommand(boolean unknownCommand) {
		this.unknownCommand = unknownCommand;
		return this;
	}

	public boolean isUnknownCommand() {
		return unknownCommand;
	}
	
	public CommandFramework setSlashCommandsPerGuild(boolean slashCommandsPerGuild) {
		this.slashCommandsPerGuild = slashCommandsPerGuild;
		return this;
	}
	
	public boolean isSlashCommandsPerGuild() {
		return slashCommandsPerGuild;
	}
	
	public void setRemoveUnknownSlashCommands(boolean removeUnknownSlashCommands) {
		this.removeUnknownSlashCommands = removeUnknownSlashCommands;
	}
	
	public boolean isRemoveUnknownSlashCommands() {
		return removeUnknownSlashCommands;
	}
	
	public ListenerAdapter build()
	{
		if(LOG.isDebugEnabled())
			LOG.debug("Listening to following commands ({}):\n{}", CommandHandler.getCommands().size(), String.join(", ", CommandHandler.getCommands().keySet()));
		
		return new CommandListener(this);
	}
	
	
	public static CommandFramework getInstance() {
		return instance;
	}

	public String getPrefix() {
		return prefix;
	}

	public String[] getOwners() {
		return owners;
	}

	Consumer<CommandEvent> getUnknownCommandConsumer() {
		return unknownCommandConsumer;
	}

	public CommandFramework setUnknownMessage(Consumer<CommandEvent> unknownCommandConsumer) {
		this.unknownCommandConsumer = unknownCommandConsumer;
		return this.setUnknownCommand(true);
	}
	
	Consumer<ButtonClickEvent> getUnknownButtonConsumer() {
		return unknownButtonConsumer;
	}
	
	public CommandFramework setUnknownButton(Consumer<ButtonClickEvent> unknownButtonConsumer) {
		this.unknownButtonConsumer=unknownButtonConsumer;
		return this;
	}

	public boolean isMentionPrefix() {
		return mentionPrefix;
	}

	public CommandFramework setOwners(String[] owners)
	{
		return this.setOwners(owners[0], Arrays.copyOfRange(owners, 1, owners.length));
	}

	public Map<String, ICommand> getCommands()
	{
		return CommandHandler.getCommands();
	}

	private static final class CommandListener extends ListenerAdapter
	{
		private final CommandFramework framework;

		public CommandListener(CommandFramework framework)
		{
			this.framework=framework;
		}

		@Override
		public void onReady(ReadyEvent event) {
			initializeSlashCommands(event.getJDA());
		}
		
		private void initializeSlashCommands(JDA jda) {
			Collection<CommandData> slashCommands = getSlashCommands();
			initializeSlashCommands(jda,slashCommands);
		}
		
		private void initializeSlashCommands(JDA jda, Collection<CommandData> slashCommands) {
			if (framework.isSlashCommandsPerGuild()) {
				for (Guild guild : jda.getGuilds()) {
					initializeSlashCommands(slashCommands, guild::updateCommands, guild::retrieveCommands)
							.queue(cmds -> setupSlashCommandPermissions(guild, cmds));
				}
			} else {
				initializeSlashCommands(slashCommands, jda::updateCommands, jda::retrieveCommands).queue(cmds -> {
					for (Guild guild : jda.getGuilds()) {
						setupSlashCommandPermissions(guild, cmds);
					}
				});
			}
		}
		
		private void setupSlashCommandPermissions(Guild g,Map<String, String> commandIds) {
			Map<String, Collection<? extends CommandPrivilege>> privileges = framework.getCommands().entrySet().stream().map(
					cmd -> new AbstractMap.SimpleEntry<>(commandIds.get(cmd.getKey()), cmd.getValue().getPrivileges(g)))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			g.updateCommandPrivileges(privileges).queue();
		}
		
		@CheckReturnValue
		private RestAction<Map<String, String>> initializeSlashCommands(Collection<CommandData> slashCommands,Supplier<CommandUpdateAction> commandUpdater,Supplier<RestAction<List<net.dv8tion.jda.api.interactions.commands.Command>>> commandRetriever) {
			CommandUpdateAction commandsAction = commandUpdater.get().addCommands(slashCommands);
			if(framework.isRemoveUnknownSlashCommands()) {
				commandRetriever.get().queue(commands->commands.stream().filter(cmd->slashCommands.stream().noneMatch(sCmd->sCmd.getName().equals(cmd.getName()))).forEach(cmd->{
					cmd.delete().queue();
				}));
			}
			
			return commandsAction.map(commands->commands.stream().collect(Collectors.toMap(cmd->cmd.getName(),cmd->cmd.getId())));
		}
		
		private Collection<CommandData> getSlashCommands(){
			Collection<CommandData> slashCommands=new ArrayList<>();
			framework.getCommands().forEach((name,cmd)->{
				
				CommandData commandData=new CommandData(name, cmd.help());
				SubcommandGroupData groupData=null;
				SubcommandData subCommandData=null;
				for (ArgumentTemplate arg : cmd.getExpectedArguments()) {
					
					switch(arg.getType()) {
					case SUB_COMMAND:
						subCommandData=new SubcommandData(arg.getName(), arg.getDescription());
						if(groupData==null) {
							commandData.addSubcommands(subCommandData);
						}else {
							groupData.addSubcommand(subCommandData);
						}
						break;
					case SUB_COMMAND_GROUP:
						groupData=new SubcommandGroupData(arg.getName(), arg.getDescription());
						commandData.addSubcommandGroups(groupData);
						break;
					default:
						OptionData option=new OptionData(arg.getType(), arg.getName(), arg.getDescription());
						option.setRequired(arg.isRequired());
						if(arg.hasChoices()) {
							for (String choice : arg.getChoices()) {
								if(arg.getType()==OptionType.INTEGER) {
									option.addChoice(choice, Integer.parseInt(choice));
								}else {
									option.addChoice(choice, choice);
								}
							}
						}
						if(subCommandData==null) {
							commandData.addOptions(option);
						}else {
							subCommandData.addOption(option);
						}
					
					}
				}
				commandData.setDefaultEnabled(cmd.isAvailableToEveryone());
				slashCommands.add(commandData);
			});
			return slashCommands;
		}
		

		@Override
		public void onMessageReceived(@Nonnull MessageReceivedEvent event)
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
				CommandHandler.handle(CommandHandler.CommandParser.parse(event, CommandHandler.CommandParser.SPACE_PATTERN.split(contentRaw)[0] + " "));
				return;
			}

			if (message.getContentDisplay().startsWith(framework.getPrefix()))
				CommandHandler.handle(CommandHandler.CommandParser.parse(event, framework.getPrefix()));
		}
		
		@Override
		public void onSlashCommand(SlashCommandEvent event) {
			event.deferReply().queue();
			SlashCommandFrameworkEvent frameworkEvent = new SlashCommandFrameworkEvent(event);
			CommandHandler.handle(new CommandHandler.CommandContainer(event.getName(), frameworkEvent));
		}
		
		@Override
		public void onButtonClick(ButtonClickEvent event) {
			CommandHandler.handleButtonClick(event);
		}
	}
}
