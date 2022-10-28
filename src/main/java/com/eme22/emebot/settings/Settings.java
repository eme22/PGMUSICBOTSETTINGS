
package com.eme22.emebot.settings;

import com.eme22.emebot.config.BotConfiguration;
import com.eme22.emebot.entities.Birthday;
import com.eme22.emebot.entities.MemeImage;
import com.eme22.emebot.entities.Poll;
import com.eme22.emebot.entities.RoleManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.jagrosh.jdautilities.command.GuildSettingsProvider;
import lombok.*;
import net.dv8tion.jda.api.entities.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "text_channel_id",
        "voice_channel_id",
        "dj_role_id",
        "admin_role_id",
        "default_playlist",
        "repeat_mode",
        "prefix",
        "skip_ratio",
        "bienvenidas_channel",
        "bienvenidas_channel_id",
        "bienvenidas_channel_image",
        "bienvenidas_channel_message",
        "despedidas_channel",
        "despedidas_channel_id",
        "despedidas_channel_image",
        "despedidas_channel_message",
        "image_only_channels_ids",
        "meme_images",
        "polls",
        "role_manager",
        "8ball_answers",
        "birthdays",
        "birthday_channel_id",
        "anti_raid_mode"
})

@AllArgsConstructor
@NoArgsConstructor
@With
@Getter
@Setter
@Entity
@ToString
public class Settings implements GuildSettingsProvider {

    @JsonIgnore
    @Transient
    private SettingsManager manager;
    @Id
    private long guild;
    @JsonProperty("text_channel_id")
    private long textChannelId;
    @JsonProperty("voice_channel_id")
    private long voiceChannelId;
    @JsonProperty("dj_role_id")
    private long djRoleId;
    @JsonProperty("admin_role_id")
    private long adminRoleId;
    @JsonIgnore
    private int volume = BotConfiguration.DEFAULT_VOLUME;
    @JsonProperty("default_playlist")
    private String defaultPlaylist;
    @JsonProperty("repeat_mode")
    private RepeatMode repeatMode;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("skip_ratio")
    private double skipRatio;
    @JsonProperty("bienvenidas_channel")
    private Boolean bienvenidasChannelEnabled = false;
    @JsonProperty("bienvenidas_channel_id")
    private long bienvenidasChannelId;
    @JsonProperty("bienvenidas_channel_image")
    private String bienvenidasChannelImage;
    @JsonProperty("bienvenidas_channel_message")
    private String bienvenidasChannelMessage;
    @JsonProperty("despedidas_channel")
    private Boolean despedidasChannelEnabled = false;
    @JsonProperty("despedidas_channel_id")
    private long despedidasChannelId;
    @JsonProperty("despedidas_channel_image")
    private String despedidasChannelImage;
    @JsonProperty("despedidas_channel_message")
    private String despedidasChannelMessage;
    @JsonProperty("image_only_channels_ids")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Long> imageOnlyChannelsIds = new ArrayList<>();
    @JsonProperty("meme_images")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<MemeImage> memeImages = new ArrayList<>();
    @JsonProperty("polls")
    @OneToMany(cascade = CascadeType.ALL)
    @NotFound(action = NotFoundAction.IGNORE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Poll> polls = new ArrayList<>();
    @JsonProperty("role_manager")
    @OneToMany(cascade = CascadeType.ALL)
    @NotFound(action = NotFoundAction.IGNORE)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<RoleManager> roleManagerList = new ArrayList<>();
    @JsonProperty("8ball_answers")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> eightBallAnswers = new ArrayList<>();

    @JsonProperty("birthdays")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Birthday> birthdays = new ArrayList<>();

    @JsonProperty("birthday_channel_id")
    private long birthdayChannelId;

    @JsonProperty("anti_raid_mode")
    private Boolean antiRaidMode = false;

    public void addPollForGuild(Long messageId, Poll poll) {
        if (this.polls.stream().anyMatch(poll1 -> poll1.getId() == messageId))
            return;
        this.polls.add(poll.withId(messageId));
    }

    public void removePollFromGuild(Long messageId) {
        this.polls.removeIf(poll -> poll.getId() == messageId);
    }

    public TextChannel getTextChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(textChannelId);
    }

    public VoiceChannel getVoiceChannel(Guild guild) {
        return guild == null ? null : guild.getVoiceChannelById(voiceChannelId);
    }

