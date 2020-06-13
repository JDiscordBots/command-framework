package io.github.jdiscordbots.command_framework;

import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main
{
	private static String token;

	static
	{
		try (final Scanner sc = new Scanner(new File(".token")))
		{
			if (sc.hasNextLine())
				token = sc.nextLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalStateException(".token is empty");
		}
	}

	public static void main(String[] args) throws LoginException {
		final JDABuilder builder = JDABuilder.createDefault(token);
		final CommandFramework framework = new CommandFramework()
			.setUnknownMessage(channel -> channel.sendMessage("Custom unknown message reply").queue())
			.setMentionPrefix(true)
			.setPrefix("nd--")
			.setOwners(new String[] {"358291050957111296", "321227144791326730"});

		builder.addEventListeners(framework.build()).build();
	}
}
