package de.bergwerklabs.party.server.listener

import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.api.party.AtlantisParty
import de.bergwerklabs.atlantis.api.party.packages.createparty.PartyCreateRequestPackage
import de.bergwerklabs.atlantis.api.party.packages.createparty.PartyCreateResponsePackage
import de.bergwerklabs.atlantis.api.party.packages.createparty.PartyCreateResponseType
import de.bergwerklabs.party.server.AtlantisPackageListener
import de.bergwerklabs.party.server.currentParties
import de.bergwerklabs.party.server.packageService
import de.bergwerklabs.party.server.pendingInvites

import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Yannic Rieger on 21.09.2017.
 *
 * @author Yannic Rieger
 */
class PartyCreateRequestListener : AtlantisPackageListener<PartyCreateRequestPackage>() {
    
    private val logger = AtlantisLogger.getLogger(this::class.java)
    
    override fun onResponse(pkg: PartyCreateRequestPackage) {
        if (currentParties.values.any { party -> party.members.contains(pkg.owner) || party.owner.equals(pkg.owner) }) {
            logger.info("Cannot create party because requesting player is already partied.")
            packageService.sendResponse(PartyCreateResponsePackage(pkg.partyId, PartyCreateResponseType.ALREADY_PARTIED), pkg)
            return
        }
        else if (pkg.members.size > 7) { // TODO: make configurable && maybe check if owner is premium
            logger.info("Too much party members for party ${pkg.partyId}. Party member count: ${pkg.members.size}")
            packageService.sendResponse(PartyCreateResponsePackage(pkg.partyId, PartyCreateResponseType.DENY_TOO_MANY_MEMBERS_DEFAULT), pkg)
            return
        }
        
        logger.info("Creating party ${pkg.partyId} with member count of ${pkg.members.size}")
        val partyId = this.determineId()
        
        currentParties.put(partyId, AtlantisParty(pkg.owner, pkg.members, partyId))
        pendingInvites[pkg.partyId] = CopyOnWriteArrayList()
        packageService.sendResponse(PartyCreateResponsePackage(partyId, PartyCreateResponseType.SUCCESS), pkg)
    }
    
    /**
     * Determines a [UUID] that isn't already taken.
     */
    private fun determineId(): UUID {
        var partyId: UUID
        do {
            partyId = UUID.randomUUID()
        } while (currentParties.containsKey(partyId))
        return partyId
    }
}
