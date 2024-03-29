package io.github.jdiscordbots.command_framework;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.jdiscordbots.command_framework.command.Command;
import io.github.jdiscordbots.command_framework.command.CommandEvent;
import io.github.jdiscordbots.command_framework.command.ICommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;

/**
 * Main class of the command framework.
 * 
 * See the README file of the project for details.
 * This class is thread safe.
 * 
 * @see CommandFramework#build()
 */
public class CommandFramework
{
	private static final Logger LOG=LoggerFactory.getLogger(CommandFramework.class);
	
	private final AtomicReference<Consumer<CommandEvent>> unknownCommandConsumer = new AtomicReference<>();
	private final AtomicReference<Consumer<ButtonInteractionEvent>> unknownButtonConsumer = new AtomicReference<>();
	private final AtomicReference<String> prefix = new AtomicReference<>("!");
	private final Set<String> owners = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private volatile boolean mentionPrefix = true;
	private volatile boolean unknownCommand = true;
	private volatile boolean slashCommandsPerGuild=false;
	private volatile boolean removeUnknownSlashCommands=true;
	
	private CommandHandler handler=new CommandHandler();
	
	/**
	 * Constructs a new CommandFramework instance with the caller-package
	 *
	 * Only classes in the same package (and subpackages) than the calling class will be scanned for commands.
	 * @implSpec This constructor should not be used by subclasses. Instead, subclasses should use {@code super(getCallerPackageName())}
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#CommandFramework(String)
	 */
	public CommandFramework()
	{
		this(getCallerPackageName());
		if(getClass()!=CommandFramework.class)
		{
			throw new IllegalStateException("This constructor may not be called by subclasses!");
		}
	}

	/**
	 * Gets the package name of the class of the calling method
	 * @return the package name of the class of the calling method
	 */
	protected static final String getCallerPackageName()
	{
		try
		{
			StackTraceElement trace = new Throwable().getStackTrace()[2];
			String clName = trace.getClassName();
			return Thread.currentThread().getContextClassLoader().loadClass(clName).getPackage().getName();
		}
		catch (ClassNotFoundException ignored)
		{
			throw new IllegalStateException("caller class not available");
		}
	}
	
	/**
	 * Creates an instance of the command framework.
	 * Only classes in the passed package (and subpackages) will be scanned for commands.
	 * @param commandsRootPackage the root package to scan
	 */
	public CommandFramework(String commandsRootPackage)
	{
		try (ScanResult scanResult = new ClassGraph().acceptPackages(commandsRootPackage).enableAnnotationInfo().scan())
		{
			addCommands(scanResult);
		}
	}

	private void addCommands(ScanResult scanResult)
	{
		addAction(scanResult, (cmdAsAnnotation, annotatedAsObject) ->
		{
			final Command cmdAsBotCommand = (Command) cmdAsAnnotation;
			final ICommand cmd = (ICommand) annotatedAsObject;
			
			for (String alias : cmdAsBotCommand.value())
			{
				handler.addCommand(alias.toLowerCase(), cmd);
			}
		});
	}

	/**
	 * Instantiate Command-classes
	 *
	 * @param scanResult {@link ScanResult ScanResult}
	 * @param function    {@link java.util.function.BiConsumer BiConsumer}
	 */
	private static void addAction(ScanResult scanResult, BiConsumer<Annotation, Object> function)
	{
		for (ClassInfo cInfo : scanResult.getClassesWithAnnotation(Command.class.getCanonicalName()))
		{
			try
			{
				final Class<?> cl=cInfo.loadClass();
				final Object annotatedAsObject = cl.getDeclaredConstructor().newInstance();
				final Annotation cmdAsAnnotation = cl.getAnnotation((Class<? extends Annotation>) Command.class);

				function.accept(cmdAsAnnotation, annotatedAsObject);
			}
			catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
			{
				if(LOG.isErrorEnabled())
					LOG.error("An exception occurred trying to create and register an instance of the class {}.", cInfo.getName(), e);
			}
		}
	}
	/**
	 * Sets the prefix for commands to listen on.
	 * 
	 * Only messages beginning with the given prefix are interpreted as commands.
	 * @param prefix the new prefix
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public final CommandFramework setPrefix(String prefix)
	{
		this.prefix.set(prefix);
		return this;
	}
	
	/**
	 * Gets the current prefix for commands to listen on.
	 * 
	 * Only messages beginning with the given prefix are interpreted as commands.
	 * @return prefix of current {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setPrefix(String)
	 */
	public final String getPrefix()
	{
		return prefix.get();
	}
	
