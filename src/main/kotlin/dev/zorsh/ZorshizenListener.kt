package dev.zorsh

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.w3c.dom.Text
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class ZorshizenListener: Listener {
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
                    //player.sendMessage("§fЗаклинание: §d$itemData")
                    val spellText = File("plugins/Zorshizen2/books/${player.name}/$itemData.txt").readText()
                    val parser = ZorshizenParser()
                    parser.parseSpell(spellText, player)
                    //player.sendMessage(spellText)
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
            if (fPage.startsWith("[SPELL]") && fPage.contains("\n") && fPage.substringAfter("\n").startsWith("Name: ")) {
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
                player.sendMessage("§eEdited: $spellName")
                player.sendMessage(text)
                Path("plugins/Zorshizen2/books/${player.name}").createDirectories()
                val file = File("plugins/Zorshizen2/books/${player.name}/$spellName.txt")
                file.createNewFile()
                file.writeText(text)
            }
        }
    }
}