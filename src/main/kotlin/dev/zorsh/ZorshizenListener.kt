package dev.zorsh

import dev.mryd.Main
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.awt.print.Book
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class ZorshizenListener: Listener {
    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    fun onPlayerRightClicksBlock(event: PlayerInteractEvent) {
        val player = event.player
        if (event.hand == EquipmentSlot.HAND && player.inventory.itemInMainHand.type == Material.STICK) {
            if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
                val itemData = event.item?.itemMeta?.persistentDataContainer?.get(MAGIC_STICK, PersistentDataType.STRING) ?: return
                if (itemData == "no_spell") {
                    event.isCancelled = true
                    player.sendMessage("§cЗаклинание не выбрано!")
                } else {
                    event.isCancelled = true

                    if (!Main.mana.containsKey(player.name)) {
                        Main.mana[player.name] = maxMana
                    }
                    val spellText = File("plugins/Zorshizen2/books/${player.name}/$itemData.txt").readText()
                    val parser = ZorshizenParser(player)
                    GlobalScope.launch {
                        parser.parseSpell(spellText)
                    }
                }
            }
        } else if (event.hand == EquipmentSlot.HAND && player.inventory.itemInMainHand.type == Material.WRITABLE_BOOK) {
            if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
                val pages = (event.item?.itemMeta as BookMeta).pageCount
                if (pages > 0) {
                    val fPage = ((event.item?.itemMeta as BookMeta).page(1) as TextComponent).content()
                    if (fPage.startsWith("[ZORSHIZEN]") && fPage.contains("\n") && fPage.substringAfter("\n")
                            .startsWith("Name: ")
                    ) {
                        var spellName = fPage.substringAfter("\nName: ")
                        if (spellName.contains(" ")) {
                            spellName = spellName.substringBefore(" ")
                        }
                        if (spellName.contains("\n")) {
                            spellName = spellName.substringBefore("\n")
                        }
                        var text = ""
                        for (i in 1..pages) {
                            text += ((event.item?.itemMeta as BookMeta).page(i) as TextComponent).content()
                            text += "\n"
                        }
                        player.sendMessage("§a[Zorshizen] §7Редактирование: §f$spellName")
                        Path("plugins/Zorshizen2/books/${player.name}").createDirectories()
                        val file = File("plugins/Zorshizen2/books/${player.name}/$spellName.txt")
                        file.createNewFile()
                        file.writeText(text)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onPlayerEditBook(event: PlayerEditBookEvent) {
        val player = event.player
        val pages = event.newBookMeta.pageCount
        if (pages > 0) {
            val fPage = (event.newBookMeta.page(1) as TextComponent).content()
            if (fPage.startsWith("[ZORSHIZEN]") && fPage.contains("\n") && fPage.substringAfter("\n").startsWith("Name: ")) {
                var spellName = fPage.substringAfter("\nName: ")
                if (spellName.contains(" ")) {
                    spellName = spellName.substringBefore(" ")
                }
                if (spellName.contains("\n")) {
                    spellName = spellName.substringBefore("\n")
                }
                var text = ""
                for (i in 1..pages) {
                    text += (event.newBookMeta.page(i) as TextComponent).content()
                    text += "\n"
                }
                player.sendMessage("§a[Zorshizen] §7Заклинание обновлено: §f$spellName")
                object : BukkitRunnable() {
                    override fun run() {
                        val item = player.inventory.itemInMainHand
                        if (item.itemMeta is BookMeta && (item.itemMeta as BookMeta) == event.newBookMeta) {
                            val meta = item.itemMeta
                            meta.lore(listOf(Component.text("§7Заклинание: §b$spellName"), Component.text("§7Последнее изменение: §6${player.name}")))
                            item.setItemMeta(meta)
                            player.inventory.setItemInMainHand(item)
                        }
                    }
                }.runTaskLater(Main.instance, 1L)
                Path("plugins/Zorshizen2/books/${player.name}").createDirectories()
                val file = File("plugins/Zorshizen2/books/${player.name}/$spellName.txt")
                file.createNewFile()
                file.writeText(text)
            }
        }
    }
}