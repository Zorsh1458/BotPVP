package dev.zorsh

import dev.mryd.Main
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.components.CustomModelDataComponent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.StringUtil
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists


class ZorshizenCommands : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name.equals("zorshizen", ignoreCase = true)) {
                sender.sendMessage("§eExecuting ${args.getOrNull(0)} Zorshizen command...")
                when (args.getOrNull(0)) {
                    "spell" -> {
                        if (args.getOrNull(1) == null) {
                            sender.sendMessage("§cУкажите заклинание!")
                        } else if (sender.inventory.itemInMainHand.type != Material.STICK) {
                            sender.sendMessage("§cВозьмите в руки палочку!")
                        } else {
                            val spell = args.getOrNull(1)
                            val path = Path("plugins/Zorshizen2/books/${sender.name}/$spell.txt")
                            val item = sender.inventory.itemInMainHand
                            val meta = item.itemMeta
                            meta.displayName(Component.text("§dВолшебная палочка"))
                            if (path.exists()) {
                                meta.lore(mutableListOf(Component.text("§7Выбранно: §e$spell")))
                                meta.persistentDataContainer.set(MAGIC_STICK, PersistentDataType.STRING, "$spell")
                            } else {
                                meta.lore(mutableListOf(Component.text("§cЗаклинание не выбрано")))
                                meta.persistentDataContainer.set(MAGIC_STICK, PersistentDataType.STRING, "no_spell")
                            }
                            item.itemMeta = meta
                            sender.inventory.setItemInMainHand(item)
                        }
                    }
//                    "book" -> {
//                        if (args.getOrNull(1) == null) {
//                            sender.sendMessage("§cУкажите заклинание!")
//                        } else if (sender.inventory.itemInMainHand.type != Material.WRITABLE_BOOK) {
//                            sender.sendMessage("§cВозьмите в руки книгу с пером!")
//                        } else {
//                            val spell = args.getOrNull(1)
//                            val path = Path("plugins/Zorshizen2/books/${sender.name}/$spell.txt")
//                            val item = sender.inventory.itemInMainHand
//                            val meta = item.itemMeta
//                            if (path.exists()) {
//                                meta.lore(mutableListOf(Component.text("§7Выбранно: §e$spell")))
//                                meta.persistentDataContainer.set(MAGIC_STICK, PersistentDataType.STRING, "$spell")
//                            }
//                            item.itemMeta = meta
//                            sender.inventory.setItemInMainHand(item)
//                        }
//                    }
                }
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (args.isEmpty()) {
            return listOf("spell")
        }

        val availableSubCommands = listOf("spell")
        return StringUtil.copyPartialMatches(args[0], availableSubCommands, mutableListOf())
    }

}