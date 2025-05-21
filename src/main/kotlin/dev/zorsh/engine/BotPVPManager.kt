package dev.zorsh.engine

import dev.zorsh.entities.BotEntity
import org.bukkit.entity.Player

class BotPVPManager {
    companion object {
        private val botList = hashMapOf<Player, BotEntity>()

        @JvmStatic
        fun spawnPvpBot(player: Player) {
            val loc = player.location
            val bot = BotEntity(loc)
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