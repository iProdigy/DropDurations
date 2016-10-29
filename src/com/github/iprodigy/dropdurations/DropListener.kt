package com.github.iprodigy.dropdurations

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent

class DropListener(val plugin: ClearPlugin) : Listener {

    @EventHandler
    fun onItemSpawn(event: ItemSpawnEvent) {
        plugin.watch(event.entity)
    }

}
