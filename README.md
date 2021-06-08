# Command Framework

## Usage

1. Add Command-Framework to the dependencies section of your `pom.xml` (replace VERSION with [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.jdiscordbots/command-framework/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.jdiscordbots/command-framework)):
```xml
<dependencies>
	<dependency>
		<groupId>io.github.jdiscordbots</groupId>
		<artifactId>command-framework</artifactId>
		<version>VERSION</version>
		<scope>compile</scope>
	</dependency>
</dependencies>
```

2. Create an instance of `CommandFramework`
3. Set prefix, owners etc.
4. Build the command listener using `CommandFramework#build()` and add it to your JDA(Builder) instance as an event listener
5. Create command classes which are annotated by `@Command("commandname")` and implement the `ICommand` interface
6. See how it works perfectly

### Working example:

Main:
```java
public class Main {
    public static void main(String[] args) throws LoginException {
    		final JDABuilder builder = JDABuilder.createDefault("your token");
    		final CommandFramework framework = new CommandFramework() // Step 1
                    /* Step 2 */
    				.setMentionPrefix(true) // Allow mention prefix, Default: true
    				.setPrefix("prefix") // Default: !
    				.setOwners("owner1", "optional owner 2", "...", "optional owner n"); // Set owners for permissions system, Default: {}
    
    		builder.addEventListeners(framework.build()).build(); // Step 3
    	}
}
```

Example command:
```java
import io.github.jdiscordbots.command_framework.command.*;

@Command({"example", "examplealias"}) // Step 4
public class Example implements ICommand {
    @Override
    public void action(CommandEvent event) {
        event.getChannel().sendMessage("Example command reply").queue();
    }

    @Override
    public String help() {
        return "Example command";
    }
}
```

Example permission restricted command:
```java

import io.github.jdiscordbots.command_framework.command.*;

import net.dv8tion.jda.api.Permission;

@Command({"permissionexample", "permissionexamplealias"}) // Step 4
public class PermissionExample implements ICommand {
    @Override
    public void action(CommandEvent event) {
        event.getChannel().sendMessage("Permission example command reply").queue();
    }
    
    @Override
    public boolean allowExecute(CommandEvent event) {
        return event.getMember().hasPermission(Permission.MANAGE_PERMISSIONS); // Allow use of command only to members with manage permissions permission
    }

    @Override
    public String help() {
        return "Permission example command";
    }
}
```