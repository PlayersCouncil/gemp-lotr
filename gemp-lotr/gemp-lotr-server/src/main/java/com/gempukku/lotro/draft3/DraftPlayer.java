package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Objects;

public class DraftPlayer {
    protected final TableDraft table;
    private final String name;

    public DraftPlayer(TableDraft table, String name) {
        this.table = table;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getCardsToPickFrom() {
        return table.getCardsToPickFrom(this);
    }

    public int getSecondsRemainingForPick() throws IllegalStateException {
        return table.getSecondsRemainingForPick();
    }

    public void chooseCard(String cardId) {
        table.chooseCard(this, cardId);
    }

    public CardCollection getCollection() {
        return table.getPickedCards(this);
    }

    public String getChosenCard() {
        return table.getChosenCard(this);
    }

    public boolean draftFinished() {
        return table.isFinished();
    }

    public Document getDocument(LotroCardBlueprintLibrary cardLibrary, LotroFormatLibrary formatLibrary) throws ParserConfigurationException {
        return table.getDocument(this, cardLibrary, formatLibrary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DraftPlayer that = (DraftPlayer) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
