package de.bergwerklabs.party.client.bungee.command

import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.party.api.PartyApi
import de.bergwerklabs.party.client.bungee.partyBungeeClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 17.10.2017.
 *
 * Promotes a party member to party owner.
 *
 * @author Yannic Rieger
 */
class PartyPromoteCommand : BungeeCommand {
    
    override fun getUsage() = "/party promote <owner>"
    
    override fun getName() = "promote"
    
    override fun getDescription() = "Befördert einen Spieler zum Party-Owner."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            partyBungeeClient!!.runAsync {
                val optional = PartyApi.getParty(sender.uniqueId)
                if (optional.isPresent) {
                    val party = optional.get()
                    if (party.isOwner(sender.uniqueId)) {
                        val name = args!![0]
                        PlayerResolver.resolveNameToUuid(name).ifPresent {
                            val color = partyBungeeClient!!.zBridge.getRankColor(it)
                            partyBungeeClient!!.messenger.message("§7Du hast $color$name §7zum Party-Owner promoted.", sender)
                            party.changeOwner(it)
                        }
                    }
                    else partyBungeeClient!!.messenger.message("§cDu bist nicht der Party-Owner", sender)
                }
                else partyBungeeClient!!.messenger.message("§cDu bist in keiner Party.", sender)
            }
        }
    }
}