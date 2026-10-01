package com.gempukku.lotro.common;

import java.util.*;

public class JSONDefs {
    public static class Pack {
        public enum PackType {
            SELECTION, PACK, RANDOM, TENGWAR, RANDOM_FOIL, BOOSTER
        }

        public String name;
        public PackType type;
        /**
         * When true, the children this product emits are themselves opened rather than deposited.
         * This describes what happens to this product's <i>contents</i>, not to the product itself.
         */
        public boolean recursive = false;
        /**
         * When true, awarding this product to a player deposits its (recursively opened) contents
         * instead of the product itself.  Intended for art-less "random X" products which should
         * never sit visibly in a collection.  Ignored for SELECTION products, which need a player
         * to choose and therefore cannot be opened at award time.
         */
        public boolean openOnDelivery = false;
        public List<String> items;
        public Map<String, String> data;
    }

    public static class SealedTemplate {
        public String name;
        public String id;
        public String format;
        public List<List<String>> seriesProduct;
        public boolean hall = false;
    }

    public static class ItemStub {
        public String code;
        public String name;
        public ItemStub(String c, String n) {
            code = c;
            name = n;
        }
    }

    public static class LiveDraftInfo {
        public String code;
        public String name;
        public int maxPlayers;
        public String recommendedTimer;
        public LiveDraftInfo(String c, String n, int p, String t) {
            code = c;
            name = n;
            maxPlayers = p;
            recommendedTimer = t;
        }
    }

    public static class Format {
        public String adventure;
        public String code;
        public String name;
        /** Optional one- or two-sentence plain-English summary shown to players next to the format name. */
        public String description;
        /** Server-computed for the Play popup, e.g. "Cards from sets 1-10, V1-V3" (never read from lotrFormats.hjson). */
        public String setSummary;
        public int order = 1000;
        public String surveyUrl;
        public String sites;
        public boolean cancelRingBearerSkirmish = false;
        public boolean ruleOfFour = true;
        public boolean winAtEndOfRegroup = false;
        public boolean discardPileIsPublic = false;
        public boolean winOnControlling5Sites = false;
        public boolean playtest = false;
        public boolean validateShadowFPCount = true;
        public int minimumDeckSize = 60;
        public int maximumSameName = 4;
        public boolean mulliganRule = true;
        public boolean usesMaps = false;
        public ArrayList<Integer> sets;
        public ArrayList<String> blocks;
        public ArrayList<BlockFilter> blockFilters;
        public ArrayList<String> banned = new ArrayList<>();
        public ArrayList<String> restricted = new ArrayList<>();
        public ArrayList<String> valid = new ArrayList<>();
        public ArrayList<String> limit2 = new ArrayList<>();
        public ArrayList<String> limit3 = new ArrayList<>();
        public ArrayList<String> restrictedName = new ArrayList<>();
        public ArrayList<Integer> errataSets = new ArrayList<>();
        public Map<String, String> errata = new HashMap<>();
        public boolean hall = true;

    }

    public static class BlockFilter {
        public String name;
        public String filter;
        public Map<String, String> setFilters = new HashMap<>();
    }

    public static class Set {
        public int setId;
        public String setName;
        public String rarityFile;
        public boolean originalSet = true;
        public boolean merchantable = true;
        public boolean needsLoading = true;
        public boolean playable = true;
    }

    public static class FullFormatReadout {
        public Map<String, Format> Formats;
        public Map<String, SealedTemplate> SealedTemplates;
        public Map<String, ItemStub> DraftTemplates;
        public Map<String, ItemStub> TableDraftTemplates;
        public List<String> TableDraftTimerTypes;
        /** The game timers a Casual table can be opened with, in menu order. */
        public List<TimerInfo> HallTimers;
    }

    public static class TimerInfo {
        public String code;
        public String name;
        public int minutesPerPlayer;
        public int minutesPerDecision;
        /** The server's plain-English explanation of the timer. */
        public String description;
    }

    public static class PlayerMadeTournamentAvailableFormats {
        public List<ItemStub> constructed;
        public List<ItemStub> sealed;
        public List<ItemStub> soloDrafts;
        public List<LiveDraftInfo> tableDrafts;
        public List<String> draftTimerTypes;
    }

    public static class TimerType {
        public String type;
    }

    public static class ErrataInfo {
        public static String PC_Errata = "PC";
        public String BaseID;
        public String Name;
        public String LinkText;
        public Map<String, String> ErrataIDs;

        public String getPCErrata() {
            return ErrataIDs.get(PC_Errata);
        }
        public void addPCErrata(String errataBP) { ErrataIDs.put(PC_Errata, errataBP); }
    }

    public static class PlayHistoryStats {
        public List<DBDefs.FormatStats> Stats;
        public int ActivePlayers;
        public int GamesCount;
        /** games with a bot on either side (also counted in GamesCount) */
        public int BotGamesCount;
        public String StartDate;
        public String EndDate;
    }

}
