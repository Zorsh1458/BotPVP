package dev.zorsh.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText


class SpellinfoCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name.equals("spellinfo", ignoreCase = true)) {
                if (args.getOrNull(0) == null) {
                    sender.sendMessage("§cУкажите заклинание!")
                } else {
                    val spell = args.getOrNull(0)
                    val path = Path("plugins/Zorshizen2/books/${sender.name}/$spell.txt")
                    if (path.exists()) {
                        sender.sendMessage("§d[Zorshizen] §7Заклинание §e$spell§7:")
                        val spellText = path.readText()
                        sender.sendMessage(Component.text(spellText)
                            .hoverEvent(HoverEvent.showText(Component.text("§7Нажмите чтобы скопировать")))
                            .clickEvent(ClickEvent.copyToClipboard(spellText))
                        )
                    } else {
                        sender.sendMessage("§d[Zorshizen] §cУ вас нет заклинания $spell")
                    }
                }
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