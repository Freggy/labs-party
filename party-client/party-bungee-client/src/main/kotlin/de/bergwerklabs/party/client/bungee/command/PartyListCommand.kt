package de.bergwerklabs.party.client.bungee.command

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.party.api.Party
import de.bergwerklabs.party.api.PartyApi
import de.bergwerklabs.party.client.bungee.partyBungeeClient
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Created by Yannic Rieger on 30.09.2017.
 *
 * Lists the player currently in a party.
 *
 * @author Yannic Rieger
 */
class PartyListCommand : BungeeCommand {
    
    override fun getUsage() = "/party list"
    
    override fun getName() = "list"
    
    override fun getDescription() = "Listet alle Mitglieder der Party auf."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            PartyApi.getParty(sender.uniqueId, Consumer { optional ->
                if (optional.isPresent) {
                    val party = optional.get()
                    
                    if (party.getMembers().isEmpty()) {
                        partyBungeeClient!!.messenger.message("§cEs sind keine Mitglieder in deiner Party.", sender)
                        return@Consumer
                    }
                    
                    if (party.isOwner(sender.uniqueId)) {
                        this.displayOwnerView(sender, party)
                    }
                    else this.displayMemberView(sender, party)
                }
                else partyBungeeClient!!.messenger.message("§cDu bist in keiner Party.", sender)
            })
        }
    }
    
    /**
     * Display the owner view of a party, providing methods to easily promote or kick players.
     *
     * @param player player to send the messages to.
     * @param party  party that the player is the owner of.
     */
    private fun displayOwnerView(player: ProxiedPlayer, party: Party) {
        val future = CompletableFuture<List<PlayerNameToUuidMapping>>()
        partyBungeeClient!!.proxy.scheduler.runAsync(partyBungeeClient!!, {
            future.complete(party.getMembers().map { member -> PlayerResolver.resolveUuidToNameAsync(member).join().get() }.toList())
        })
        
        future.thenAccept { mappings ->
            player.sendMessage(
                ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-----§b Party-Übersicht §6§m-----")
            )
            mappings.forEach { mapping ->
                if (mapping.uuid != party.getPartyOwner()) {
                    val message = ComponentBuilder("✖").color(ChatColor.RED)
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party kick ${mapping.name}"))
                        .event(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText("Entfernt ${mapping.name} von der Party.")
                            )
                        )
                        .append("☗").color(ChatColor.GREEN)
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party promote ${mapping.name}"))
                        .event(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText("Befördert ${mapping.name} zun neuen Party-Owner.")
                            )
                        )
                        .append("➥").color(ChatColor.AQUA)
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party tp ${mapping.name}"))
                        .event(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText("Du wirst zu ${mapping.name} teleportiert.")
                            )
                        )
                        .append(" ${mapping.name}")
                        .create()
                    player.sendMessage(ChatMessageType.CHAT, *message)
                }
            }
            player.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------------------------"))
        }
    }
    
    /**
     * Displays the basic view of a party to the given player.
     *
     * @param player player to send the member view to.
     * @param party  party the player is a member of.
     */
    private fun displayMemberView(player: ProxiedPlayer, party: Party) {
        val future = CompletableFuture<List<PlayerNameToUuidMapping>>()
        partyBungeeClient!!.proxy.scheduler.runAsync(partyBungeeClient!!, {
            future.complete(party.getMembers().map { member -> PlayerResolver.resolveUuidToNameAsync(member).join().get() })
        })
        
        
        future.thenAccept { mappings ->
            player.sendMessage(
                ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-----§b Party-Übersicht §6§m-----")
            )
            
            mappings.forEach { mapping ->
                val name = mapping.name
                if (mapping.uuid == party.getPartyOwner()) {
                    val message = ComponentBuilder("■").color(ChatColor.GOLD)
                        .append("➥").color(ChatColor.AQUA)
                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party tp $name"))
                        .event(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                TextComponent.fromLegacyText("Du wirst zu $name teleportiert.")
                            )
                        )
                        .append(" $name").color(
                            ChatColor.getByChar(partyBungeeClient!!.bridge.getGroupPrefix(party.getPartyOwner())[1])
                        )
                        .create()
                    player.sendMessage(ChatMessageType.CHAT, *message)
                }
                else {
                    if (mapping.uuid != player.uniqueId) {
                        val msg = ComponentBuilder("■").color(ChatColor.GREEN)
                            .append("➥").color(ChatColor.AQUA)
                            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party tp $name"))
                            .event(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    TextComponent.fromLegacyText("Du wirst zu $name teleportiert.")
                                )
                            )
                            .append(" $name").color(
                                ChatColor.getByChar(partyBungeeClient!!.bridge.getGroupPrefix(mapping.uuid)[1])
                            )
                            .create()
                        player.sendMessage(ChatMessageType.CHAT, *msg)
                    }
                }
            }
            player.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------------------------"))
        }
    }
}