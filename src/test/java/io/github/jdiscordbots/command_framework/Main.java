package io.github.jdiscordbots.command_framework;

import io.github.jdiscordbots.command_framework.command.ICommand;
import io.github.jdiscordbots.command_framework.commands.ManualCommand;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Testing main class for {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework}
 */
public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 * Main method for running Java applications
	 *
	 * @param args JVM arguments
	 */
	public static void main(String[] args) {
		try (final Scanner sc = new Scanner(new File(".token"))) {
			if (sc.hasNextLine()) {
				final String token = sc.nextLine();
				final CommandFramework exclamation = new CommandFramework();
				final CommandFramework question = new CommandFramework().setPrefix("?");

				final Map<String, ICommand> commands = new HashMap<>();
				commands.put("manual", new ManualCommand());
				final CommandFramework manual = new CommandFramework(commands).setPrefix("manual ");

				JDABuilder.createDefault(token).addEventListeners(exclamation.build(), question.build(), manual.build()).build();
			} else
				LOG.error("The file .token is empty.");
		} catch (FileNotFoundException e) {
			LOG.error("The file .token does not exist or it is not accessible.", e);
		} catch (LoginException e) {
			LOG.error("The login process failed.", e);
		}

	}
}
