package dev.zorsh.entities

import dev.mryd.Main
import dev.zorsh.engine.ZVector
import dev.zorsh.engine.plus
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.scheduler.BukkitRunnable

class BotEntity(spawnLocation: Location) {
    private val myEntity = spawnLocation.world.spawnEntity(spawnLocation, EntityType.HUSK)
    private var living = true
    private val isOnGround: Boolean
        get() = myEntity.isOnGround
    private val gravity = -0.08
    private var velocity = ZVector(0.0, 0.0, 0.0)
    private var strafeLR = 0.0
    private var strafeFB = 0.0

    val location: Location
        get() = myEntity.location

    init {
        myEntity.setNoPhysics(true)
        object : BukkitRunnable() {
            override fun run() {
                if (!myEntity.isValid) {
                    living = false
                }
                if (!living) {
                    this.cancel()
                }
                velocity = ZVector(strafeLR, velocity.y + gravity, strafeFB)
                if (isOnGround) {
                    velocity.y = 0.0
                }
                val hitData = location.world.rayTrace(location, velocity.normalized().vector(), velocity.length(), FluidCollisionMode.NEVER, true, 0.4, null)
                var hit = location + velocity
                if (hitData != null) {
                    val pos = hitData.hitPosition
                    hit = Location(location.world, pos.x, pos.y, pos.z)
                }
                myEntity.teleport(hit)
            }
        }.runTaskTimer(Main.instance, 1L, 1L)
    }

    fun setStrafeLR(value: Double) {
        strafeLR = value
    }

    fun setStrafeFB(value: Double) {
        strafeFB = value
    }

    fun getEntity(): Entity {
        return myEntity
    }

    fun remove() {
        living = false
        myEntity.remove()
    }
}