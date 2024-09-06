package com.gempukku.lotro.collection;

import com.gempukku.lotro.common.Names;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DeckRenderer {

    private final LotroCardBlueprintLibrary _bpLibrary;
    private final LotroFormatLibrary _formatLibrary;
    private final SortAndFilterCards _cardFilter;

    public DeckRenderer(LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary, SortAndFilterCards cardfilter) {
        _bpLibrary = library;
        _cardFilter = cardfilter;
        _formatLibrary = formatLibrary;
    }


    public List<LotroDeck> ExtractDecksFromReplaySummary(ReplayMetadata summary) {
        var decks = new ArrayList<LotroDeck>();
        for(var deck : summary.Decks.values()) {
            decks.add(ConvertMetadataDeckToLotroDeck(deck));
        }
        return decks;
    }

    public LotroDeck ConvertMetadataDeckToLotroDeck(ReplayMetadata.DeckMetadata recordedDeck) {
        var deck = new LotroDeck(recordedDeck.DeckName);
        deck.setNotes(recordedDeck.Owner);
        deck.setTargetFormat(recordedDeck.TargetFormat);
        deck.setRing(recordedDeck.Ring);
        deck.setRingBearer(recordedDeck.RingBearer);
        for(var site : recordedDeck.AdventureDeck) {
            deck.addSite(site);
        }
        for(var card : recordedDeck.DrawDeck) {
            deck.addCard(card);
        }
        return deck;
    }

    public String AddDeckReadoutHeaderAndFooter(String fragment) {
        return AddDeckReadoutHeaderAndFooter(Collections.singletonList(fragment));
    }

    public String AddDeckReadoutHeaderAndFooter(List<String> fragments) {
        String preamble = """
<html>
    <style>
        body {
            margin:50;
        }
        
        .tooltip {
          border-bottom: 1px dotted black; /* If you want dots under the hoverable text */
          color:#0000FF;
        }
        
        .tooltip span, .tooltip title {
            display:none;
        }
        .tooltip:hover span:not(.click-disabled),.tooltip:active span:not(.click-disabled) {
            display:block;
            position:fixed;
            overflow:hidden;
            background-color: #FAEBD7;
            width:auto;
            z-index:9999;
            top:20%;
            left:350px;
        }
        /* This prevents tooltip images from automatically shrinking if they are near the window edge.*/
        .tooltip span > img {
            max-width:none !important;
            overflow:hidden;
        }
                        
    </style>
    <body>""";
        String divider = "<br/>[hr]<hr><br/>";
        String postamble = "</body></html>";
        return preamble + String.join(divider, fragments) + postamble;

    }

    public String convertDeckToHTMLFragment(LotroDeck deck, String author) throws CardNotFoundException {
        return convertDeckToHTMLFragment(deck, author, new ArrayList<>());
    }

    public String convertDeckToHTMLFragment(LotroDeck deck, String author, List<Map.Entry<String, String>> replays) throws CardNotFoundException {

        if (deck == null)
            return null;

        StringBuilder result = new StringBuilder();
        result.append("<div>")
                .append("<h1>").append(StringEscapeUtils.escapeHtml3(deck.getDeckName())).append("</h1>")
                .append("<h2>Format: ").append(StringEscapeUtils.escapeHtml3(deck.getTargetFormat())).append("</h2>");

        if(author != null) {
            result.append("<h2>Author: ").append(StringEscapeUtils.escapeHtml3(author)).append("</h2>");
        }

        if(replays != null && replays.size() > 0) {
            result.append("<h3>Round Replays: </h3>");
            List<String> rounds = new ArrayList<>();

            for(var replay : replays) {
                if(replay.getValue().isBlank()) {
                    rounds.add(replay.getKey());
                }
                else {
                    rounds.add("<a href='" + replay.getValue() + "' target='_blank'>" + replay.getKey() + "</a>");
                }
            }

            result.append(String.join(" • ", rounds));
        }

        String ringBearer = deck.getRingBearer();
        if (ringBearer != null) {
            result.append("<b>Ring-bearer:</b> ").append(generateCardTooltip(ringBearer)).append("<br/>");
        }
        String ring = deck.getRing();
        if (ring != null) {
            result.append("<b>Ring:</b> ").append(generateCardTooltip(ring)).append("<br/>");
        }

        DefaultCardCollection deckCards = new DefaultCardCollection();
        for (String card : deck.getDrawDeckCards()) {
            deckCards.addItem(_bpLibrary.getBaseBlueprintId(card), 1);
        }
        for (String site : deck.getSites()) {
            deckCards.addItem(_bpLibrary.getBaseBlueprintId(site), 1);
        }

        result.append("<br/>");
        result.append("<b>Adventure deck:</b><br/>");
        for (CardCollection.Item item : _cardFilter.process("cardType:SITE sort:siteNumber,twilight", deckCards.getAll(),
                _bpLibrary, _formatLibrary)) {
            result.append(generateCardTooltip(item)).append("<br/>");
        }

        result.append("<br/>");
        result.append("<b>Free Peoples Draw Deck:</b><br/>");
        for (CardCollection.Item item : _cardFilter.process("side:FREE_PEOPLE sort:cardType,culture,name", deckCards.getAll(),
                _bpLibrary, _formatLibrary)) {
            result.append(item.getCount()).append("x ").append(generateCardTooltip(item)).append("<br/>");
        }

        result.append("<br/>");
        result.append("<b>Shadow Draw Deck:</b><br/>");
        for (CardCollection.Item item : _cardFilter.process("side:SHADOW sort:cardType,culture,name", deckCards.getAll(),
                _bpLibrary, _formatLibrary)) {
            result.append(item.getCount()).append("x ").append(generateCardTooltip(item)).append("<br/>");
        }

        result.append("<h3>Notes</h3><br>").append(deck.getNotes().replace("\n", "<br/>"));

        result.append("</div>");

        return result.toString();
    }

    public String convertDeckToForumFragment(LotroDeck deck, String author) throws CardNotFoundException {
        return convertDeckToForumFragment(deck, author, new ArrayList<>());
    }

    public String convertDeckToForumFragment(LotroDeck deck, String author, List<Map.Entry<String, String>> replays) throws CardNotFoundException {

        if (deck == null)
            return null;

        StringBuilder result = new StringBuilder();
        result.append("<div>");
                //.append("<h1>").append(StringEscapeUtils.escapeHtml3(deck.getDeckName())).append("</h1>")
                //.append("<h2>Format: ").append(StringEscapeUtils.escapeHtml3(deck.getTargetFormat())).append("</h2>");

        if(author != null) {
            result.append("<h2>Author: ").append(StringEscapeUtils.escapeHtml3(author)).append("</h2>");
        }

        if(replays != null && replays.size() > 0) {
            result.append("<h3>[b]Round Replays: [/b]</h3>");
            List<String> rounds = new ArrayList<>();

            for(var replay : replays) {
                if(replay.getValue().isBlank()) {
                    rounds.add(replay.getKey());
                }
                else {
                    rounds.add("[url=" + replay.getValue() + "]" + replay.getKey() + "[/url]");
                }
            }

            result.append(String.join(" • ", rounds));
        }

        result.append("<br/><br/>");

        String ringBearer = deck.getRingBearer();
        if (ringBearer != null) {
            result.append("<b>[b]Ring-bearer:[/b]</b> ").append(generateCardTooltip(ringBearer)).append("<br/>");
        }
        String ring = deck.getRing();
        if (ring != null) {
            result.append("<b>[b]Ring:[/b]</b> ").append(generateCardTooltip(ring)).append("<br/>");
        }

        DefaultCardCollection deckCards = new DefaultCardCollection();
        for (String card : deck.getDrawDeckCards()) {
            deckCards.addItem(_bpLibrary.getBaseBlueprintId(card), 1);
        }
        for (String site : deck.getSites()) {
            deckCards.addItem(_bpLibrary.getBaseBlueprintId(site), 1);
        }

        result.append("<br/>");
        result.append("<b>[b]Adventure deck:[/b]</b><br/>");
        for (CardCollection.Item item : _cardFilter.process("cardType:SITE sort:siteNumber,twilight", deckCards.getAll(),
                _bpLibrary, _formatLibrary)) {
            result.append(generateCardTooltip(item)).append("<br/>");
        }

        result.append("<br/>");
        result.append("<b>[b]Free Peoples Draw Deck:[/b]</b><br/>");
        for (CardCollection.Item item : _cardFilter.process("side:FREE_PEOPLE sort:cardType,culture,name", deckCards.getAll(),
                _bpLibrary, _formatLibrary)) {
            result.append(item.getCount()).append("x ").append(generateCardTooltip(item)).append("<br/>");
        }

        result.append("<br/>");
        result.append("<b>[b]Shadow Draw Deck:[/b]</b><br/>");
        for (CardCollection.Item item : _cardFilter.process("side:SHADOW sort:cardType,culture,name", deckCards.getAll(),
                _bpLibrary, _formatLibrary)) {
            result.append(item.getCount()).append("x ").append(generateCardTooltip(item)).append("<br/>");
        }

        result.append("</div>");

        return result.toString();
    }

    private String generateCardTooltip(CardCollection.Item item) throws CardNotFoundException {
        return generateCardTooltip(_bpLibrary.getLotroCardBlueprint(item.getBlueprintId()), item.getBlueprintId());
    }

    private String generateCardTooltip(String bpid) throws CardNotFoundException {
        return generateCardTooltip(_bpLibrary.getLotroCardBlueprint(bpid), bpid);
    }

    private String generateCardTooltip(LotroCardBlueprint bp, String bpid) throws CardNotFoundException {
        String[] parts = bpid.split("_");
        int setnum = Integer.parseInt(parts[0]);
        String set = String.format("%02d", setnum);
        String subset = "S";
        int version = 0;
        if(setnum >= 50 && setnum <= 69) {
            setnum -= 50;
            set = String.format("%02d", setnum);
            subset = "E";
            version = 1;
        }
        else if(setnum >= 70 && setnum <= 89) {
            setnum -= 70;
            set = String.format("%02d", setnum);
            subset = "E";
            version = 1;
        }
        else if(setnum >= 100 && setnum <= 149) {
            setnum -= 100;
            set = "V" + setnum;
        }
        int cardnum = Integer.parseInt(parts[1].replace("*", "").replace("T", ""));

        String id = "LOTR-EN" + set + subset + String.format("%03d", cardnum) + "." + String.format("%01d", version);
        String displayName = Names.SanitizeDisplayName(GameUtils.getFullName(bp));
        if(subset.equals("E")) {
            displayName += " (Errata)";
        }
        String result = "<span class=\"tooltip\">" + displayName
                + "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>";

        return result;
    }
}
