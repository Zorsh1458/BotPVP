package dev.zorsh.engine

import dev.zorsh.entities.BotEntity
import org.bukkit.entity.Player

class BotPVPManager {
    companion object {
        private val botList = hashMapOf<Player, BotEntity>()

        @JvmStatic
        fun spawnPvpBot(player: Player) {
            val loc = player.location
            val bot = BotEntity(loc, player)
            if (botList.containsKey(player)) {
                botList[player]?.remove()
            }
            botList[player] = bot
        }

        @JvmStatic
        fun removeBot(player: Player) {
            if (botList.containsKey(player)) {
                botList[player]?.remove()
                botList.remove(player)
            }
        }

        @JvmStatic
        fun getBot(player: Player): BotEntity? {
            if (botList.containsKey(player)) {
                return botList[player]
            }
            return null
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