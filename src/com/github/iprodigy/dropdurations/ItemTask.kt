package com.github.iprodigy.dropdurations

import org.bukkit.entity.Item
import java.util.HashMap

class ItemTask(val watched: HashMap<Item, Long>) : Runnable {

    override fun run() {
        if (watched.isEmpty())
            return

        for (i in (0..(watched.size - 1)).reversed()) {
            val time = watched.values.elementAt(i)

            if (System.currentTimeMillis() >= time) {
                val item = watched.keys.elementAt(i)

                if (item.isValid)
                    item.remove()

                watched.remove(item)
            }
        }
    }

}
