package dev.zorsh.commands

import dev.zorsh.engine.BotPVPManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries


class BotPVPCommands : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name.equals("bot", ignoreCase = true)) {
                BotPVPManager.spawnPvpBot(sender)
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (args.isEmpty()) {
            return listOf()
        }

        val res = listOf<String>()
        return StringUtil.copyPartialMatches(args[0], res, mutableListOf())
    }
}