	/**
	 * Sets the owners of the bot.
	 * 
	 * Owners can bypass permissions
	 * @param owners an array containing the IDs of all owners
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public final CommandFramework setOwners(String... owners)
	{
		setOwners(Arrays.asList(owners));
		return this;
	}
	
	/**
	 * Gets the owners of the bot.
	 * 
	 * Owners can bypass permissions
	 * @return a {@link Set} containing the IDs of all owners
	 */
	public final Set<String> getOwners()
	{
		return Collections.unmodifiableSet(owners);
	}
	
	/**
	 * Sets the owners of the bot.
	 * 
	 * Owners can bypass permissions.
	 * @param owners a {@link Collection} containing the IDs of all owners
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public final CommandFramework setOwners(Collection<String> owners)
	{
		synchronized (this.owners)
		{
			this.owners.clear();
			this.owners.addAll(owners);
		}
		return this;
	}

	/**
	 * Set whether the system should respond to mentions or not
	 *
	 * @param mentionPrefix <code>true</code> if a mention of the bot should be a valid prefix, else <code>false</code>
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#isMentionPrefix()
	 */
	public final CommandFramework setMentionPrefix(boolean mentionPrefix)
	{
		this.mentionPrefix=mentionPrefix;
		return this;
	}
	
	/**
	 * Checks whether messages starting with a mention are interpreted as commands or not.
	 *
	 * @return <code>true</code> if mention prefix is enabled, otherwise <code>false</code>
	 * @see io.github.jdiscordbots.command_framework.CommandFramework#setMentionPrefix(boolean)
	 */
	public final boolean isMentionPrefix()
	{
		return mentionPrefix;
	}

	/** 
	 * Sets whether an action should be taken if an unknown command is executed.
	 * @param unknownCommand <code>true</code> if an action should be taken if an unknown command is executed, else <code>false</code>
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see CommandFramework#isUnknownCommand()
	 * @see CommandFramework#setUnknownCommandAction(Consumer)
	 */
	public final CommandFramework setUnknownCommand(boolean unknownCommand)
	{
		this.unknownCommand=unknownCommand;
		return this;
	}
	
	protected Consumer<CommandEvent> getUnknownCommandConsumer()
	{
		return unknownCommandConsumer.get();
	}

	

	/**
	 * Sets the action that occurs when an unkwown command is entered.
	 * @param unknownCommandConsumer the action triggered on unknown commands
	 * @return updated {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} for chaining convenience
	 * @see CommandFramework#isUnknownCommand()
	 * @see CommandFramework#setUnknownCommand(boolean)
	 */
	public final CommandFramework setUnknownCommandAction(Consumer<CommandEvent> unknownCommandConsumer)
	{
		this.unknownCommandConsumer.set(unknownCommandConsumer);
		return this.setUnknownCommand(true);
	}

	/**
	 * Checks whether an action should be taken if an unknown command is executed.
	 * @return <code>true</code> if an action should be taken if an unknown command is executed, else <code>false</code>
	 */
	public final boolean isUnknownCommand()
	{
		return unknownCommand;
	}
	
	/**
	 * Sets whether slash commands should be enabled globally or on a per-guild basis.
	 * 
	 * <a href="https://discord.com/developers/docs/interactions/slash-commands#registering-a-command">Per-Guild commands are updated instantly while updating of global slash commands may take an hour to update.</a>
	 * @param slashCommandsPerGuild <code>true</code> if slash commands should be set up per-guild
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public final CommandFramework setSlashCommandsPerGuild(boolean slashCommandsPerGuild)
	{
		this.slashCommandsPerGuild = slashCommandsPerGuild;
		return this;
	}
	
	/**
	 * Checks whether slash commands should be enabled globally or on a per-guild basis.
	 * @return <code>true</code> if slash commands should be set up per-guild
	 * @see CommandFramework#setSlashCommandsPerGuild(boolean)
	 */
	public final boolean isSlashCommandsPerGuild()
	{
		return slashCommandsPerGuild;
	}
	
