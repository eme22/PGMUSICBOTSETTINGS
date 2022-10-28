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
package com.eme22.emebot.utils;

import com.eme22.emebot.MBotApplication;
import com.eme22.emebot.entities.Bot;
import com.eme22.emebot.config.BotConfiguration;
import com.eme22.emebot.entities.Answer;
import com.eme22.emebot.entities.Pair;
import com.eme22.emebot.entities.Poll;
import com.eme22.emebot.entities.Prompt;
import com.eme22.emebot.settings.Settings;
import com.jagrosh.jlyrics.Lyrics;
import com.jagrosh.jlyrics.LyricsClient;
import com.typesafe.config.Config;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
@Log4j2
public class OtherUtil
{
    public final static String NEW_VERSION_AVAILABLE = "Hay una nueva version de MBot\n"
                    + "Version Actual: %s\n"
                    + "Nueva Version: %s\n\n"
                    + "Please visit https://github.com/eme22/PGMUSICBOT/releases/latest to get the latest release.";
    private final static String WINDOWS_INVALID_PATH = "c:\\windows\\system32\\";

    private static final List<String> SHA = new ArrayList<>();

    /**
     * gets a Path from a String
     * also fixes the windows tendency to try to start in system32
     * any time the bot tries to access this path, it will instead start in the location of the jar file
     * 
     * @param path the string path
     * @return the Path object
     */
    public static Path getPath(String path)
    {
        Path result = Paths.get(path);
        // special logic to prevent trying to access system32
        if(result.toAbsolutePath().toString().toLowerCase().startsWith(WINDOWS_INVALID_PATH))
        {
            try
            {
                result = Paths.get(new File(MBotApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath() + File.separator + path);
            }
            catch(URISyntaxException ignored) {}
        }
        return result;
    }
    
    /**
     * Loads a resource from the jar as a string
     * 
     * @param clazz class base object
     * @param name name of resource
     * @return string containing the contents of the resource
     */
    public static String loadResource(Object clazz, String name)
    {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(clazz.getClass().getResourceAsStream(name)))))
        {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(line -> sb.append("\r\n").append(line));
            return sb.toString().trim();
        }
        catch(IOException ex)
        {
            return null;
        }
    }
    
    /**
     * Loads image data from a URL
     * 
     * @param url url of image
     * @return inputstream of url
     */
    public static InputStream imageFromUrl(String url)
    {
        if(url==null) {
            log.error("No se ha especificado una imagen", new NullPointerException());
            return null;
        }
        try (Response response = new OkHttpClient
                    .Builder()
                    .build()
                    .newCall(new Request
                            .Builder()
                            .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36")
                            .get()
                            .url(url)
                            .build()
                    )
                    .execute()) {
                ResponseBody body = response.body();
                if (body != null) {
                    return body.byteStream();
                }
            }
        catch(IOException | IllegalArgumentException exception) {
            log.error("No se ha podido obtener la imagen: " + url, exception);
        }
        return null;
    }
    
    /**
     * Parses an activity from a string
     * 
     * @param game the game, including the action such as 'playing' or 'watching'
     * @return the parsed activity
     */
    public static Activity parseGame(String game)
    {
        if(game==null || game.trim().isEmpty() || game.trim().equalsIgnoreCase("default"))
            return null;
        String lower = game.toLowerCase();
        if(lower.startsWith("playing"))
            return Activity.playing(makeNonEmpty(game.substring(7).trim()));
        if(lower.startsWith("listening to"))
            return Activity.listening(makeNonEmpty(game.substring(12).trim()));
        if(lower.startsWith("listening"))
            return Activity.listening(makeNonEmpty(game.substring(9).trim()));
        if(lower.startsWith("watching"))
            return Activity.watching(makeNonEmpty(game.substring(8).trim()));
        if(lower.startsWith("streaming"))
        {
            String[] parts = game.substring(9).trim().split("\\s+", 2);
            if(parts.length == 2)
            {
                return Activity.streaming(makeNonEmpty(parts[1]), "https://twitch.tv/"+parts[0]);
            }
        }
        return Activity.playing(game);
    }
   
    public static String makeNonEmpty(String str)
    {
        return str == null || str.isEmpty() ? "\u200B" : str;
    }
    
    public static OnlineStatus parseStatus(String status)
    {
        if(status==null || status.trim().isEmpty())
            return OnlineStatus.ONLINE;
        OnlineStatus st = OnlineStatus.fromKey(status);
        return st == null ? OnlineStatus.ONLINE : st;
    }
    
    public static String checkVersion(Prompt prompt)
    {
        // Get current version number
        String version = getCurrentVersion();
        
        // Check for new version
        String latestVersion = getLatestVersion();
        
        if(latestVersion!=null && !latestVersion.equals(version))
        {
            prompt.alert(Prompt.Level.WARNING, String.format(NEW_VERSION_AVAILABLE, version, latestVersion));
        }
        
        // Return the current version
        return version;
    }
    
    public static String getCurrentVersion()
    {
        if(MBotApplication.class.getPackage()!=null && MBotApplication.class.getPackage().getImplementationVersion()!=null)
            return MBotApplication.class.getPackage().getImplementationVersion();
        else
            return "UNKNOWN";
    }


    public static String getLatestVersion()
    {
        try
        {
            Response response = new OkHttpClient.Builder().build()
                    .newCall(new Request.Builder().get().url("https://api.github.com/repos/eme22/PGMUSICBOT/releases/latest").build())
                    .execute();
            ResponseBody body = response.body();
            if(body != null)
            {
                try(Reader reader = body.charStream())
                {
                    JSONObject obj = new JSONObject(new JSONTokener(reader));
                    return obj.getString("tag_name");
                }
                finally
                {
                    response.close();
                }
            }
            else
                return null;
        }
        catch(IOException | JSONException | NullPointerException ex)
        {
            return null;
        }
    }

    public static String makePollString(Poll poll){
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#.##");

        sb.append("*").append(poll.getQuestion()).append("*").append("\n").append("\n");

        List<Answer> answers = poll.getAnswers();

        AtomicReference<Integer> votes = new AtomicReference<>(poll.getAllVoteCount());

        IntStream.range(0, answers.size())
                .forEach(index -> {
                    Answer answers1 = answers.get(index);
                    sb.append(OtherUtil.numtoString(index)).append(": ").append(answers1.getAnswer()).append("\n");
                    double perc;
                    float count = answers1.getVotes().size();
                    if (Math.abs(votes.get()) < 0.0001)
                        perc = 0;
                    else
                        perc = count /votes.get()*100;
                    int perc2 = (int) (perc/100*20);
                    sb.append("[ ").append(percentajetoDraw(perc2));
                    sb.append(" | ").append(df.format(perc)).append("% (").append(count).append(")]");
                    sb.append("\n");
                });
        return sb.toString();
    }

    public static String numtoString(int num){
        num = num % 10;
        switch (num) {
            default: return "";
            case 0: return ":zero:";
            case 1: return ":one:";
            case 2: return ":two:";
            case 3: return ":three:";
            case 4: return ":four:";
            case 5: return ":five:";
            case 6: return ":six:";
            case 7: return ":seven:";
            case 8: return ":eight:";
            case 9: return ":nine:";
        }
    }

    public static String percentajetoDraw(int num){

        switch (num) {
            default: return "";
            case 0: return  "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 1: return  "█⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 2: return  "██⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 3: return  "███⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 4: return  "████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 5: return  "█████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 6: return  "██████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 7: return  "███████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 8: return  "████████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 9: return  "█████████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 10: return "██████████⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 11: return "███████████⠀⠀⠀⠀⠀⠀⠀⠀⠀";
            case 12: return "████████████⠀⠀⠀⠀⠀⠀⠀⠀";
            case 13: return "█████████████⠀⠀⠀⠀⠀⠀⠀";
            case 14: return "██████████████⠀⠀⠀⠀⠀⠀";
            case 15: return "███████████████⠀⠀⠀⠀⠀";
            case 16: return "████████████████⠀⠀⠀⠀";
            case 17: return "█████████████████⠀⠀⠀";
            case 18: return "██████████████████⠀⠀";
            case 19: return "███████████████████⠀";
            case 20: return "████████████████████";
        }
    }

    public static void createImage(String message, String name, String id, InputStream background, String userImage, File image) throws IOException {

        try {
            int width = 1000, height = 500;

            BufferedImage userPic = null;
            try {
                InputStream userPicStream = imageFromUrl(userImage);
                userPic = ImageIO.read(userPicStream);
                userPicStream.close();
            } catch (IIOException e) {
                Logger log = LoggerFactory.getLogger("MusicBot");
                log.error("Exception", e);
            }

            if (userPic != null)
                userPic = createAvatar(userPic);


            BufferedImage background2 = ImageIO.read(background);

            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig2 = bi.createGraphics();

            ig2.drawImage(background2, 0,0, width, height, null );
            if (userPic != null)
                ig2.drawImage(userPic, 370, 25, null);

            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("trans.ttf");

            @SuppressWarnings("ConstantConditions")
            Font font2 = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(90f);
            Font font1 = font2.deriveFont(70f);
            ig2.setFont(font1);
            drawOutlinedAndCenteredString(message, width, height, ig2, 370);
            ig2.setFont(font2);
            ig2.setPaint(Color.white);
            drawOutlinedAndCenteredString(name, width, height, ig2, 470);
            ig2.dispose();


            ImageIO.write(bi, "png", image);

        } catch (FontFormatException ie) {
            ie.printStackTrace();
        }
    }

    private static void drawOutlinedAndCenteredString(String s, int w, int h, @NotNull Graphics2D g, int fh) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(s)) / 2;
        int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        y = fh == 0 ? y: fh;
        g.setColor(Color.black);
        g.drawString(s, x + 10, y);
        g.drawString(s, x - 10, y);
        g.drawString(s, x,y + 10);
        g.drawString(s, x, y - 10);
        g.setColor(Color.white);
        g.drawString(s, x, fh == 0 ? y: fh);

    }

    private static void drawCenteredString(String s, int w, int h, Graphics2D g, int fw, int fh, Color color) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(s)) / 2;
        int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.setColor(color);
        g.drawString(s, fw == 0 ? x: fw, fh == 0 ? y: fh);
    }


    private static BufferedImage createAvatar(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w + 10, h + 10, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        //g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 180, 180));
        g2.fill(new Ellipse2D.Float(0, 0, w, h));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(10));
        g2.drawOval(0, 0, w , h);
        g2.dispose();

        Image tmp = output.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
        output = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

        g2 = output.createGraphics();
        g2.drawImage(tmp, 0, 0, null);
        g2.dispose();


        return output;
    }

    public static void loadFileFromGit(File file, String token) throws IOException, NoSuchAlgorithmException {

        GitHub github = new GitHubBuilder().withOAuthToken(token).build();
        GHRepository repo = github.getRepository("eme22/PGMUSICBOTSETTINGS");

        Files.copy(repo.getTree("main").getEntry(file.getPath()).readAsBlob(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
        SHA.add(getFileChecksum(md5Digest, file));

    }

    public static void writeFileToGitHub(File file) throws IOException, NoSuchAlgorithmException {


        MessageDigest md5Digest = MessageDigest.getInstance("SHA-256");
        String checksum = getFileChecksum(md5Digest, file);

        if (SHA.contains(checksum))
            return;

        GitHub github = new GitHubBuilder().withOAuthToken(System.getenv("GITHUB_OAUTH")).build();
        GHRepository repo = github.getRepository("eme22/PGMUSICBOTSETTINGS");
        GHRef masterRef = repo.getRef("heads/main");
        String masterTreeSha = repo.getTree("main").getSha();

        GitCommit commit = repo.createContent()
                .content(Files.readAllBytes(file.toPath()))
                .message(LocalDateTime.now() + " Settings")
                .path(file.getName())
                .sha(repo.getFileContent(file.getName()).getSha())
                .commit()
                .getCommit();

        masterRef.updateTo(commit.getSHA1());


    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static boolean hasValue(JSONArray json, String value) {
        for(int i = 0; i < json.length(); i++) {  // iterate through the JsonArray
            // first I get the 'i' JsonElement as a JsonObject, then I get the key as a string and I compare it with the value
            if(json.get(i).equals(value)) return true;
        }
        return false;
    }

    public static boolean hasValue(JSONArray json, Integer value) {
        for(int i = 0; i < json.length(); i++) {  // iterate through the JsonArray
            // first I get the 'i' JsonElement as a JsonObject, then I get the key as a string and I compare it with the value
            if(json.get(i).equals(value)) return true;
        }
        return false;
    }

    public static boolean hasValue(JSONArray json, Long value) {
        for(int i = 0; i < json.length(); i++) {  // iterate through the JsonArray
            // first I get the 'i' JsonElement as a JsonObject, then I get the key as a string and I compare it with the value
            if(json.get(i).equals(value)) return true;
        }
        return false;
    }

    public static boolean hasValue(JSONArray json, Map<String,String> value) {
        for(int i = 0; i < json.length(); i++) {  // iterate through the JsonArray
            // first I get the 'i' JsonElement as a JsonObject, then I get the key as a string and I compare it with the value
            if(json.get(i).equals(value)) return true;
        }
        return false;
    }

    public static boolean hasValue(JSONArray json, Pair<String, String> value) {
        for(int i = 0; i < json.length(); i++)
            if(json.get(i).equals(value)) return true;
        return false;
    }

    public static boolean hasValue(JSONArray json, JSONArray value) {
        for(int i = 0; i < json.length(); i++)
            if(json.get(i).equals(value)) return true;
        return false;
    }

    public static boolean hasValue(ArrayList<Long> list, Long channel) {
        for (Long aLong : list)
            if (aLong.equals(channel)) return true;
        return false;
    }

    public static boolean hasValue(ArrayList<Pair<String, String>> data, Pair<String, String> meme) {

        for (Pair<String,String> num : data)
            if (num.equals(meme)) return true;
        return false;

    }

    public static boolean hasValue(HashSet<Long> list, Long channel) {
        for (Long aLong : list)
            if (aLong.equals(channel)) return true;
        return false;
    }

    public static boolean hasValue(List<Map<String, String>> list, Map<String, String> meme) {
        for (Map<String, String> data : list)
            if (data.equals(meme)) return true;
        return false;
    }

    public static int EmojiToNumber(String emoji) {
        switch (emoji){
            default: return -1;
            case "0️⃣": return 0;
            case "1️⃣": return 1;
            case "2️⃣": return 2;
            case "3️⃣": return 3;
            case "4️⃣": return 4;
            case "5️⃣": return 5;
            case "6️⃣": return 6;
            case "7️⃣": return 7;
            case "8️⃣": return 8;
            case "9️⃣": return 9;

        }
    }

    public static boolean isValidUrl(String imageAddress) {

        try {
            new URL(imageAddress).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }

        return true;
    }

    public static boolean checkImage(String imageAddress) {
        if(isValidUrl(imageAddress)) {
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) new URL(imageAddress).openConnection();
                connection.setRequestMethod("HEAD");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            String contentType = connection.getHeaderField("Content-Type");

            return contentType.startsWith("image/");
        }
        return false;
    }

    public static InputStream getBackground(Settings settingsTEST, boolean b) {
        if (b){
            String image = settingsTEST.getBienvenidasChannelImage();
            if ( image == null){
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                return classloader.getResourceAsStream("images/bienvenida.png");
            }
            else {
                return imageFromUrl(image);
            }
        } else {
            String image = settingsTEST.getDespedidasChannelImage();
            if ( image == null){
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                return classloader.getResourceAsStream("images/despedida.png");
            }
            else {
                return imageFromUrl(image);
            }
        }
    }

    public static String getMessage(Bot bot, Guild guild, boolean b) {

        Settings settings = bot.getSettingsManager().getSettings(guild.getIdLong());
        BotConfiguration config = bot.getConfig();

        String message = b ? settings.getBienvenidasChannelMessage() : settings.getDespedidasChannelMessage();

        if (message == null)
            return b ? config.getWelcomeString() : config.getGoodByeString();
        else
            return message;
    }

    public static boolean isAudioChannelAllowed(Guild guild, Settings settings, Member member){
        VoiceChannel current = guild.getSelfMember().getVoiceState().getChannel();
        GuildVoiceState userState = member.getVoiceState();

        if(current==null) {
            current = settings.getVoiceChannel(guild);
            if (current == null) {
                return true;
            }
            else
                return userState.getChannel().equals(current);

        }
        else
            return userState.getChannel().equals(current);

    }

    public static int isUserInVoice(Guild guild, Settings settings, Member member){
        GuildVoiceState userState = member.getVoiceState();
        if (userState.inVoiceChannel()) {
            VoiceChannel afkChannel = guild.getAfkChannel();
            if(afkChannel != null && afkChannel.equals(userState.getChannel()))
                return 2;
            return 1;
        }
        return 0;
    }

    public static Lyrics getLyrics(String title) {

        // Remove words between parentheses
        title = title.replaceAll("\\(.*\\)", "");
        // Remove words between brackets
        title = title.replaceAll("\\[.*\\]", "");
        // Remove words between curly braces
        title = title.replaceAll("\\{.*\\}", "");

        String[] sources = { "A-Z Lyrics", "Genius", "MusixMatch", "LyricsFreak" };
        final LyricsClient client = new LyricsClient();
        try {
            for (String source : sources) {
                Lyrics lyrics = client.getLyrics(title, source).get();
                if (lyrics != null)
                    return lyrics;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isRoleHierarchyLower(@Nonnull List<Role> roles, @Nonnull Role matchRole) {
        Checks.notNull(matchRole, "Match roles can not be null");
        return isRoleHierarchyLower(roles, matchRole.getPosition());
    }

    public static boolean isRoleHierarchyLower(@Nonnull List<Role> roles, int hierarchyPosition) {
        for (Role role : roles) {
            if (role.getPosition() < hierarchyPosition) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRoleHierarchyLower(@Nonnull Role role, @Nonnull Role roleToCompare) {
        return role.getPosition() < roleToCompare.getPosition();
    }

    /**
     * Get the role that is position highest in the role hierarchy for the given member.
     *
     * @param member The member whos roles should be used.
     * @return Possibly-null, if the user has any roles the role that is ranked highest in the role hierarchy will be returned.
     */
    @Nullable
    public static Role getHighestFrom(@Nonnull Member member) {
        Checks.notNull(member, "Member object can not be null");
        List<Role> roles = member.getRoles();
        return getHighestRole(roles);
    }

    @Nullable
    public static Role getHighestFrom(@Nonnull List<Role> roles) {
        Checks.notNull(roles, "Member object can not be null");
        return getHighestRole(roles);
    }

    @Nullable
    private static Role getHighestRole(@Nonnull List<Role> roles) {
        if (roles.isEmpty()) {
            return null;
        }
        return roles.stream().min((first, second) -> {
            if (first.getPosition() == second.getPosition()) {
                return 0;
            }
            return first.getPosition() > second.getPosition() ? -1 : 1;
        }).orElse(null);
    }

    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNullOrEmpty(String oldFile) {
        return oldFile == null || oldFile.isBlank();
    }

    public static boolean isNullOrEmpty(Map<String, String> transforms) {
        return transforms == null || transforms.isEmpty();
    }

    public static boolean isNullOrEmpty(Config oldTransforms) {
        return  oldTransforms == null;
    }

    public static boolean checkDev(String[] args) {
        return Arrays.asList(args).contains("-dev");
    }
}
