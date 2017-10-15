package de.bergwerklabs.party.server.listener

import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.api.party.packages.invite.*
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.party.server.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Yannic Rieger on 04.10.2017.
 *
 * Listens for the [PartyClientInviteRequestPackage]
 *
 * @author Yannic Rieger
 */
class PartyInviteRequestListener : AtlantisPackageListener<PartyClientInviteRequestPackage>() {
    
    private val logger = AtlantisLogger.getLogger(this::class.java)
    
    override fun onResponse(pkg: PartyClientInviteRequestPackage) {
        val party = currentParties[pkg.partyId]
        
        logger.info("Received invite request from ${pkg.sender} for ${pkg.invitedPlayer} to party ${pkg.partyId}")
        
        if (party != null) {
            if (party.members.size >= 7) {
                logger.info("Party is already full, sending error message back.")
                packageService.sendResponse(PartyServerInviteResponsePackage(pkg.partyId, null, pkg.sender, InviteStatus.PARTY_FULL), pkg)
                return
            }
        }
        else {
            logger.info("Party does not exist anymore, sending error message back.")
            packageService.sendResponse(PartyServerInviteResponsePackage(pkg.partyId, null, pkg.sender, InviteStatus.PARTY_NOT_PRESENT), pkg)
            return
        }
    
        logger.info("Invite is now pending, after 30 seconds it will be removed.")
        pendingInvites.computeIfAbsent(pkg.partyId, { uuid -> CopyOnWriteArrayList() })
        pendingInvites[pkg.partyId]!!.add(InviteInfo(pkg.invitedPlayer, System.currentTimeMillis()))
        
        packageService.sendPackage(PartyServerInviteRequestPackage(party.id, pkg.invitedPlayer, pkg.sender, null), PartyClientInviteResponsePackage::class.java, AtlantisPackageService.Callback { response ->
            val responseParty = pendingInvites[pkg.partyId]
            val clientResponse = response as PartyClientInviteResponsePackage
            
            if (responseParty != null) { // check if party is present
                if (responseParty.any { inviteInfo -> inviteInfo.player == clientResponse.responder }) { // check if invite is not expired.
                    
                    if (party.members.size >= 7) {
                        logger.info("Party is already full, sending error message back...")
                        packageService.sendResponse(PartyServerInviteResponsePackage(clientResponse.partyId, clientResponse.responder, clientResponse.initalSender, InviteStatus.PARTY_FULL), pkg)
                    }
                    else {
                        logger.info("Sending response of invited player...")
                        packageService.sendResponse(PartyServerInviteResponsePackage(clientResponse.partyId, clientResponse.responder, clientResponse.initalSender, clientResponse.status), pkg)
                    }
                }
                else {
                    logger.info("Party invitation expired, sending error message back...")
                    packageService.sendResponse(PartyServerInviteResponsePackage(clientResponse.partyId, clientResponse.responder, clientResponse.initalSender, InviteStatus.EXPIRED), pkg)
                }
            }
            else {
                logger.info("Party not present anymore, sending error message back...")
                packageService.sendResponse(PartyServerInviteResponsePackage(clientResponse.partyId, clientResponse.responder, clientResponse.initalSender, InviteStatus.PARTY_NOT_PRESENT), pkg)
            }
        })
    }
}