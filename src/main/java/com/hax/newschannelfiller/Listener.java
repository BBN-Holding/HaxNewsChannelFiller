package com.hax.newschannelfiller;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;

public class Listener extends ListenerAdapter {

    JSONObject config;

    public Listener(JSONObject config) {
        this.config = config;
    }

    ArrayList<Message> lastmessages = new ArrayList<>();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        this.updateNewsChannel(event.getJDA());
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!(event.getChannel().getId().equals(config.getString("VCLOG_ID"))
                && event.getAuthor().getId().equals("BBNBOT_ID"))&&
                !event.getChannel().getId().equals(config.getString("NC_ID"))) {
            if (lastmessages.size() == 5) {
                lastmessages.remove(4);
            }
            lastmessages.add(0, event.getMessage());
        }
        this.updateNewsChannel(event.getJDA());
    }

    public void updateNewsChannel(JDA jda) {
        TextChannel channel = jda.getTextChannelById(config.getString("NC_ID"));
        MessageHistory history = channel.getHistory();
        history.retrievePast(100).queue(
                success -> {
                    if (history.size() != 0) {
                        for (int i = 0; i < history.size(); i++) {
                            Message message = history.getRetrievedHistory().get(i);
                            if (message.getAuthor().getId().equals(jda.getSelfUser().getId())) {
                                if (i != 0) {
                                    String lastlastmessages = message.getEmbeds().get(0).getFields().get(1).getValue();
                                    message.delete().queue();
                                    channel.sendMessage(new EmbedBuilder().setTitle("This Message will get updated").build()).queue(msgtoupdate -> this.updateMessage(msgtoupdate, lastlastmessages));
                                    return;
                                } else {
                                    this.updateMessage(message, null);
                                    return;
                                }
                            }
                        }
                        channel.sendMessage(new EmbedBuilder().setTitle("This Message will get updated").build()).queue(msgtoupdate -> this.updateMessage(msgtoupdate, null));
                    } else {
                        channel.sendMessage(new EmbedBuilder().setTitle("This Message will get updated").build()).queue(msgtoupdate -> this.updateMessage(msgtoupdate, null));
                    }
                }
        );
    }

    public void updateMessage(Message message, String lastlastmessages) {
        ArrayList<String> vcactions = new ArrayList<>();
        TextChannel voicelogchannel = message.getGuild().getTextChannelById(config.getString("VCLOG_ID"));
        assert voicelogchannel != null;
        voicelogchannel.getHistory().retrievePast(100).queue(
                messages -> {
                    for (int i = 0; vcactions.size() < 5; i++) {
                        Message vcmessage = messages.get(i);
                        if (vcmessage.getAuthor().getId().equals(config.getString("BBNBOT_ID"))) {
                            if (vcmessage.getEmbeds().size() == 1) {
                                MessageEmbed messageEmbed = vcmessage.getEmbeds().get(0);
                                vcactions.add(String.format("%s - %s (%s)", messageEmbed.getTitle(),
                                        messageEmbed.getFields().get(0).getValue(), messageEmbed.getFields().get(1).getValue()));
                            }
                        }
                    }
                    String lastmessagesoutput;
                    if (lastlastmessages == null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Message lastmessage : lastmessages) {
                            stringBuilder.append(String.format("#%s - %s: %s (%s Attachment(s))\n", lastmessage.getChannel().getName(),
                                    lastmessage.getAuthor().getAsTag(), lastmessage.getContentRaw(), lastmessage.getAttachments().size()));
                        }
                        if (stringBuilder.chars().count() == 0) {
                            lastmessagesoutput = "No new captured Messages";
                        } else lastmessagesoutput = stringBuilder.toString();
                    } else lastmessagesoutput = lastlastmessages;

                    message.editMessage(
                            new EmbedBuilder()
                                    .setTitle("Overview")
                                    .addField("Last 5 Actions in #voicelog", String.join("\n", vcactions.toArray(new String[0])), true)
                                    .addField("Last 5 new Messages", lastmessagesoutput, true)
                                    .setFooter("Last updated", "https://bigbotnetwork.com/images/avatar.png")
                                    .setTimestamp(Instant.now())
                                    .build()
                    ).queue();
                }
        );
    }
}
