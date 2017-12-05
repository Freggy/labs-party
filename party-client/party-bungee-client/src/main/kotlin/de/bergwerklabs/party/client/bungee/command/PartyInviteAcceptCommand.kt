package de.bergwerklabs.party.client.bungee.command

import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.party.api.PartyApi
import de.bergwerklabs.party.api.wrapper.PartyInviteStatus
import de.bergwerklabs.party.client.bungee.partyBungeeClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
/**
 * Created by Yannic Rieger on 30.09.2017.
 *
 * Accepts a party invitation.
 *
 * @author Yannic Rieger
 */
class PartyInviteAcceptCommand : BungeeCommand {
    
    override fun getUsage() = "/party accept"
    
    override fun getName() = "accept"
    
    override fun getDescription() = "Akzeptiert eine Party-Einladung."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            PartyApi.respondToInvite(PartyInviteStatus.ACCEPTED, sender.uniqueId, partyBungeeClient!!.invitedFor[sender.uniqueId]!!)
        }
    }
}