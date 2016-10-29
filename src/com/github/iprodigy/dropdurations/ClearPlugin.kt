package com.github.iprodigy.dropdurations

import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.craftbukkit.libs.com.google.gson.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.Duration
import java.util.HashMap

class ClearPlugin() : JavaPlugin() {
    internal val configFile = File(dataFolder, "config.json")
    internal val delays = HashMap<Material, Duration>()
    internal val watched = HashMap<Item, Long>()
    internal var waitTicks = 20L
    internal var defaultWait = Duration.ofMinutes(1)

    override fun onEnable() {
        readConfig()
        server.pluginManager.registerEvents(DropListener(this), this)
        server.scheduler.scheduleSyncRepeatingTask(this, ItemTask(watched), 0L, waitTicks)
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }

    fun watch(item: Item) {
        val mat = item.itemStack.type
        val wait = if (delays.containsKey(mat)) delays[mat] ?: defaultWait else defaultWait
        watched.put(item, System.currentTimeMillis() + wait.toMillis())
    }

    fun readConfig() {
        try {
            fun buildConfig(): JsonObject {
                val configObj = JsonObject()
                configObj.addProperty("wait_ticks", waitTicks)
                configObj.addProperty("default_wait", defaultWait.toMillis())

                val delaysArray = JsonArray()

                val example1 = JsonObject()
                example1.addProperty("material", "glass_bottle")
                example1.addProperty("delay", 15000)
                delaysArray.add(example1)

                val example2 = JsonObject()
                val mats = JsonArray()
                arrayOf(
                        "bow", "diamond_sword",
                        "diamond_helmet", "diamond_chestplate",
                        "diamond_leggings", "diamond_boots"
                ).forEach { item -> mats.add(JsonPrimitive(item)) }
                example2.add("materials", mats)
                example2.addProperty("delay", 300000)
                delaysArray.add(example2)

                configObj.add("delays", delaysArray)

                GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .toJson(configObj, FileWriter(configFile))

                return configObj
            }

            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                configFile.createNewFile()
                buildConfig()
            }

            val json = JsonParser().parse(FileReader(configFile))
            val config = if (json.isJsonObject) json.asJsonObject else buildConfig()

            waitTicks = Math.max(config.get("wait_ticks")?.asLong ?: waitTicks, 1)
            defaultWait = Duration.ofMillis(Math.max(config.get("default_wait")?.asLong ?: defaultWait.toMillis(), 1))

            fun writeDelay(matElement: JsonElement, delay: Long) {
                val mat = Material.matchMaterial(matElement.asString ?: return) ?: return
                delays.put(mat, Duration.ofMillis(delay))
            }

            config.get("delays")?.asJsonArray?.forEach { watch ->
                run {
                    val watchObj = watch.asJsonObject ?: return@forEach
                    val delay = watchObj.get("delay")?.asLong ?: return@forEach

                    watchObj.get("materials")?.asJsonArray?.forEach { mat -> writeDelay(mat, delay) }
                    writeDelay(watchObj.get("material") ?: return@forEach, delay)
                }
            }
        } catch (ex: Exception) {
            logger.severe(ex.message)
        }
    }

}
