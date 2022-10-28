package com.eme22.emebot.entities;

public enum MusicPlayerEmoji {

    MUTE("\uD83D\uDD07"),
    NEXT("⏭"),
    PLAYORPAUSE("⏯"),
    LYRICS("\uD83C\uDFB5"),
    QUEUE("\uD83D\uDCC3");

    private final String emoji;

    MusicPlayerEmoji(String emoji) {
        this.emoji = emoji;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return emoji;
    }

    public static MusicPlayerEmoji isEmojiValid(String text){
        switch (text){
            default: return null;
            case "\uD83D\uDD07": return MUTE;
            case "⏭": return NEXT;
            case "⏯": return PLAYORPAUSE;
            case "\uD83C\uDFB5": return LYRICS;
            case "\uD83D\uDCC3": return QUEUE;
        }
    }
}
