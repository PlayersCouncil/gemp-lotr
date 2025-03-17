package com.gempukku.lotro.draft3.format.fotr_power_max;

import com.gempukku.lotro.draft3.StartingCollectionProducer;
import com.gempukku.lotro.game.*;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Random;

public class FotrPowerMaxTableDraftStartingCollectionProducer implements StartingCollectionProducer {
    private static final int HIGH_ENOUGH_PRIME_NUMBER = 8963;


    private static final List<String> CORE_CARDS = List.of("1_2", "1_290", "1_299", "Fellowship Site Pack", "1_231", "1_234", "2_51"); // Frodo, Ring, Sword, Sites, Enquea, Nertea, Balrog
    private static final int FP_WHEEL_LENGTH = 7;
    private static final List<String> FP1_CARDS = List.of(
            "1_310", "1_296", "1_307", // Hobbits - Sam Faithful, Bill, Pippin
            "1_365", "1_112", "1_92", "1_365", // Aragorn, Sword, Armor, Aragorn
            "1_311", "1_317", "1_303", // Hobbits - Sam, Tale, Merry
            "2_122", "1_75", "1_87", "2_122", // Gandalf, Glamdring, Never Late, Gandalf
            "2_114", "1_296", "1_303", //Hobbits - Sam, Intuition, Merry
            "1_365", "1_92", "1_112", "1_365", // Aragorn, Armor, Sword, Aragorn
            "1_307", "1_298", "1_303", // Hobbits - PippinR, Stealth, Merry
            "2_122", "2_29", "1_75", "2_122"); // Gandalf, Staff, Glamdring, Gandalf
    private static final List<String> FP2_CARDS = List.of(
            "2_121", "1_15", "1_11", "1_9", "2_121", // 5x dwarves - Gimli, Helm, Farin, Dwarven Axe, Gimli
            "1_97", // 1x comp boromir
            "3_121", "3_21", "3_7", "1_31", "3_121",// 5x elves - Legolas, Long-knives of Legolas, Arwen Rider, Asfaloth, Legolas
            "1_97", // 1x comp boromir
            "2_121", "1_9", "2_6", "2_3", "2_121", // 5x dwarves - Gimli R, Dwarven Axe, Fror, Bracers, Gimli
            "1_97", // 1x comp boromir
            "3_7", "1_47", "3_121", "1_41", "3_7", // 5x elves - Arwen, Gwemegil, Legolas, Bow, Arwen
            "2_121", // 1x comp gimli
            "3_122", "1_94", "3_42", "2_37","3_122", // 5x gondor - Boromir Defender, Athelas, Horn, Sentinels of Numenor, Boromir Defender
            "3_121"); // 1x comp legolas
    private static final int SHADOW_WHEEL_LENGTH = 13;
    private static final List<String> SHADOW_CARDS = List.of(
            "1_270", "2_89", "1_262", "1_281", "2_93", "1_281","1_262", "2_89", "1_270", // Sauron Trackers - Orc Scouting Band, Orc Scout, Orc Assassin, Under the Watching Eye, Tower Assassin
            "1_191", "1_178", "1_184", "1_196", "1_165", "1_196", "1_184", "1_178", "1_191", // Moria - Moria Scout, Goblin Runner, Goblin Wallcrawler, They Are Coming, Cave Troll
            "1_158", "1_152", "1_145", "1_121", "1_143", "1_121", "1_145", "1_152", "1_158", // Uruks - Uruk-hai Raiding Party, Uruk Shaman, Uruk Brood, Bred For Battle, Troop of Uruk-hai
            "1_267", "1_270", "1_271", "1_272",  "1_264", "1_272", "1_271", "1_270", "1_267", // Sauron Wounds - Orc Hunters, Orc Scouting Band, Orc Soldier, Orc War Band, Orc Bowman
            "1_176", "2_67", "2_60", "1_180", "1_183", "1_180", "2_60", "2_67", "1_176", // Moria Archers - Goblin Marksman, Moria Archer Troop, Goblin Bowman, Goblin Scimitar, Goblin Swarms
            "3_57", "3_58", "3_69", "3_63", "3_64", "3_63", "3_69", "3_58", "3_57"); // Isengard Orcs - Isengard Retainer, Isengard Servant, Saruman, One of You Must Do This, Orc Commander

    public FotrPowerMaxTableDraftStartingCollectionProducer(){

    }

    @Override
    public MutableCardCollection getStartingCardCollection(String uniqueEventName, String playerName) {
        long seed = uniqueEventName.hashCode() + (long) playerName.hashCode() * HIGH_ENOUGH_PRIME_NUMBER;
        Random rnd = new Random(seed);

        final DefaultCardCollection startingCollection = new DefaultCardCollection();

        // Add Frodo SoD, the Ruling Ring, all fellowship site cards, enquea, nertea and balrog
        for (String coreCard : CORE_CARDS) {
            startingCollection.addItem(coreCard, 1);
        }
        // 7 cards from FP wheel 1
        int fp1Start = rnd.nextInt(FP1_CARDS.size());
        for (String fp1Card : getCyclingIterable(FP1_CARDS, fp1Start, FP_WHEEL_LENGTH)) {
            startingCollection.addItem(fp1Card, 1);
        }
        // 7 cards from FP wheel 2
        int fp2Start = rnd.nextInt(FP2_CARDS.size());
        for (String fp2Card : getCyclingIterable(FP2_CARDS, fp2Start, FP_WHEEL_LENGTH)) {
            startingCollection.addItem(fp2Card, 1);
        }
        // 13 cards from shadow wheel
        int shadowStart = rnd.nextInt(SHADOW_CARDS.size());
        for (String shadowCard : getCyclingIterable(SHADOW_CARDS, shadowStart, SHADOW_WHEEL_LENGTH)) {
            startingCollection.addItem(shadowCard, 1);
        }

        return startingCollection;
    }

    private static Iterable<String> getCyclingIterable(List<String> list, int start, int length) {
        return Iterables.limit(Iterables.skip(Iterables.cycle(list), start), length);
    }
}
