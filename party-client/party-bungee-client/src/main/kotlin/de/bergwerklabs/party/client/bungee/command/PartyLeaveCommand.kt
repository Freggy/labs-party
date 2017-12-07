package de.bergwerklabs.party.client.bungee.command

import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.party.api.PartyApi
import de.bergwerklabs.party.api.wrapper.PartyUpdateAction
import de.bergwerklabs.party.client.bungee.partyBungeeClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 30.09.2017.
 *
 * A player executing this command will leave the party they're in.
 *
 * @author Yannic Rieger
 */
class PartyLeaveCommand : BungeeCommand {
    
    override fun getUsage() = "/party leave"
    
    override fun getName() = "leave"
    
    override fun getDescription() = "Verlässt die Party."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
        
            if (args != null && args.isEmpty()) {
                partyBungeeClient!!.messenger.message("§cDu musst mindestens einen Spieler angeben.", sender)
                return
            }
            
            partyBungeeClient!!.runAsync {
                val optional = PartyApi.getParty(sender.uniqueId)
    
                if (optional.isPresent) {
                    val party = optional.get()
        
                    // TODO: display message to all members.
                    // TODO: disban
        
                    party.removeMember(sender.uniqueId, PartyUpdateAction.PLAYER_LEAVE)
                }
                else partyBungeeClient!!.messenger.message("§cDu bist in keiner Party.", sender)
            }
        }
    }
}