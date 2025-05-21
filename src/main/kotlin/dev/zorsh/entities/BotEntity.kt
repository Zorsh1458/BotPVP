package dev.zorsh.entities

import dev.mryd.Main
import dev.zorsh.engine.ZVector
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class BotEntity(spawnLocation: Location, private val player: Player) {
    private val myEntity = spawnLocation.world.spawnEntity(spawnLocation, EntityType.HUSK)
    private var living = true
    private var strafeLR = 0.0
    private var strafeFB = 0.0
    private var yaw = 0.0
    private var pitch = 0.0
    private var jumping = false

    val location: Location
        get() = myEntity.location

    init {
        //myEntity.setNoPhysics(true)
        (myEntity as Mob).isAware = false
        object : BukkitRunnable() {
            override fun run() {
                if (!myEntity.isValid) {
                    living = false
                }
                if (!living) {
                    this.cancel()
                }
                //velocity = ZVector(0.0, velocity.y + gravity, 0.0) + ZVector(Vector(strafeLR, 0.0, strafeFB).rotateAroundY(yaw / 180.0 * -3.1415))
//                player.sendMessage("Bot Vel: ${velocity.y}")
                //val botFilter: Predicate<Entity> = Predicate<Entity> { ent -> ent != myEntity }
                //val groundData = location.world.rayTrace(location, Vector(0, -1,0), 0.05, FluidCollisionMode.NEVER, true, 0.4, botFilter)
                //if (isOnGround || (groundData != null && groundData.hitPosition.distance(location.toVector()) < 0.0499)) {
                //    velocity.y = max(velocity.y, 0.0001)
                //}
                //val hitData = location.world.rayTrace(location, velocity.vector(), velocity.length(), FluidCollisionMode.NEVER, true, 0.4, botFilter)
                //var hit = Location(location.world, location.x + velocity.x, location.y + velocity.y, location.z + velocity.z, yaw.toFloat(), pitch.toFloat())
                //if (hitData != null && hitData.hitPosition.distance(location.toVector()) < velocity.length() - 0.0001) {
                //    val pos = hitData.hitPosition
                //    hit = Location(location.world, pos.x, pos.y, pos.z, yaw.toFloat(), pitch.toFloat())
//                    player.sendMessage("HIT: ${hit.y - location.y} | ${pos.distance(location.toVector())} | ${velocity.length()}")
                //}
                val vel = Vector(strafeLR, myEntity.velocity.y, strafeFB)
                if (myEntity.isOnGround) {
                    vel.x = strafeLR * 5
                    vel.z = strafeFB * 5
                } else {
                    val localEntVel = Vector(0,0,0).copy(myEntity.velocity)
                    localEntVel.rotateAroundY(yaw / 180.0 * 3.1415)
                    vel.x = localEntVel.x * 0.99 + strafeLR * 0.5
                    vel.z = localEntVel.z * 0.99 + strafeFB * 0.5
                }
                vel.rotateAroundY(yaw / 180.0 * -3.1415)
                if (jumping) {
                    vel.y = 0.4
                }
                myEntity.velocity = vel
                myEntity.setRotation(yaw.toFloat(), pitch.toFloat())
                strafeFB = 0.0
                strafeLR = 0.0
                jumping = false
            }
        }.runTaskTimer(Main.instance, 1L, 1L)
    }

    fun setStrafeLR(value: Double) {
        strafeLR = value
    }

    fun setStrafeFB(value: Double) {
        strafeFB = value
    }

    fun jump() {
        if (myEntity.isOnGround) {
            jumping = true
        }
    }

    fun look(yaw: Double, pitch: Double) {
        this.yaw = yaw
        this.pitch = pitch
    }

    fun getEntity(): Entity {
        return myEntity
    }

    fun remove() {
        living = false
        myEntity.remove()
    }
}