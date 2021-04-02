package io.github.jdiscordbots.command_framework;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main
{
	private static final Logger LOG=LoggerFactory.getLogger(Main.class);
	public static void main(String[] args){
		try (final Scanner sc = new Scanner(new File(".token")))
		{
			if (sc.hasNextLine()){
				String token = sc.nextLine();
				final JDABuilder builder = JDABuilder.createDefault(token);
				final CommandFramework framework = new CommandFramework()
					.setUnknownMessage(event -> event.reply("Custom unknown message reply"))
					.setMentionPrefix(true)
					.setPrefix("nd--")
					.setOwners(new String[] {"358291050957111296", "321227144791326730"})
					.setSlashCommandsPerGuild(false);

				builder.addEventListeners(framework.build()).build();
			}else
				LOG.error("The file .token is empty.");
		} catch (FileNotFoundException e) {
			LOG.error("The file .token does not exist or it is not accessible.", e);
		} catch(LoginException e) {
			LOG.error("The login process failed.", e);
		}
		
	}
}
