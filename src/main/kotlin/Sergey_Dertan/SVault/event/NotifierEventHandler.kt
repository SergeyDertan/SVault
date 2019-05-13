package Sergey_Dertan.SVault.event

import Sergey_Dertan.SVault.messenger.Messenger
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.server.DataPacketReceiveEvent
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket

class NotifierEventHandler(version: String, description: String) : Listener {

    private val message = Messenger.getMessage("update-available", arrayOf("@version", "@description"), arrayOf(version, description))

    @Suppress("UNUSED")
    @EventHandler
    fun dataPacketReceive(e: DataPacketReceiveEvent) {
        if (e.packet is SetLocalPlayerAsInitializedPacket && e.player.hasPermission("svault.admin")) {
            e.player.sendMessage(this.message)
        }
    }
}
