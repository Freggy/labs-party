package de.bergwerklabs.party.server.listener

import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.api.party.packages.PartyDisbandPackage
import de.bergwerklabs.party.server.AtlantisPackageListener
import de.bergwerklabs.party.server.currentParties

/**
 * Created by Yannic Rieger on 21.09.2017.
 * <p>
 * @author Yannic Rieger
 */
class PartyDisbandListener : AtlantisPackageListener<PartyDisbandPackage>() {
    
    private val logger = AtlantisLogger.getLogger(PartyDisbandListener::class.java)
    
    override fun onResponse(pkg: PartyDisbandPackage) {
        logger.info("Disbanding party ${pkg.partyId}")
        currentParties.remove(pkg.partyId)
    }
}
