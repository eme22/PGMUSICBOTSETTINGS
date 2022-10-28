/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eme22.emebot.audio;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.entities.Pair;
import com.eme22.emebot.settings.Settings;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class NowplayingHandler
{
    private final Bot bot;
    private final HashMap<Long, Pair<Long,Long>> lastNP; // guild -> channel,message

    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public NowplayingHandler(Bot bot)
    {
        this.bot = bot;
        this.lastNP = new HashMap<>();
    }
    
    public void init()
    {
        if(!bot.getConfig().isNpImages())
            bot.getThreadpool().scheduleWithFixedDelay(this::updateAll, 0, 5, TimeUnit.SECONDS);
    }
    
    public void setLastNPMessage(Message m)
    {
        clearLastNPMessage((m.getGuild()));
        lastNP.put(m.getGuild().getIdLong(), new Pair<>(m.getTextChannel().getIdLong(), m.getIdLong()));
    }
    
    public void clearLastNPMessage(Guild guild)
    {
        deleteLastMessage(lastNP.get(guild.getIdLong()));
        lastNP.remove(guild.getIdLong());
    }

    public void clearLastNPMessage(long guild)
    {
        deleteLastMessage(lastNP.get(guild));
        lastNP.remove(guild);
    }

    private void deleteLastMessage(Pair<Long, Long> lastmessage) {
        if (lastmessage != null)
        {
            TextChannel music = bot.getJDA().getTextChannelById(lastmessage.getKey());
            if (music != null) {
                music.deleteMessageById(lastmessage.getValue()).queue();
            }
        }
    }

    private void updateAll()
    {
        Set<Long> toRemove = new HashSet<>();
        for(long guildId: lastNP.keySet())
        {
            Guild guild = bot.getJDA().getGuildById(guildId);
            if(guild==null)
            {
                toRemove.add(guildId);
                continue;
            }
            Pair<Long,Long> pair = lastNP.get(guildId);
            TextChannel tc = guild.getTextChannelById(pair.getKey());
            if(tc==null)
            {
                toRemove.add(guildId);
                continue;
            }
            AudioHandler handler = (AudioHandler)guild.getAudioManager().getSendingHandler();
            Message msg = handler.getNowPlaying(bot.getJDA());
            if(msg==null)
            {
                msg = handler.getNoMusicPlaying(bot.getJDA());
                toRemove.add(guildId);
            }
            try 
            {
                tc.editMessageById(pair.getValue(), msg).queue(m->{}, t -> lastNP.remove(guildId));
            } 
            catch(Exception e) 
            {
                toRemove.add(guildId);
            }
        }
        toRemove.forEach(lastNP::remove);
    }
    
    public void updateTopic(long guildId, AudioHandler handler, boolean wait)
    {
        Guild guild = bot.getJDA().getGuildById(guildId);
        if(guild==null)
            return;
        Settings settings = bot.getSettingsManager().getSettings(guildId);
        TextChannel tchan = settings.getTextChannel(guild);
        if(tchan!=null && guild.getSelfMember().hasPermission(tchan, Permission.MANAGE_CHANNEL))
        {
            String otherText;
            String topic = tchan.getTopic();
            if(topic==null || topic.isEmpty())
                otherText = "\u200B";
            else if(topic.contains("\u200B"))
                otherText = topic.substring(topic.lastIndexOf("\u200B"));
            else
                otherText = "\u200B\n "+topic;
            String text = handler.getTopicFormat(bot.getJDA()) + otherText;
            if(!text.equals(tchan.getTopic()))
            {
                try 
                {
                    // normally here if 'wait' was false, we'd want to queue, however,
                    // new discord ratelimits specifically limiting changing channel topics
                    // mean we don't want a backlog of changes piling up, so if we hit a 
                    // ratelimit, we just won't change the topic this time
                    tchan.getManager().setTopic(text).complete(wait);

                } 
                catch(PermissionException e) { e.printStackTrace();}
                catch (RateLimitedException ignored) {
                    log.warn("La accion se ha ratelimitado, no se volvera a intentar hasta el siguiente evento");
                }
            }
        }
    }
    
    // "event"-based methods
    public void onTrackUpdate(long guildId, AudioTrack track, AudioHandler handler)
    {
        // update bot status if applicable
        if(bot.getConfig().isSongInStatus())
        {
            if(track!=null && bot.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inVoiceChannel()).count()<=1)
                bot.getJDA().getPresence().setActivity(Activity.listening(track.getInfo().title));
            else
                bot.resetGame();
        }
        
        // update channel topic if applicable
        updateTopic(guildId, handler, false);
    }

    public Pair<Long, Long> getLastNP(Guild guild) {
        return lastNP.get(guild.getIdLong());
    }

    public void onMessageDelete(Guild guild, long messageId)
    {
        Pair<Long,Long> pair = lastNP.get(guild.getIdLong());
        if(pair==null)
            return;
        if(pair.getValue() == messageId)
            lastNP.remove(guild.getIdLong());
    }
}
