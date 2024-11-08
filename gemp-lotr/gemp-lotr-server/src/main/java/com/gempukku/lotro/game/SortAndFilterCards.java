package com.gempukku.lotro.game;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.util.MultipleComparator;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class SortAndFilterCards {
    public <T extends CardItem> List<T> process(String filter, Iterable<T> items, LotroCardBlueprintLibrary cardLibrary, LotroFormatLibrary formatLibrary) {
        if (filter == null)
            filter = "";
        var params = parseFilterParams(filter);

        var product = getSingleton(params.getOrDefault("product", new ArrayList<>()));
        var rarities = params.getOrDefault("rarity", new ArrayList<>());
        var side = Side.Parse(getSingleton(params.get("side")));
        var sort = params.getOrDefault("sort", new ArrayList<>());
        var sets = params.getOrDefault("set", new ArrayList<>());
        var cardTypes = getEnumFilter(CardType.values(), CardType.class, params.get("cardtype"), false);

        var cultures = getEnumFilter(Culture.values(), Culture.class, params.get("culture"), true);

        var nameWords = getWords(params.get("name"), true);
        var textWords = getWords(params.get("gametext"), false);

        var keywords = getEnumFilter(Keyword.values(), Keyword.class, params.get("keyword"), true);
        var phases = getEnumFilter(Keyword.values(),Keyword.class, params.get("phase"), true);
        var races = getEnumFilter(Race.values(), Race.class, params.get("race"), true);
        var itemClasses = getEnumFilter(PossessionClass.values(), PossessionClass.class, params.get("itemclass"), true);

        var twilight = getStat(params.get("sitenumber"));
        var twilightComparator = getSingleton(params.get("twilightcompare"));
        var siteNumber = getStat(params.get("sitenumber"));
        var siteNumberComparator = getSingleton(params.get("sitenumbercompare"));
        var strength = getStat(params.get("strength"));
        var strengthComparator = getSingleton(params.get("strengthcompare"));
        var vitality = getStat(params.get("vitality"));
        var vitalityComparator = getSingleton(params.get("vitalitycompare"));
        var resistance = getStat(params.get("resistance"));
        var resistanceComparator = getSingleton(params.get("resistancecompare"));
        var signets = getEnumFilter(Signet.values(), Signet.class, params.get("signet"), true);
        
        var canStartWithRing = getBoolean(params.get("canstartwithring"));

        List<T> result = new ArrayList<>();
        var cardBPCache = new HashMap<String, LotroCardBlueprint>();
        var setDefs = cardLibrary.getSetDefinitions();

        //In this giant loop, we will go through every item handed to us and see if it matches all of the above filter
        // conditions.  Whenever we fail a filter, we hit "continue" and move on to the next item to check.
        for (T item : items) {
            String blueprintId = item.getBlueprintId();
            String strippedId = BlueprintUtils.stripModifiers(blueprintId);

            if (isPack(blueprintId)) {
                if (product == null || product.equals("pack")) {
                    result.add(item);
                }
                continue;
            }

            //Cache our looked-up blueprints so we're not always re-searching; also used later for sorting
            try {
                if(!cardBPCache.containsKey(blueprintId)) {
                    cardBPCache.putIfAbsent(blueprintId, cardLibrary.getLotroCardBlueprint(blueprintId));
                }
            } catch (CardNotFoundException ignored) {
                // Ignore the card
                continue;
            }

            var card = cardBPCache.get(blueprintId);
            boolean valid = true;

            if(!IsFlagAccepted(canStartWithRing, card.canStartWithRing()))
                continue;

            if(product != null) {
                switch(product.toLowerCase()) {
                    case "foil" -> {
                        if (!blueprintId.contains("*"))
                            continue;
                    }
                    case "nonfoil" -> {
                        if (blueprintId.contains("*"))
                            continue;
                    }
                    case "tengwar" -> {
                        if (!blueprintId.contains("T"))
                            continue;
                    }
                    case "pack" -> {
                        continue;
                    }
                }
            }

            if(side != null) {
                if(side == Side.NONE) {
                    // The filter is looking for cards with no side, i.e. The One Ring, Sites, or Maps.
                    if(card.getSide() != null)
                        continue;
                }
                //Now compare for Freeps or Shadow
                else if(!side.equals(card.getSide()))
                    continue;
            }

            if(!rarities.isEmpty()) {
                var setDef = setDefs.get(BlueprintUtils.getSet(blueprintId));
                if(setDef != null) {
                    var rarity = setDef.getCardRarity(strippedId);
                    if(rarity == null || !rarities.contains(rarity))
                        continue;
                }
            }

            if(!sets.isEmpty() && !isInSets(blueprintId, card, sets, cardLibrary, formatLibrary))
                continue;

            if(!cardTypes.isEmpty() && !cardTypes.contains(card.getCardType()))
                continue;

            if(!cultures.isEmpty() && !cultures.contains(card.getCulture()))
                continue;

            if(!keywords.isEmpty() && !containsAnyKeywords(card, keywords))
                continue;

            if(!isAttributeValueAccepted(twilight, twilightComparator, card.getTwilightCost()))
                continue;

            if(card.getCardType() == CardType.ALLY) {
                for(var home : card.getAllyHomes()) {
                    if(!isAttributeValueAccepted(siteNumber, siteNumberComparator, home.siteNum())) {
                        valid = false;
                        break;
                    }
                }

                if(!valid)
                    continue;
            }
            else if(!isAttributeValueAccepted(siteNumber, siteNumberComparator, card.getSiteNumber()))
                continue;

            if(!isAttributeValueAccepted(strength, strengthComparator, card.getStrength()))
                continue;

            if(!isAttributeValueAccepted(vitality, vitalityComparator, card.getVitality()))
                continue;

            if(!isAttributeValueAccepted(resistance, resistanceComparator, card.getResistance()))
                continue;

            if(!signets.isEmpty() && !signets.contains(card.getSignet()))
                continue;

            if(!races.isEmpty() && !races.contains(card.getRace()))
                continue;

            if(!phases.isEmpty()) {
                if(card.getCardType() != CardType.EVENT || !containsAnyKeywords(card, phases))
                    continue;
            }

            if(!itemClasses.isEmpty() && !containsAnyClasses(card, itemClasses))
                continue;

            if(!nameWords.isEmpty() && !containsAllWords(GameUtils.getFullSanitizedName(card), nameWords))
                continue;

            if(!textWords.isEmpty() && !containsAllWords(GameUtils.getFullText(card), textWords))
                continue;

            //Reached the end of the gauntlet and nothing filtered it out.
            result.add(item);

        }

        if (sort.isEmpty() || sort.stream().noneMatch(StringUtils::isBlank)) {
            sort.add("name");
        }

        MultipleComparator<CardItem> comparators = new MultipleComparator<>();
        for (String oneSort : sort) {
            switch (oneSort) {
                case "twilight" ->
                        comparators.addComparator(new PacksFirstComparator(new TwilightComparator(cardBPCache)));
                case "siteNumber" ->
                        comparators.addComparator(new PacksFirstComparator(new SiteNumberComparator(cardBPCache)));
                case "strength" ->
                        comparators.addComparator(new PacksFirstComparator(new StrengthComparator(cardBPCache)));
                case "vitality" ->
                        comparators.addComparator(new PacksFirstComparator(new VitalityComparator(cardBPCache)));
                case "cardType" ->
                        comparators.addComparator(new PacksFirstComparator(new CardTypeComparator(cardBPCache)));
                case "culture" ->
                        comparators.addComparator(new PacksFirstComparator(new CultureComparator(cardBPCache)));
                case "name" ->
                        comparators.addComparator(new PacksFirstComparator(new NameComparator(cardBPCache)));
            }
        }

        result.sort(comparators);

        return result;
    }

    //Key: a particular filter parameter label
    //Value: List of individual arguments for that parameter, which may have been part of multiple different instances
    // of that parameter (e.g. "name:foo name:bar") or a comma-separated batch of instances (e.g. "name:foo,bar") or
    // any combination of the two.
    //Note that this coerces the key into lowercase
    private Map<String, List<String>> parseFilterParams(String filter) {
        String[] params = filter.split(" ");
        Map<String, List<String>> result = new HashMap<>();

        for (String param : params) {
            if(!param.contains(":"))
                continue;

            var parts = param.split(":");
            String key = parts[0].toLowerCase();

            if(!result.containsKey(key)) {
                var list = new ArrayList<>(Arrays.stream(parts[1].split(",")).toList());
                result.put(key, list);
            }
            else {
                result.get(key).addAll(Arrays.asList(parts[1].split(",")));
            }
        }

        return result;
    }

    private boolean isInSets(String blueprintId, LotroCardBlueprint card, List<String> setFilters, LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary) {
        for (String set : setFilters) {
            LotroFormat format = formatLibrary.getFormat(set);

            if (format != null) {
                if (card.getCardType() == CardType.SITE) {
                    String invalid = format.validateSite(blueprintId);
                    if(!StringUtils.isEmpty(invalid))
                        return false;
                }

                String invalid = format.validateCard(blueprintId);
                return StringUtils.isEmpty(invalid);
            }

            if (set.contains("-")) {
                final String[] split = set.split("-", 2);
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                for (int setNo = min; setNo <= max; setNo++) {
                    if (blueprintId.startsWith(setNo + "_") || library.hasAlternateInSet(blueprintId, setNo))
                        return true;
                }
            } else {
                if (blueprintId.startsWith(set + "_") || library.hasAlternateInSet(blueprintId, Integer.parseInt(set)))
                    return true;
            }
        }


        return false;
    }

    private List<String> getWords(List<String> params, boolean sanitize) {
        var result = new ArrayList<String>();
        if(params == null)
            return result;

        for (String str : params) {
            //If we're sanitizing, then we want to remove all spaces and diacritics.  If not,
            // then we need to coerce underscores in the input to preserve spaces
            if(sanitize) {
                for(String word : str.split(" ")) {
                    result.add(Names.SanitizeName(word));
                }
            }
            else {
                result.add(str.replace("_", " "));
            }
        }
        return result;
    }

    private Boolean getBoolean(List<String> params) {
        var statStr = getSingleton(params);
        if(statStr == null)
            return null;

        return Boolean.parseBoolean(statStr);
    }

    private Integer getStat(List<String> params) {
        var statStr = getSingleton(params);
        try {
            return Integer.parseInt(statStr);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String getSingleton(List<String> params) {
        if(params == null)
            return null;
        var statStr = params.stream().filter(x -> !StringUtils.isBlank(x)).findFirst();
        return statStr.orElse(null);
    }

    private boolean containsAnyKeywords(LotroCardBlueprint blueprint, Set<Keyword> keywords) {
        for (Keyword keyword : keywords) {
            if (blueprint.hasKeyword(keyword))
                return true;
        }
        return false;
    }

    private boolean containsAnyClasses(LotroCardBlueprint blueprint, Set<PossessionClass> itemClassFilters) {
        if (blueprint == null)
            return false;
        
        if (blueprint.getPossessionClasses() == null) {
            return itemClassFilters.contains(PossessionClass.CLASSLESS);
        }

        //disjoint is true if there's zero overlap between both collections;
        // thus, false means there is at least one shared item between both.
        return !Collections.disjoint(itemClassFilters, blueprint.getPossessionClasses());
    }

    private boolean containsAllWords(String cardData, List<String> words) {
        if(cardData == null || cardData.isEmpty())
            return false;

        cardData = cardData.toLowerCase();
        for (String word : words) {
            if (!cardData.contains(word.toLowerCase()))
                return false;
        }
        return true;
    }

    // Converts a provided list of consolidated filter parameters into their associated enums.
    private <T extends Enum> Set<T> getEnumFilter(T[] enumValues, Class<T> enumType, List<String> args, boolean startEmpty) {

        //By default we assume the passed filters are negative and pre-populate our set of valid enums with every possible
        // value.  When we detect negative filters, we will remove them from this set later.
        Set<T> result;
        if(startEmpty) {
            result = new HashSet<>();
        }
        else {
            result = new HashSet<>(Arrays.asList(enumValues));
        }
        if(args == null)
            return result;

        if(args.stream().anyMatch(x -> !x.startsWith("-"))) {
            //Some of the passed in filters are positive filters, meaning we want to by default only show the positives
            // (while leaving room for compatible negative filters to remove themselves, canceling out any incompatible filter.)
            result.clear();
        }

        for (String arg : args) {
            if (arg.startsWith("-")) { //Negative filter
                arg = arg.substring(1);

                try {
                    T t = (T) Enum.valueOf(enumType, arg);
                    result.remove(t);
                }
                //We don't care about the null case so we squelch it, but we do want IllegalArgument to be passed up
                // so it gets logged that a bad value has been passed
                catch (NullPointerException ignored) { }
            } else {
                try {
                    T t = (T) Enum.valueOf(enumType, arg);
                    result.add(t);
                }
                catch (NullPointerException ignored) { }
            }
        }

        return result;
    }

    private static boolean IsFlagAccepted(Boolean filterValue, Boolean blueprintValue) {
        if(filterValue == null)
            return true;

        if (blueprintValue == null)
            return false;

        return filterValue == blueprintValue;
    }


    /**
     * Determines if the blueprint attribute value is accepted by the filter comparison.
     * @param compareType the compare type
     * @param filterValue the filter value
     * @param blueprintValue the card blueprint value
     * @return true or false
     */
    private static boolean isAttributeValueAccepted(Integer filterValue, String compareType, Integer blueprintValue) {
        if(filterValue == null || compareType == null)
            return true;

        if (blueprintValue == null)
            return false;

        if ("EQUALS".equals(compareType) || "GREATER_THAN_OR_EQUAL_TO".equals(compareType) || "LESS_THAN_OR_EQUAL_TO".equals(compareType)) {
            if (blueprintValue.equals(filterValue))
                return true;
        }
        if ("GREATER_THAN".equals(compareType) || "GREATER_THAN_OR_EQUAL_TO".equals(compareType)) {
            if (blueprintValue > filterValue)
                return true;
        }
        if ("LESS_THAN".equals(compareType) || "LESS_THAN_OR_EQUAL_TO".equals(compareType)) {
            if (blueprintValue < filterValue)
                return true;
        }
        return false;
    }

    private static boolean isPack(String blueprintId) {
        return !blueprintId.contains("_");
    }

    private static class PacksFirstComparator implements Comparator<CardItem> {
        private final Comparator<CardItem> _cardComparator;

        private PacksFirstComparator(Comparator<CardItem> cardComparator) {
            _cardComparator = cardComparator;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            final boolean pack1 = isPack(o1.getBlueprintId());
            final boolean pack2 = isPack(o2.getBlueprintId());
            if (pack1 && pack2)
                return o1.getBlueprintId().compareTo(o2.getBlueprintId());
            else if (pack1)
                return -1;
            else if (pack2)
                return 1;
            else
                return _cardComparator.compare(o1, o2);
        }
    }

    private static class SiteNumberComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private SiteNumberComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            int siteNumberOne;
            try {
                siteNumberOne = _cardBlueprintMap.get(o1.getBlueprintId()).getSiteNumber();
            } catch (UnsupportedOperationException exp) {
                siteNumberOne = 10;
            }
            int siteNumberTwo;
            try {
                siteNumberTwo = _cardBlueprintMap.get(o2.getBlueprintId()).getSiteNumber();
            } catch (UnsupportedOperationException exp) {
                siteNumberTwo = 10;
            }

            return siteNumberOne - siteNumberTwo;
        }
    }

    private static class NameComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private NameComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return GameUtils.getFullName(_cardBlueprintMap.get(o1.getBlueprintId())).compareTo(GameUtils.getFullName(_cardBlueprintMap.get(o2.getBlueprintId())));
        }
    }

    private static class TwilightComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private TwilightComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return _cardBlueprintMap.get(o1.getBlueprintId()).getTwilightCost() - _cardBlueprintMap.get(o2.getBlueprintId()).getTwilightCost();
        }
    }

    private static class StrengthComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private StrengthComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return getStrengthSafely(_cardBlueprintMap.get(o1.getBlueprintId())) - getStrengthSafely(_cardBlueprintMap.get(o2.getBlueprintId()));
        }

        private int getStrengthSafely(LotroCardBlueprint blueprint) {
            try {
                return blueprint.getStrength();
            } catch (UnsupportedOperationException exp) {
                return Integer.MAX_VALUE;
            }
        }
    }

    private static class VitalityComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private VitalityComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            return getVitalitySafely(_cardBlueprintMap.get(o1.getBlueprintId())) - getVitalitySafely(_cardBlueprintMap.get(o2.getBlueprintId()));
        }

        private int getVitalitySafely(LotroCardBlueprint blueprint) {
            try {
                return blueprint.getVitality();
            } catch (UnsupportedOperationException exp) {
                return Integer.MAX_VALUE;
            }
        }
    }

    private static class CardTypeComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private CardTypeComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            CardType cardType1 = _cardBlueprintMap.get(o1.getBlueprintId()).getCardType();
            CardType cardType2 = _cardBlueprintMap.get(o2.getBlueprintId()).getCardType();
            return cardType1.ordinal() - cardType2.ordinal();
        }
    }

    private static class CultureComparator implements Comparator<CardItem> {
        private final Map<String, LotroCardBlueprint> _cardBlueprintMap;

        private CultureComparator(Map<String, LotroCardBlueprint> cardBlueprintMap) {
            _cardBlueprintMap = cardBlueprintMap;
        }

        @Override
        public int compare(CardItem o1, CardItem o2) {
            Culture culture1 = _cardBlueprintMap.get(o1.getBlueprintId()).getCulture();
            Culture culture2 = _cardBlueprintMap.get(o2.getBlueprintId()).getCulture();

            return getOrdinal(culture1) - getOrdinal(culture2);
        }

        private int getOrdinal(Culture culture) {
            if (culture == null)
                return -1;
            return culture.ordinal();
        }
    }
}
