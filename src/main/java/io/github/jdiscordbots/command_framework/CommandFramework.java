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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandUpdateAction.OptionData;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandFramework
{
	private static final Logger LOG=LoggerFactory.getLogger(CommandFramework.class);
	
	private static CommandFramework instance;

	private Consumer<CommandEvent> unknownCommandConsumer = null;
	private String prefix = "!";
	private String[] owners = {};
	private boolean mentionPrefix = true;
	private boolean unknownCommand = true;
	private boolean slashCommandsPerGuild=false;
	private boolean acknowledgeSlashCommandsAutomatically=true;
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
	
	public void setAcknowledgeSlashCommandsAutomatically(boolean acknowledgeSlashCommandsAutomatically) {
		this.acknowledgeSlashCommandsAutomatically = acknowledgeSlashCommandsAutomatically;
	}
	
	public boolean isAcknowledgeSlashCommandsAutomatically() {
		return acknowledgeSlashCommandsAutomatically;
	}

	public ListenerAdapter build()
	{
		if(LOG.isDebugEnabled())
			LOG.debug("Listening to following commands ({}):\n{}", CommandHandler.getCommands().size(), String.join(", ", CommandHandler.getCommands().keySet()));
		
		return new CommandListener(this);
	}
	
	
	private void initializeSlashCommands(JDA jda) {
		initializeSlashCommands(jda,getSlashCommands());
	}
	
	private void initializeSlashCommands(JDA jda,Collection<CommandData> slashCommands) {
		if(slashCommandsPerGuild) {
			for (Guild guild : jda.getGuilds()) {
				initializeSlashCommands(slashCommands,guild::updateCommands,guild::retrieveCommands);
			}
		}else {
			initializeSlashCommands(slashCommands,jda::updateCommands,jda::retrieveCommands);
		}
	}
	
	private void initializeSlashCommands(Collection<CommandData> slashCommands,Supplier<CommandUpdateAction> commandUpdater,Supplier<RestAction<List<net.dv8tion.jda.api.entities.Command>>> commandRetriever) {
		if(removeUnknownSlashCommands) {
			commandRetriever.get().queue(commands->commands.stream().filter(cmd->slashCommands.stream().noneMatch(sCmd->sCmd.getName().equals(cmd.getName()))).forEach(cmd->cmd.delete().queue()));
		}
		commandUpdater.get().addCommands(slashCommands).queue();
	}
	
	private Collection<CommandData> getSlashCommands(){
		Collection<CommandData> slashCommands=new ArrayList<>();
		getCommands().forEach((name,cmd)->{
			CommandData cmdData=new CommandData(name, cmd.help());
			for (ArgumentTemplate arg : cmd.getExpectedArguments()) {
				cmdData.addOption(new OptionData(arg.getType(), arg.getName(), arg.getDescription()).setRequired(arg.isRequired()));
				//TODO support subcommands
			}
			slashCommands.add(cmdData);
		});
		return slashCommands;
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
			framework.initializeSlashCommands(event.getJDA());
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
			SlashCommandFrameworkEvent frameworkEvent = new SlashCommandFrameworkEvent(event);
			frameworkEvent.acknowledge();
			CommandHandler.handle(new CommandHandler.CommandContainer(event.getCommandPath(), frameworkEvent));
		}
	}
}
