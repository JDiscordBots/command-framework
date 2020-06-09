package io.github.jdiscordbots.command_framework.utils;

import io.github.jdiscordbots.command_framework.CommandFramework;
import io.github.jdiscordbots.command_framework.command.CommandEvent;

import java.util.Arrays;

public class PermissionUtils
{
	private PermissionUtils()
	{
		/* Prevent instantiation */
	}

	public static boolean checkOwner(CommandEvent event)
	{
		return Arrays.stream(CommandFramework.getInstance().getOwners()).anyMatch(id -> id.equals(event.getAuthor().getId()));
	}
}
