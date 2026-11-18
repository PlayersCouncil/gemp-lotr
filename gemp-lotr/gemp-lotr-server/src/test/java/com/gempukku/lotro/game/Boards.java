package com.gempukku.lotro.game;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.timing.loops.BoardFingerprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything on a game's table as text, the same text for the same table, for tests that compare one game with
 * another game that ought to be in the same place (a game and the game rebuilt from its journal).
 */
final class Boards {
    private Boards() { }

    /**
     * The assignments are put in order first: the game keeps them in the order it happened to read them out of a
     * hash map, which is not the same from one run to the next and means nothing.
     */
    static String of(LotroGame game) {
        String board;
        try {
            board = BoardFingerprint.of(game).toString();
        } catch (RuntimeException exp) {
            //Before the players have been seated (the bids) there is no board to read
            return "(no board yet)";
        }
        List<String> rest = new ArrayList<>();
        List<String> assignments = new ArrayList<>();
        for (String part : board.split("\\|"))
            (part.startsWith("A:") ? assignments : rest).add(part);
        Collections.sort(assignments);
        rest.addAll(assignments);
        return String.join("|", rest);
    }

    static final String ARAGORN_STARTER =
            "1_290|1_2|1_320,1_327,1_340,1_346,1_349,1_351,1_355,1_358,1_361|1_365,1_365,1_92,1_94,1_94,1_97," +
                    "1_97,1_101,1_104,1_104,1_106,1_107,1_107,1_296,1_296,1_299,1_299,1_51,1_108,1_108,1_110,1_110,1_309," +
                    "1_311,1_116,1_116,1_116,1_117,1_117,1_117,1_121,1_121,1_121,1_133,1_133,1_141,1_141,1_145,1_150," +
                    "1_150,1_150,1_150,1_151,1_151,1_151,1_152,1_152,1_152,1_152,1_153,1_153,1_153,1_153,1_154,1_154," +
                    "1_154,1_157,1_157,1_158,1_158";
    static final String GANDALF_STARTER =
            "1_290|1_2|1_326,1_331,1_337,1_345,1_349,1_350,1_353,1_359,1_360|1_70,1_97,1_286,1_286,1_286,1_37," +
                    "1_37,1_364,1_364,1_12,1_12,1_296,1_296,1_299,1_76,1_76,1_76,1_51,1_51,1_78,1_78,1_78,1_78,1_304," +
                    "1_304,1_84,1_312,1_26,1_26,1_86,1_168,1_168,1_168,1_176,1_176,1_176,1_176,1_177,1_178,1_178,1_178" +
                    ",1_178,1_179,1_179,1_179,1_180,1_180,1_180,1_180,1_181,1_181,1_181,1_187,1_187,1_187,1_191," +
                    "1_191,1_191,1_196,1_196";
}