    public TextChannel getHelloChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(bienvenidasChannelId);
    }

    public TextChannel getGoodbyeChannel(Guild guild) {
        return guild == null ? null : guild.getTextChannelById(despedidasChannelId);
    }
    @Transient
    public ArrayList<TextChannel> getOnlyImageChannels(Guild guild) {

        ArrayList<TextChannel> channels = new ArrayList<>();
        this.imageOnlyChannelsIds
                .forEach(channelid -> channels.add(guild.getTextChannelById(String.valueOf(channelid))));
        return channels;
    }

    public void addOnlyImageChannels(TextChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        if (imageOnlyChannelsIds.contains(channel))
            return;

        this.imageOnlyChannelsIds.add(channel);
    }

    public void addOnlyImageChannels(MessageChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        if (imageOnlyChannelsIds.contains(channel))
            return;

        this.imageOnlyChannelsIds.add(channel);
    }

    public void removeFromOnlyImageChannels(TextChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        imageOnlyChannelsIds.removeIf(element -> element.equals(channel));
    }

    public void removeFromOnlyImageChannels(MessageChannel onlyImageChannel) {

        Long channel = onlyImageChannel.getIdLong();

        imageOnlyChannelsIds.removeIf(element -> element.equals(channel));
    }

    public boolean isOnlyImageChannel(TextChannel textChannel) {
        return imageOnlyChannelsIds.stream().anyMatch(channel -> channel.equals(textChannel.getIdLong()));
    }

    public boolean isOnlyImageChannel(MessageChannel textChannel) {
        return imageOnlyChannelsIds.stream().anyMatch(channel -> channel.equals(textChannel.getIdLong()));
    }

    @JsonIgnore
    public MemeImage getRandomMemeImages() {
        int rand = new Random().nextInt(this.memeImages.size());
        return this.memeImages.get(rand);
    }

    public void addToMemeImages(String message, String imageLink) {

        MemeImage meme = new MemeImage(message, imageLink);
        if (this.memeImages.contains(meme))
            return;

        this.memeImages.add(meme);
    }

    public void deleteFromMemeImages(int position) {
        this.memeImages.remove(position);
    }

    @JsonIgnore
    public String getRandomAnswer() {

        if (this.eightBallAnswers.isEmpty())
            addDefault8BallAnswers();

        int rand = new Random().nextInt(this.eightBallAnswers.size());
        return this.eightBallAnswers.get(rand);
    }

    @JsonIgnore
    private void addDefault8BallAnswers() {
        this.eightBallAnswers.add("Sí");
        this.eightBallAnswers.add("No");
    }

    public void addToEightBallAnswers(String answer) {
        if (this.eightBallAnswers.contains(answer))
            return;

        this.eightBallAnswers.add(answer);
    }

    public void removeFrom8BallAnswers(int answer) {
        this.eightBallAnswers.remove(answer);
    }

    public void addToRoleManagers(RoleManager manager) {
        this.roleManagerList.add(manager);
    }

    @JsonIgnore
    public RoleManager getRoleManager(Long messageID) {
        return roleManagerList.stream().filter(manager -> manager.getId() == messageID).findFirst().orElse(null);
    }

    public void deleteRoleManagers(Long messageID) {
        this.roleManagerList.removeIf(memeImage -> memeImage.getId() == messageID);
    }

    public void clearServerData(Guild guild) {
        this.manager.deleteSettings(guild.getId());
    }

    @JsonIgnore
    public Role getAdminRoleId(Guild guild) {
        return guild == null ? null : guild.getRoleById(adminRoleId);
    }

    @JsonIgnore
    public Role getDJRoleId(Guild guild) {
        return guild == null ? null : guild.getRoleById(djRoleId);
    }

    public void addBirthDay(Birthday birthday) {
        if (this.birthdays.contains(birthday))
            return;

        this.birthdays.add(birthday);
    }

    public void removeBirthDay(long user) {
        this.birthdays.removeIf(birthday -> birthday.getUserId() == user);
    }

    @JsonIgnore
    @Nullable
    @Override
    public Collection<String> getPrefixes() {
        return prefix == null ? Collections.emptySet() : Collections.singleton(prefix);
    }

    public Birthday getUserBirthday(long idLong) {
        return birthdays.stream().filter(birthday -> birthday.getUserId() == idLong).findFirst().orElse(null);
    }
}
