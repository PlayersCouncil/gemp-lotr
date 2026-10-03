package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Where things are printed on the face of a card, so that a scenario can point at "the strength" of a card it is
 * showing large without its author measuring anything.
 *
 * A region is {left, top, width, height} in percent of the card image.  The figures were read off Decipher's card
 * images for sets 1 to 10, which share three layouts: characters (companions, allies, minions), sites, and
 * everything else.  Later sets moved some things, sites most of all; for anything these do not fit, a scenario
 * gives the rectangle itself, as rect(left, top, width, height).
 */
public final class CardFaces {
    private CardFaces() { }

    public enum Frame { CHARACTER, ITEM, SITE }

    private static final Map<Frame, Map<String, double[]>> REGIONS = new LinkedHashMap<>();

    private static void region(Frame frame, String name, double left, double top, double width, double height) {
        REGIONS.computeIfAbsent(frame, key -> new LinkedHashMap<>()).put(name.toLowerCase(Locale.ROOT), new double[] {left, top, width, height});
    }

    static {
        region(Frame.CHARACTER, "cost", 4, 3, 15, 11);
        region(Frame.CHARACTER, "title", 19, 4, 53, 10);
        region(Frame.CHARACTER, "culture", 70, 3, 26, 15);
        region(Frame.CHARACTER, "art", 10, 13.5, 80, 44);
        region(Frame.CHARACTER, "type", 11, 57.5, 79, 6.5);
        region(Frame.CHARACTER, "strength", 5.5, 62, 15, 11);
        region(Frame.CHARACTER, "vitality", 5.5, 74, 15, 11);
        //The third symbol down: a companion's signet, a Ring-bearer's resistance, a minion's home site
        region(Frame.CHARACTER, "signet", 5.5, 84.5, 15, 11);
        region(Frame.CHARACTER, "resistance", 5.5, 84.5, 15, 11);
        region(Frame.CHARACTER, "siteNumber", 5.5, 84.5, 15, 11);
        region(Frame.CHARACTER, "stats", 5.5, 62, 15, 33.5);
        region(Frame.CHARACTER, "text", 20.5, 64, 74, 29.5);
        region(Frame.CHARACTER, "collector", 83, 91.5, 12, 3.5);

        region(Frame.ITEM, "cost", 4, 3, 15, 11);
        region(Frame.ITEM, "title", 6, 13, 11, 46);
        region(Frame.ITEM, "culture", 72, 3, 24, 13);
        region(Frame.ITEM, "art", 20, 10.5, 74, 40);
        region(Frame.ITEM, "type", 20, 57.5, 75, 6.5);
        //A possession or artifact that changes its bearer's strength or vitality shows by how much
        region(Frame.ITEM, "strength", 5.5, 62, 15, 12);
        region(Frame.ITEM, "vitality", 5.5, 74, 15, 11);
        region(Frame.ITEM, "text", 20.5, 64, 74, 29.5);
        region(Frame.ITEM, "collector", 83, 91.5, 12, 3.5);

        region(Frame.SITE, "siteNumber", 4, 7, 10, 13);
        region(Frame.SITE, "shadowNumber", 86, 6, 10, 14);
        region(Frame.SITE, "title", 30, 6, 40, 8);
        region(Frame.SITE, "art", 11, 13, 78, 60);
        region(Frame.SITE, "text", 11, 75, 78, 14);
        region(Frame.SITE, "arrow", 37, 88.5, 26, 5);
    }

    public static Frame frameOf(CardType type) {
        if (type == CardType.SITE)
            return Frame.SITE;
        if (type == CardType.COMPANION || type == CardType.ALLY || type == CardType.MINION)
            return Frame.CHARACTER;
        return Frame.ITEM;
    }

    /**
     * @return The region with that name on that kind of card; null if that kind of card has no such thing.
     */
    public static double[] region(Frame frame, String name) {
        return REGIONS.get(frame).get(name.toLowerCase(Locale.ROOT));
    }

    /** Whether any kind of card has a region with that name. */
    public static boolean known(String name) {
        for (Map<String, double[]> regions : REGIONS.values())
            if (regions.containsKey(name.toLowerCase(Locale.ROOT)))
                return true;
        return false;
    }

    public static Set<String> names() {
        Set<String> names = new TreeSet<>();
        for (Map<String, double[]> regions : REGIONS.values())
            names.addAll(regions.keySet());
        return names;
    }

    public static Set<String> names(Frame frame) {
        return new TreeSet<>(REGIONS.get(frame).keySet());
    }

