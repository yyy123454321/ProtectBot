package com.github.yyy123454321.protectbot;

import java.awt.Event;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.intent.Intent;

public class Main extends Thread {
    public static DiscordApi api;
    public static Server protectServer;
    public static Long protectServerId = 866940300534218773L;
    public static List<Webhook> protectServerWebhooks;
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        String token =                                                                                                                                                                                 "ODY2OTUxODAxMDY0NzgzOTA0.YPaByA.Fce5R9VRThQ6-u091sAJxM68nsI";
        api = new DiscordApiBuilder().setToken(token)
                .setIntents(Intent.GUILDS,
                        Intent.GUILD_MEMBERS,
                        Intent.GUILD_WEBHOOKS,
                        Intent.GUILD_MESSAGES,
                        Intent.GUILD_INTEGRATIONS)
                .login().join();
        api.updateActivity("Protect");
        reload();
        api.addMessageCreateListener(event -> {
            try {
                if(event.getChannel().getType() == ChannelType.SERVER_TEXT_CHANNEL & !event.getMessageAuthor().isWebhook()) {
                    if(event.getServerTextChannel().get().getTopic().startsWith("id:")) {
                        MessageBuilder eventMessageBuilder = event.getMessage().toMessageBuilder();
                        for (ServerTextChannel i : protectServer.getTextChannels()) {
                            if(!i.getTopic().startsWith("id:")) continue;
                            eventMessageBuilder.send(getIncomingWebhookByName(i,event.getServerTextChannel().get().getTopic().split(" ", 2)[1]));
                        }
                        event.getMessage().delete();
                    }
                    else if(event.getMessageAuthor().isServerAdmin()) {
                        if(event.getMessageContent().equalsIgnoreCase("!reload")) reload();
                        if(event.getMessageContent().equalsIgnoreCase("!reset webhook")) {
                            event.getChannel().sendMessage("Reset...");
                            String printer = "";
                            protectServerWebhooks = protectServer.getWebhooks().get();
                            for (Webhook i : protectServerWebhooks) {
                                printer += i.getName();
                                printer += ": ";
                                printer += i.delete().isCompletedExceptionally();
                                printer += "\n";
                            }
                            event.getChannel().sendMessage(printer + "Reset Complete!");
                        }
                    }

                }
            } catch (Exception e) { e.printStackTrace(); }
        });

    }
    public static void reload() throws InterruptedException, ExecutionException {
        protectServer = api.getServerById(protectServerId).get();
        protectServerWebhooks = protectServer.getWebhooks().get();
        List<ServerTextChannel> protectServerTextChannels = protectServer.getTextChannels();
        Collection<User> protectServerMember = protectServer.getMembers();
        api.getServerTextChannelById(866940313716391936L).get().sendMessage("Reloading! Member: " + protectServerMember.size());
        for(User i:protectServerMember) {
            if(i.isBot()) continue;
            ServerTextChannel userChattingChannel = null;
            if(protectServer.getTextChannelsByName(i.getIdAsString()).size() == 0) {
                userChattingChannel = new ServerTextChannelBuilder(protectServer)
                        .setName(i.getIdAsString()).setTopic("id: " + protectServer.getTextChannels().size())
                        .addPermissionOverwrite(i, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build())
                        .addPermissionOverwrite(protectServer.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                        .create().get();
            } else {
                userChattingChannel = protectServer.getTextChannelsByName(i.getIdAsString()).get(0);
                userChattingChannel.createUpdater()
                        .setTopic("id: " + (userChattingChannel.getPosition()))
                        .addPermissionOverwrite(i, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build())
                        .addPermissionOverwrite(protectServer.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                        .update();
            }
        }
        api.getServerTextChannelById(866940313716391936L).get().sendMessage("Reload Complete.");
    }
    public static IncomingWebhook getIncomingWebhookByName(ServerTextChannel ch, String name) throws InterruptedException, ExecutionException {
        boolean IncomingWebhookIsPresent = false;
        IncomingWebhook r = null;
        List<IncomingWebhook> protectServerTextChannelWebhooks = ch.getIncomingWebhooks().get();
        if(protectServerTextChannelWebhooks.size() != 0) {
            for(IncomingWebhook i : protectServerTextChannelWebhooks) {
                if(!i.getName().isPresent()) continue;
                if(i.getName().get().equals(name)) {
                    r = i.asIncomingWebhook().get();
                    IncomingWebhookIsPresent = true;
                    break;
                }
            }
        }
        if(!IncomingWebhookIsPresent) {
            BufferedImage av = new BufferedImage(500,500, BufferedImage.TYPE_4BYTE_ABGR);
            r = ch.createWebhookBuilder().setName(name).setAvatar(av).create().get();
        }
        return r;
    }
    public void run() {

    }
}