package com.gempukku.lotro.draft3;

import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public interface TableDraft {
    DraftPlayer registerPlayer(String name);
    DraftPlayer getPlayer(String name);
    boolean removePlayer(String name);
    void advanceDraft();
    List<String> getCardsToPickFrom(DraftPlayer draftPlayer);
    String getChosenCard(DraftPlayer draftPlayer);
    void chooseCard(DraftPlayer who, String what);
    boolean isFinished();
    int getSecondsRemainingForPick() throws IllegalStateException;
    CardCollection getPickedCards(DraftPlayer draftPlayer);
    Document getDocument(DraftPlayer draftPlayer, LotroCardBlueprintLibrary cardLibrary, LotroFormatLibrary formatLibrary) throws ParserConfigurationException;
}
