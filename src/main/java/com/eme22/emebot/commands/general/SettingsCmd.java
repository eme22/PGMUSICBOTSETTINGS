/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.eme22.emebot.commands.general;

import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.settings.RepeatMode;
import com.eme22.emebot.settings.Settings;
import com.eme22.emebot.utils.FormatUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Component
public class SettingsCmd extends SlashCommand {
        private final static String EMOJI = "\uD83C\uDFA7"; // 🎧

        public SettingsCmd(Bot bot) {
                this.name = "settings";
                this.help = "muestra las opciones del bot";
                this.aliases = bot.getConfig().getAliases(this.name);
                this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
                Settings s = getClient().getSettingsFor(event.getGuild());
                MessageBuilder builder = new MessageBuilder()
                                .append(EMOJI + " **")
                                .append(FormatUtil.filter(event.getGuild().getSelfMember().getUser().getName()))
                                .append("** settings:");
                TextChannel wchan = s.getHelloChannel(event.getGuild());
                TextChannel dchan = s.getGoodbyeChannel(event.getGuild());
                TextChannel tchan = s.getTextChannel(event.getGuild());
                VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
                Role djRole = s.getDJRoleId(event.getGuild());
                Role adminrole = s.getAdminRoleId(event.getGuild());
                ArrayList<TextChannel> onlyimages = s.getOnlyImageChannels(event.getGuild());

                EmbedBuilder ebuilder = new EmbedBuilder()
                                .setColor(event.getGuild().getSelfMember().getColor())
                                .setDescription("Canal de Musica: "
                                                + (tchan == null ? "Cualquiera" : "**#" + tchan.getName() + "**")
                                                + "\nCanal de Bienvenida: "
                                                + (wchan == null ? "Cualquiera" : "**#" + wchan.getName() + "**")
                                                + "\nCanal de Despedidas: "
                                                + (dchan == null ? "Cualquiera" : "**#" + dchan.getName() + "**")
                                                + "\nCanal de Voz: "
                                                + (vchan == null ? "Cualquiera" : vchan.getAsMention())
                                                + "\nRol de Admin: "
                                                + (adminrole == null ? "Ninguno" : "**" + adminrole.getName() + "**")
                                                + "\nRol de DJ: "
                                                + (djRole == null ? "Ninguno" : "**" + djRole.getName() + "**")
                                                + "\nCanales de solo imagenes: "
                                                + (onlyimages.isEmpty() ? "Ninguno" : "**" + onlyimages.size() + "**")

                                                + "\nPrefijo Personalizado: "
                                                + (s.getPrefix() == null ? "Ninguno" : "`" + s.getPrefix() + "`")
                                                + "\nModo de Repeticion: " + (s.getRepeatMode() == RepeatMode.OFF
                                                                ? s.getRepeatMode().getUserFriendlyName()
                                                                : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                                                + "\nPlaylist por defecto: "
                                                + (s.getDefaultPlaylist() == null ? "Ninguno"
                                                                : "**" + s.getDefaultPlaylist() + "**"))
                                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                                                + event.getJDA().getGuilds().stream()
                                                                .filter(g -> g.getSelfMember().getVoiceState()
                                                                                .inVoiceChannel())
                                                                .count()
                                                + " conecciones de audio", null);
                builder.setEmbeds(ebuilder.build());
                event.reply(builder.build()).queue();
        }

        @Override
        protected void execute(CommandEvent event) {
                Settings s = event.getClient().getSettingsFor(event.getGuild());
                MessageBuilder builder = new MessageBuilder()
                                .append(EMOJI + " **")
                                .append(FormatUtil.filter(event.getSelfUser().getName()))
                                .append("** settings:");
                TextChannel wchan = s.getHelloChannel(event.getGuild());
                TextChannel dchan = s.getGoodbyeChannel(event.getGuild());
                TextChannel tchan = s.getTextChannel(event.getGuild());
                VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
                Role djRole = s.getDJRoleId(event.getGuild());
                Role adminrole = s.getAdminRoleId(event.getGuild());
                ArrayList<TextChannel> onlyimages = s.getOnlyImageChannels(event.getGuild());

                EmbedBuilder ebuilder = new EmbedBuilder()
                                .setColor(event.getSelfMember().getColor())
                                .setDescription("Canal de Musica: "
                                                + (tchan == null ? "Cualquiera" : "**#" + tchan.getName() + "**")
                                                + "\nCanal de Bienvenida: "
                                                + (wchan == null ? "Cualquiera" : "**#" + wchan.getName() + "**")
                                                + "\nCanal de Despedidas: "
                                                + (dchan == null ? "Cualquiera" : "**#" + dchan.getName() + "**")
                                                + "\nCanal de Voz: "
                                                + (vchan == null ? "Cualquiera" : vchan.getAsMention())
                                                + "\nRol de Admin: "
                                                + (adminrole == null ? "Ninguno" : "**" + adminrole.getName() + "**")
                                                + "\nRol de DJ: "
                                                + (djRole == null ? "Ninguno" : "**" + djRole.getName() + "**")
                                                + "\nCanales de solo imagenes: "
                                                + (onlyimages.isEmpty() ? "Ninguno" : "**" + onlyimages.size() + "**")

                                                + "\nPrefijo Personalizado: "
                                                + (s.getPrefix() == null ? "Ninguno" : "`" + s.getPrefix() + "`")
                                                + "\nModo de Repeticion: " + (s.getRepeatMode() == RepeatMode.OFF
                                                                ? s.getRepeatMode().getUserFriendlyName()
                                                                : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                                                + "\nPlaylist por defecto: "
                                                + (s.getDefaultPlaylist() == null ? "Ninguno"
                                                                : "**" + s.getDefaultPlaylist() + "**"))
                                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                                                + event.getJDA().getGuilds().stream()
                                                                .filter(g -> g.getSelfMember().getVoiceState()
                                                                                .inVoiceChannel())
                                                                .count()
                                                + " conecciones de audio", null);
                builder.setEmbeds(ebuilder.build());
                event.reply(builder.build());
        }

}
