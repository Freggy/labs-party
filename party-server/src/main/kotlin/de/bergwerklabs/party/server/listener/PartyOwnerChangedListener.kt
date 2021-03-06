package de.bergwerklabs.party.server.listener

import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.api.party.packages.PartyChangeOwnerPacket
import de.bergwerklabs.party.server.AtlantisPackageListener
import de.bergwerklabs.party.server.currentParties

/**
 * Created by Yannic Rieger on 21.09.2017.
 *
 * @author Yannic Rieger
 */
class PartyOwnerChangedListener : AtlantisPackageListener<PartyChangeOwnerPacket>() {
    
    private val logger = AtlantisLogger.getLogger(this::class.java)
    
    override fun onResponse(pkg: PartyChangeOwnerPacket) {
        logger.info("Changed owner of party ${pkg.party.id} from ${pkg.oldOwner} to ${pkg.newOwner}")
        currentParties[pkg.party.id]?.owner = pkg.newOwner
        currentParties[pkg.party.id]?.members?.remove(pkg.newOwner)
        currentParties[pkg.party.id]?.members?.add(pkg.oldOwner)
    }
}