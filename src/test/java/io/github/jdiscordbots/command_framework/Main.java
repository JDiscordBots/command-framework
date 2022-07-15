package io.github.jdiscordbots.command_framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jdiscordbots.command_framework.commands.ManualCommand;
import net.dv8tion.jda.api.JDABuilder;

/**
 * Testing main class for {@link io.github.jdiscordbots.command_framework.CommandFramework CommandFramework}
 */
public class Main {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	/**
	 * Main method for te
	 *
	 * @param args JVM arguments
	 */
	public static void main(String[] args){
		try (final Scanner sc = new Scanner(new File(".token")))
		{
			if (sc.hasNextLine()){
				String token = sc.nextLine();
				final CommandFramework framework = new CommandFramework()
					.setUnknownCommandAction(event -> event.reply("Custom unknown message reply"))
					.setMentionPrefix(true)
					.setPrefix("nd--")
					.setOwners(new String[] {"358291050957111296", "321227144791326730"})
					.setSlashCommandsPerGuild(true);
				framework.addCommand("manual", new ManualCommand());
				JDABuilder.createDefault(token).addEventListeners(framework.build()).build();
			} else
				LOG.error("The file .token is empty.");
		} catch (FileNotFoundException e) {
			LOG.error("The file .token does not exist or it is not accessible.", e);
		} catch (LoginException e) {
			LOG.error("The login process failed.", e);
		}

	}
}
