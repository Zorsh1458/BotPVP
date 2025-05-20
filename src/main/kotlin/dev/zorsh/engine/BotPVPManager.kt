package dev.zorsh.engine

import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class BotPVPManager {
    companion object {
        private val botList = hashMapOf<Player, Entity>()

        @JvmStatic
        fun spawnPvpBot(player: Player) {
            val loc = player.location
            val bot = loc.world.spawnEntity(loc, EntityType.HUSK)
            botList[player] = bot
        }

        @JvmStatic
        fun clearPvpBots() {
            botList.forEach { (_, ent) ->
                ent.remove()
            }
            botList.clear()
        }
    }
}