package com.gempukku.lotro.game;

import com.gempukku.lotro.common.JSONDefs;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.List;
import java.util.Map;

public interface LotroFormat {
    String PCSummary = """
        As a reminder, PC formats incorporate the following changes:
         <br/>- <a href="https://wiki.lotrtcgpc.net/wiki/PC_Errata" target="_blank">PC Errata are in effect</a>
         <br/>- Set V1 is legal
         <br/>- Discard piles are public information for both sides
         <br/>- The game ends after Regroup actions are made (instead of at the start of Regroup)
        """;

    boolean isOrderedSites();

    boolean canCancelRingBearerSkirmish();

    boolean hasRuleOfFour();
    boolean usesMaps();

    boolean hasMulliganRule();

    boolean winWhenShadowReconciles();

    boolean discardPileIsPublic();

    boolean winOnControlling5Sites();

    boolean isPlaytest();
    boolean hallVisible();

    String getName();

    String getCode();
    int getOrder();

    String validateCard(String cardId);

    List<String> validateDeck(LotroDeck deck);
    String validateDeckForHall(LotroDeck deck);

    LotroDeck applyErrata(LotroDeck deck);

    List<Integer> getValidSets();

    List<String> getBannedCards();

    List<String> getRestrictedCards();

    List<String> getValidCards();

    List<String> getLimit2Cards();

    List<String> getLimit3Cards();

    List<String> getRestrictedCardNames();

    Map<String,String> getErrataCardMap();

    String applyErrata(String bpID);

    List<String> findBaseCards(String bpID);

    SitesBlock getSiteBlock();

    String getSurveyUrl();

    int getHandSize();

    Adventure getAdventure();
    JSONDefs.Format Serialize();
}
