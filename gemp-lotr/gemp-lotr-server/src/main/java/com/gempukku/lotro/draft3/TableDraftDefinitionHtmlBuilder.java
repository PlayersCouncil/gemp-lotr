package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.logic.GameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.StreamSupport;

public class TableDraftDefinitionHtmlBuilder {
    public static String buildHtml(JSONObject definition, LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary) {
        StringBuilder builder = new StringBuilder();

        builder.append(getHeadSection());

        builder.append(getDraftInfo(definition));
        builder.append(getBoostersDrafted(definition));
        builder.append(getStartingCollection(definition, library));
        builder.append(getCardSets(definition, library, formatLibrary));

        builder.append(getClosingSection());

        return builder.toString();
    }

    private static String getCardSets(JSONObject definition, LotroCardBlueprintLibrary library, LotroFormatLibrary formatLibrary) {
        SortAndFilterCards sortAndFilterCards = new SortAndFilterCards();

        StringBuilder builder = new StringBuilder();

        JSONArray cardSets = (JSONArray) definition.get("card-sets");
        JSONArray pools = (JSONArray) definition.get("pools");

        builder.append("<h2>Card Sets:</h2>");
        for (JSONObject cardSet : (Iterable<JSONObject>) cardSets) {
            String name = (String) cardSet.get("set-name");
            builder.append("<b>").append(name).append(":</b><br/>");
            if (cardSet.containsKey("card-set")) {
                List<String> cardList = (List<String>) cardSet.get("card-set");

                DefaultCardCollection setCards = new DefaultCardCollection();
                for (String card : cardList) {
                    setCards.addItem(library.getBaseBlueprintId(card), 1);
                }
                boolean printWithCount = StreamSupport.stream(setCards.getAll().spliterator(), false).anyMatch(item -> item.getCount() > 1);
                for (CardCollection.Item item : sortAndFilterCards.process("sort:side,cardType,culture,name", setCards.getAll(), library, formatLibrary)) {
                    if (printWithCount) {
                        builder.append(item.getCount() + "x ");
                    }
                    builder.append(generateCardTooltip(item.getBlueprintId(), library)).append("<br/>");
                }
            } else if (cardSet.containsKey("set-set") && cardSet.containsKey("choose")) {
                List<String> setNames = (List<String>) cardSet.get("set-set");
                int howMany = ((Number) cardSet.get("choose")).intValue();
                builder.append("<b>").append(howMany).append(" out of:</b><br/>");
                setNames.forEach(setName -> {
                    builder.append(setName).append("<br/>");
                });
            }
            builder.append("<br/>");
        }

        if (pools != null) {
            for (JSONObject cardSet : (Iterable<JSONObject>) pools) {
                String name = (String) cardSet.get("pool-name");
                builder.append("<b>").append(name).append(":</b><br/>");
                List<String> sets = (List<String>) cardSet.get("set-set");
                for (String setName : sets) {
                    builder.append(setName).append("<br/>");
                }
            }
        }

        builder.delete(builder.length() - 5, builder.length());
        return builder.toString();
    }

    private static String getStartingCollection(JSONObject definition, LotroCardBlueprintLibrary library) {
        StringBuilder builder = new StringBuilder();

        JSONArray startingCollection = (JSONArray) definition.get("starting-collection");

        builder.append("<h2>Starting Collection:</h2>");
        for (JSONObject wheel : (Iterable<JSONObject>) startingCollection) {
            int size = ((Number) wheel.get("size")).intValue();
            List<String> cardList = (List<String>) wheel.get("card-list");

            if (size == cardList. size()) {
                builder.append("<b>All of Those:</b><br/>");
            } else {
                builder.append("<b>").append(size).append(" Card");
                if (size > 1) {
                    builder.append("s");
                }
                builder.append(" from this Wheel of ").append(cardList.size()).append(":</b><br/>");
            }
            cardList.forEach(card -> {
                builder.append(generateCardTooltip(card, library)).append("<br/>");
            });
            builder.append("<br/>");
        }

        builder.delete(builder.length() - 5, builder.length());
        return builder.toString();
    }

    private static String getBoostersDrafted(JSONObject definition) {
        StringBuilder builder = new StringBuilder();

        JSONArray boosters = (JSONArray) definition.get("boosters");

        builder.append("<h2>Boosters Drafted:</h2>");
        for (JSONObject booster : (Iterable<JSONObject>) boosters) {
            builder.append("<b>Rounds:</b> ");
            JSONArray rounds = (JSONArray) booster.get("rounds");
            for (Object round : rounds) {
                int roundNumber  = ((Number) round).intValue();
                builder.append(roundNumber).append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append("<br/>");


            builder.append("<b>Cards in Booster:</b><br/>");
            JSONArray cards = (JSONArray) booster.get("cards");
            for (JSONObject countSetPair : (Iterable<JSONObject>) cards) {
                String name = (String) countSetPair.get("set-name");
                int count = ((Number) countSetPair.get("count")).intValue();
                builder.append(count).append("x ").append(name).append("<br/>");
            }
            builder.append("<br/>");
        }

        builder.delete(builder.length() - 5, builder.length());
        return builder.toString();
    }

    private static String getDraftInfo(JSONObject definition) {
        StringBuilder builder = new StringBuilder();

        String name = (String) definition.get("name");
        String timer = (String) definition.get("timer");
        int maxPlayers = ((Number) definition.get("max-players")).intValue();
        String htmlDescription = (String) definition.get("html-description");

        builder.append("<h1>").append(StringEscapeUtils.escapeHtml3(name)).append("</h1>");
        if (htmlDescription != null) {
            builder.append(htmlDescription);
        }
        builder.append("<h2>Max Players: ").append(maxPlayers).append("</h2>");
        if (timer != null) {
            builder.append("<h2>Recommended Timer: ").append(StringEscapeUtils.escapeHtml3(timer)).append("</h2>");
        }

        return builder.toString();
    }

    private static String getClosingSection() {
        return "</body></html>";
    }

    private static String getHeadSection() {
        return """
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
    }

    private static String generateCardTooltip(String id, LotroCardBlueprintLibrary library) {
        try {
            return generateCardTooltip(library.getLotroCardBlueprint(id), id);
        } catch (CardNotFoundException e) {
            return id;
        }
    }

    private static String generateCardTooltip(LotroCardBlueprint bp, String bpid) {
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

        String result;

        if (setnum >= 30 && setnum <= 33) {
            String id;
            if (bpid.length() == 4) {
                id = bpid.replace("_", "00");
            } else {
                id = bpid.replace("_", "0");
            }
            result = "<span class=\"tooltip\">" + GameUtils.getFullName(bp)
                    + "<span><img class=\"ttimage\" src=\"https://i.lotrtcgpc.net/hobbit/HDG" + id + ".jpg\" ></span></span>";

        } else {
            String id = "LOTR-EN" + set + subset + String.format("%03d", cardnum) + "." + String.format("%01d", version);
            result = "<span class=\"tooltip\">" + GameUtils.getFullName(bp)
                    + "<span><img class=\"ttimage\" src=\"https://wiki.lotrtcgpc.net/images/" + id + "_card.jpg\" ></span></span>";
        }

        return result;
    }
}