	/**
	 * Sets whether unknown slash commands should be removed on startup or not.
	 * @param removeUnknownSlashCommands <code>true</code> if unknown slash commands should be removed on startup, else <code>false</code>
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public final CommandFramework setRemoveUnknownSlashCommands(boolean removeUnknownSlashCommands)
	{
		this.removeUnknownSlashCommands = removeUnknownSlashCommands;
		return this;
	}
	
	/**
	 * Checks whether unknown slash commands should be removed on startup or not.
	 * @return <code>true</code> if unknown slash commands should be removed on startup, else <code>false</code>
	 */
	public final boolean isRemoveUnknownSlashCommands()
	{
		return removeUnknownSlashCommands;
	}
	
	/**
	 * Creates a listener for handling events related to the command framework.
	 * 
	 * The command framework is active only if the listener returned by this method is active.
	 * @return the listener required for the command framework
	 */
	public ListenerAdapter build()
	{
		if(LOG.isDebugEnabled())
			LOG.debug("Listening to following commands ({}):\n{}", handler.getCommands().size(), String.join(", ", handler.getCommands().keySet()));
		
		return new CommandListener(this);
	}
	
	protected Consumer<ButtonInteractionEvent> getUnknownButtonAction()
	{
		return unknownButtonConsumer.get();
	}
	
	/**
	 * Sets the action executed when an unknown button is pressed.
	 * @param unknownButtonConsumer the action triggered when an unknown button is pressed.
	 * @return the instance (<code>this</code>) of the {@link CommandFramework} that can be used for chaining.
	 */
	public final CommandFramework setUnknownButtonAction(Consumer<ButtonInteractionEvent> unknownButtonConsumer)
	{
		this.unknownButtonConsumer.set(unknownButtonConsumer);
		return this;
	}

	/**
	 * Get all registered commands of the {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework} instance
	 *
	 * @return all registered commands
	 * @see CommandFramework#addCommand(String, ICommand)
	 */
	public final Map<String, ICommand> getCommands()
	{
		return handler.getCommands();
	}
	
	/**
	 * Adds a new command.
	 * 
	 * The returned {@link Consumer} should be called for every {@link JDA} object the command should be used with.
	 * @param name The name of the command to create
	 * @param cmd  the command itself
	 * @return A {@link Consumer} that should be executed with any {@link JDA} object the command should be used with
	 */
	public final Consumer<JDA> addCommand(String name,ICommand cmd)
	{
		Objects.requireNonNull(name);
		Objects.requireNonNull(cmd);
		name=name.toLowerCase();
		handler.addCommand(name,cmd);
		CommandData cmdData = SlashCommandBuilder.buildSlashCommand(name, cmd);
		return jda->jda.upsertCommand(cmdData).queue();
	}

	/**
	 * Removes an existing command.
	 * 
	 * The returned {@link Consumer} should be called for every {@link JDA} object the command should be used with.
	 * @param name the name of the command to remove
	 * @return A {@link Consumer} that should be executed with any {@link JDA} object the command should be used with
	 */
	public final Consumer<JDA> removeCommand(String name)
	{
		Objects.requireNonNull(name);
		String actualName=name.toLowerCase();
		handler.removeCommand(actualName);
		return jda -> Stream.concat(
				Stream.of(jda.retrieveCommands()),
				jda.getGuilds().stream().map(Guild::retrieveCommands))
				.forEach(cmds -> removeSlashCommand(jda, actualName, cmds));
	}
	
	private void removeSlashCommand(JDA jda,String name,RestAction<List<net.dv8tion.jda.api.interactions.commands.Command>> commands)
	{
		commands.queue(cmds -> cmds.stream()
				.filter(cmd -> name.equals(cmd.getName()))
				.forEach(cmd -> jda.deleteCommandById(cmd.getId()).queue()));
	}
	
	CommandHandler getCommandHandler()
	{
		return handler;
	}
}
