package de.bergwerklabs.party.server.listener

import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.api.party.AtlantisParty
import de.bergwerklabs.atlantis.api.party.packages.update.PartyUpdate
import de.bergwerklabs.atlantis.api.party.packages.update.PartyUpdatePacket
import de.bergwerklabs.party.server.AtlantisPackageListener
import de.bergwerklabs.party.server.currentParties
import java.util.*

/**
 * Created by Yannic Rieger on 21.09.2017.
 *
 * @author Yannic Rieger
 */
class PartyUpdatePackageListener : AtlantisPackageListener<PartyUpdatePacket>() {
    
    private val logger = AtlantisLogger.getLogger(this::class.java)
    
    override fun onResponse(pkg: PartyUpdatePacket) {
        when (pkg.update) {
            PartyUpdate.PLAYER_KICK  -> this.handlePlayerKick(pkg.player.uuid, pkg.party.id)
            PartyUpdate.PLAYER_LEAVE -> this.handlePlayerLeave(pkg.player.uuid, pkg.party.id)
        }
    }
    
    private fun handlePlayerKick(player: UUID, partyId: UUID) {
        logger.info("Player $player was kicked from party $partyId")
        this.removeFromParty(currentParties[partyId], player)
    }
    
    private fun handlePlayerLeave(player: UUID, partyId: UUID) {
        logger.info("Player $player left party $partyId")
        this.removeFromParty(currentParties[partyId], player)
    }
    
    private fun removeFromParty(party: AtlantisParty?, player: UUID) {
        if (party?.owner != player) {
            party?.members?.remove(player)
        }
    }
}