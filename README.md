# Litebridge

Litebridge is a lightweight minigame plugin for Minecraft 1.8, aimed at recreating the Hypixel original minigame, **"The Bridge"**.

If you don't know what The Bridge is, here's a youtube video of some gameplay from the youtuber Technoblade. [Bridge Video](https://www.youtube.com/watch?v=RNi0gLnfUC8)

## Official Downloads

 - [Most Recent Github Release](https://github.com/liamgiraldo/Litebridge/releases/tag/untagged-4d57b82669770067e664)](https://github.com/liamgiraldo/Litebridge/releases/tag/1.0.0)

## Maintainers

 - [liamgiraldo (litebow)](https://github.com/liamgiraldo) Main developer
 - [StephenMiner](https://github.com/StephenMiner) Litecoin addon developer
 - [eteryi](https://github.com/eteryi) Litecosmetics addon developer

## Features

 - Import your own bridge maps (or just make anything a bridge map!)
 - Basic scoreboard customization
 - Bridge game customizability
	 - Set how many goals a game requires to win
	 - Have as many players as you want in one game
 - Multiple games running at the same time
 - Minecraft 1.8 support
 - (relatively) easy to use


## Setup

Litebridge is world based. To create a new bridge game, you'll need to create a new world containing your bridge map. I recommend [Multiverse-Core](https://github.com/Multiverse/Multiverse-Core) for importing worlds.

Once you've imported, or built a bridge map on a new world, do **/bridgewand** and begin right clicking to go through the build process. Follow those instructions until you have completed the map creation process.

Once that is completed, I recommend reloading the plugin using **/reload**

After that, your bridge map should be ready to use!

**IMPORTANT**
If you are booting up your server for the first time, you **must type /reload **. This is because the litebridge plugin is loaded before the worlds are loaded in, and the plugin depends on the worlds being loaded in before the plugin is. Reloading this gives the plugin a change to grab the worlds. This will probably be fixed in later releases, but reloads usually fix most of the plugin's critical issues.

## Commands

 - /q - Queues the sender to a random bridge game regardless of gamemode or map.
 - /q random - Queues the sender to a random bridge game regardless of gamemode or map.
 - /q (int) - Queues the player for a random bridge game of a specific gamemode (ex. 1 would be solos 2 would be duos)
 - /q (int) (worldname) - Attempts to queue the player for a random bridge game of a specific gamemode and map.
 - /bridgewand - Provides the sender a bridge map creation tool. Right click to begin using it.

## Attribution / Credit

Credit should be given where credit is due. This is my first ever Minecraft plugin, so if you use it, credit is appreciated, however not required.

That being said, this plugin uses [XMaterial](https://www.spigotmc.org/threads/xseries-xmaterial-xparticle-xsound-xpotion-titles-actionbar-etc.378136/).

This plugin also uses SpiGUI for the main menu inventory GUI. [SpiGUI](https://github.com/SamJakob/SpiGUI)

I was planning on using [SternalBoard](https://www.spigotmc.org/resources/sternalboard-simple-animated-scoreboard.89245/), however I just used the default spigot scoreboard api. Still will give it a shoutout.

## Integration
If (for some reason) you want to use this plugin in your own projects, you can use Maven. I really can't think of a good reason for you to do this, but I'll provide the option. If you do this please do not expect any kind of developer support, the plugin was never built to be a tool.
### Maven

Using Jitpack

```
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
```
	<dependency>
	    <groupId>com.github.liamgiraldo</groupId>
	    <artifactId>Litebridge</artifactId>
	    <version>1.0.0</version>
	</dependency>
```

