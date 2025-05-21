package dev.zorsh.commands

import dev.zorsh.engine.BotPVPManager
import dev.zorsh.engine.ZorshizenParser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries


class BotPVPCommands : CommandExecutor, TabCompleter {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name.equals("bot", ignoreCase = true)) {
                when (args.getOrNull(0)) {
                    "spawn" -> {
                        BotPVPManager.spawnPvpBot(sender)
                    }
                    "remove" -> {
                        BotPVPManager.removeBot(sender)
                    }
                    "execute" -> {
                        if (args.getOrNull(1) == null) {
                            sender.sendMessage("Укажите программу!")
                        } else {
                            val bot = BotPVPManager.getBot(sender)
                            if (bot != null) {
                                val file = File("plugins/BotPVP/books/${sender.name}/${args.getOrNull(1)}.txt")
                                if (file.exists()) {
                                    val spellText = file.readText()
                                    val parser = ZorshizenParser(sender, bot)
                                    GlobalScope.launch(Dispatchers.Default) {
                                        parser.parseSpell(spellText)
                                    }
                                    parser.updateActions()
                                } else {
                                    sender.sendMessage("У вас нет программы ${args.getOrNull(1)}")
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (args.isEmpty()) {
            return listOf()
        }

        if (args.size == 1) {
            return listOf("execute", "spawn", "remove", "select")
        }

        val path = Path("plugins/BotPVP/books/${sender.name}")
        if (path.listDirectoryEntries().isEmpty()) {
            return listOf("У вас нет программ")
        }
        val res = mutableListOf<String>()
        path.listDirectoryEntries().forEach { e ->
            val after = e.toString().substringAfter("${sender.name}/")
            if (after.contains(".txt")) {
                res.add(after.substringBefore(".txt"))
            }
        }
        return StringUtil.copyPartialMatches(args[0], res, mutableListOf())
    }
}