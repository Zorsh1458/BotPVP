package dev.zorsh.commands

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
                sender
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (args.isEmpty()) {
            val path = Path("plugins/Zorshizen2/books/${sender.name}")
            if (path.listDirectoryEntries().isEmpty()) {
                return listOf("У вас нет заклинаний")
            }
            val res = mutableListOf<String>()
            path.listDirectoryEntries().forEach { e ->
                val after = e.toString().substringAfter("${sender.name}/")
                if (after.contains(".txt")) {
                    res.add(after.substringBefore(".txt"))
                }
            }
            return res.toList()
        }

        val path = Path("plugins/Zorshizen2/books/${sender.name}")
        if (path.listDirectoryEntries().isEmpty()) {
            return listOf("У вас нет заклинаний")
        }
        val res = mutableListOf<String>()
        path.listDirectoryEntries().forEach { e ->
            val after = e.toString().substringAfter("${sender.name}/")
            if (after.contains(".txt")) {
                res.add(after.substringBefore(".txt"))
            }
        }
//        return res.toList()
        return StringUtil.copyPartialMatches(args[0], res, mutableListOf())
    }

}