    /**
     * @return The blueprint of the card a zoom shows; null if it names a site the scenario's path does not have.
     */
    public static String blueprintOf(ScenarioDefinition def, ScenarioDefinition.Zoom zoom) {
        if (zoom.card() != null)
            return def.cards.get(zoom.card()).blueprintId();
        if (zoom.site() != null) {
            List<String> sites = def.sites.get(def.human);
            return sites == null || zoom.site() > sites.size() ? null : sites.get(zoom.site() - 1);
        }
        return zoom.blueprint();
    }

    /**
     * @return What is wrong with a zoom, given what its card turns out to be; null if nothing is.
     */
    public static String check(ScenarioDefinition def, ScenarioDefinition.Zoom zoom, LotroCardBlueprintLibrary library) {
        //(a picture is whatever is in the file; its parts are rectangles, which the parser has seen to)
        if (zoom.image() != null)
            return null;
        String blueprintId = blueprintOf(def, zoom);
        if (blueprintId == null)
            return "the scenario's path has no site " + zoom.site();
        LotroCardBlueprint blueprint;
        try {
            blueprint = library.getLotroCardBlueprint(blueprintId);
        } catch (CardNotFoundException exp) {
            return "there is no card " + blueprintId;
        }
        Frame frame = frameOf(blueprint.getCardType());
        for (ScenarioDefinition.Focus focus : zoom.focus())
            if (focus.name() != null && !prints(blueprint, frame, focus.name()))
                return "'" + focus.name() + "' is not something printed on " + blueprint.getTitle() + ".  A card like that has: "
                        + String.join(", ", names(frame)) + ".  Anything else can be given as rect(left, top, width, height)";
        return null;
    }

    /**
     * @return Where a part is on one of the scenario's cards; null if that card has no such part (or does not exist).
     */
    public static double[] region(ScenarioDefinition def, String alias, String part, LotroCardBlueprintLibrary library) {
        try {
            double[] rect = region(frameOf(library.getLotroCardBlueprint(def.cards.get(alias).blueprintId()).getCardType()), part);
            return rect == null ? null : rect.clone();
        } catch (CardNotFoundException exp) {
            return null;
        }
    }

    /**
     * @return What is wrong with pointing at a part of one of the scenario's cards; null if nothing is.
     */
    public static String checkPart(ScenarioDefinition def, String alias, String part, LotroCardBlueprintLibrary library) {
        LotroCardBlueprint blueprint;
        try {
            blueprint = library.getLotroCardBlueprint(def.cards.get(alias).blueprintId());
        } catch (CardNotFoundException exp) {
            return null;    //reported where the card is declared
        }
        Frame frame = frameOf(blueprint.getCardType());
        if (!prints(blueprint, frame, part))
            return "'" + part + "' is not something printed on " + blueprint.getTitle() + ".  A card like that has: "
                    + String.join(", ", names(frame));
        return null;
    }

    private static boolean prints(LotroCardBlueprint blueprint, Frame frame, String part) {
        if (region(frame, part) == null)
            return false;
        //Only an item that changes its bearer's strength or vitality shows a number there
        if (frame == Frame.ITEM && part.equalsIgnoreCase("strength"))
            return blueprint.getStrength() != 0;
        if (frame == Frame.ITEM && part.equalsIgnoreCase("vitality"))
            return blueprint.getVitality() != 0;
        return true;
    }

    /** Where the web server has the pictures a scenario can show that are not cards. */
    public static final String IMAGES_URL = "/gemp-lotr/images/scenarios/";

    /**
     * @return What a client needs to show the card and pick out the parts of it: {blueprint, image, magnify,
     * focus: [{name, rect: [left, top, width, height]}]}.  One of blueprint and image is null.
     */
    public static Map<String, Object> describe(ScenarioDefinition def, ScenarioDefinition.Zoom zoom, LotroCardBlueprintLibrary library) {
        String blueprintId = zoom.image() != null ? null : blueprintOf(def, zoom);
        Frame frame = Frame.ITEM;
        try {
            if (blueprintId != null)
                frame = frameOf(library.getLotroCardBlueprint(blueprintId).getCardType());
        } catch (CardNotFoundException exp) {
            //The validator has already said so; the card is still shown, with whatever rectangles were given
        }
        List<Map<String, Object>> regions = new ArrayList<>();
        for (ScenarioDefinition.Focus focus : zoom.focus()) {
            double[] rect = focus.rect() != null ? focus.rect() : region(frame, focus.name());
            if (rect == null)
                continue;
            Map<String, Object> region = new LinkedHashMap<>();
            region.put("name", focus.name());
            region.put("rect", new double[] {rect[0], rect[1], rect[2], rect[3]});
            regions.add(region);
        }
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("blueprint", blueprintId);
        json.put("image", zoom.image() == null ? null : IMAGES_URL + zoom.image().replace(" ", "%20"));
        json.put("magnify", zoom.magnify());
        json.put("focus", regions);
        return json;
    }
}
