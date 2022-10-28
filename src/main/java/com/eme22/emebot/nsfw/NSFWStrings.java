package com.eme22.emebot.nsfw;

import java.util.Random;

public class NSFWStrings {

    public static final String[] kisses = {
            " Ha besado a ",
            " Le ha dado un chape a ",
            " Le ha robado un beso a "
    };

    public static final String[] bites = {
            " Le ha dado una probada a ",
            " Ha mordido a ",
            " Se ha querido comer a "
    };

    public static final String[] lick = {
            " Ha saboreado a ",
            " Ha lamido a "
    };

    public static final String[] slap = {
            " Ha abofeteado a ",
            " Ha cacheteado a "
    };

    public static final String[] poke = {
            " ha tocado a ",
            " esta molestando a "
    };

    public static final String[] fuck = {
            " se ha tirado a ",
            " ha hecho el amor con ",
            " ha follado a ",
            " ha hecho el delicioso con "
    };

    public static final String[] anal = {
            " ha analeado a",
            " le ha roto el ano a "
    };

    public static final String[] cum = {
            " se ha venido en ",
            " ha expulsado su chele en ",
            " ha eyaculado en "
    };

    private static final Random rand = new Random();

    public static String getRandomKiss(){
        return kisses[rand.nextInt(kisses.length)];
    }

    public static String getRandomBite() {
        return bites[rand.nextInt(bites.length)];
    }

    public static String getRandomLick() {
        return lick[rand.nextInt(lick.length)];
    }

    public static String getRandomSlap() {
        return slap[rand.nextInt(slap.length)];
    }

    public static String getRandomPoke() {return poke[rand.nextInt(poke.length)]; }

    public static String getRandomFuck() {return fuck[rand.nextInt(fuck.length)]; }

    public static String getRandomAnal() {return anal[rand.nextInt(anal.length)]; }

    public static String getRandomCum() {return cum[rand.nextInt(cum.length)]; }
}
