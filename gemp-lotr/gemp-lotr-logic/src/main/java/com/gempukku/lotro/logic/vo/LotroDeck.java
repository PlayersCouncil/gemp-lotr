package com.gempukku.lotro.logic.vo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class LotroDeck {
    private String _ringBearer;
    private String _ring;
    private String _map;
    private final List<String> _siteCards = new ArrayList<>();
    private final List<String> _nonSiteCards = new ArrayList<>();
    private final String _deckName;
    private String _notes;

    private String _targetFormat = "Anything Goes";

    public LotroDeck(String deckName) {
        _deckName = deckName;
    }

    public String getDeckName() {
        return _deckName;
    }

    public void setRingBearer(String ringBearer) {
        _ringBearer = ringBearer;
    }

    public void setRing(String ring) {
        _ring = ring;
    }
    public void setMap(String map) { _map = map; }

    public void addCard(String card) {
        _nonSiteCards.add(card);
    }

    public void addSite(String card) {
        _siteCards.add(card);
    }

    public List<String> getDrawDeckCards() {
        return Collections.unmodifiableList(_nonSiteCards);
    }

    public List<String> getSites() {
        return Collections.unmodifiableList(_siteCards);
    }

    public String getRingBearer() {
        return _ringBearer;
    }

    public String getRing() {
        return _ring;
    }

    public String getMap() {
        return _map;
    }

    public String getTargetFormat() { return _targetFormat; }
    public void setTargetFormat(String value) { _targetFormat = value; }

    public String getNotes() {
        return _notes;
    }

    public void setNotes(String value) {
        _notes = value;
    }

    public String getURL(String owner) {
        return GenerateDeckSharingURL(_deckName, owner);
    }
    public static String GenerateDeckSharingURL(String deckName, String owner) {
        var url = "/share/deck?id=";

        String code = owner + "|" + deckName;

        String base64 = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        String result = URLEncoder.encode(base64, StandardCharsets.UTF_8);

        return url + result;
    }
}
