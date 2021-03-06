package de.bergwerklabs.party.client.bungee.command

import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.party.api.PartyApi
import de.bergwerklabs.party.client.bungee.partyBungeeClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.function.Consumer

/**
 * Created by Yannic Rieger on 30.09.2017.
 *
 * Command to disband a party.
 * Command usage: /party disband
 *
 * @author Yannic Rieger
 */
class PartyDisbandCommand : BungeeCommand {
    
    override fun getUsage() = "/party disband"
    
    override fun getName() = "disband"
    
    override fun getDescription() = "Löst die Party auf."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            PartyApi.getParty(sender.uniqueId, Consumer { optional ->
                val uuid = sender.uniqueId
                if (optional.isPresent) {
                    val party = optional.get()
                    if (party.isOwner(uuid)) {
                        party.disband()
                        partyBungeeClient!!.messenger.message("§cDu hast die Party aufgelöst.", sender)
                    }
                    else partyBungeeClient!!.messenger.message(
                        "§cUm eine Party aufzulösen, musst du Party-Leader sein.", sender
                    )
                }
                else partyBungeeClient!!.messenger.message("§cDu befindest dich zur Zeit in keiner Party.", sender)
            })
        }
    }
}