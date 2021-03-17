package com.gempukku.lotro.cards;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.Assignment;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.vo.LotroDeck;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class GenericCardTestHelper extends AbstractAtTest {

    private int LastCardID = 100;

    public static final HashMap<String, String> FellowshipSites = new HashMap<String, String>() {{
        put("site1", "1_319");
        put("site2", "1_327");
        put("site3", "1_337");
        put("site4", "1_343");
        put("site5", "1_349");
        put("site6", "1_350");
        put("site7", "1_353");
        put("site8", "1_356");
        put("site9", "1_360");
    }};

    public static final String FOTRFrodo = "1_290";
    public static final String FOTRRing = "1_2";


    // Player key, then name/card
    public Map<String, Map<String, PhysicalCardImpl>> Cards = new HashMap<>();

    public GenericCardTestHelper(Map<String, String> cardIDs) throws CardNotFoundException, DecisionResultInvalidException {
        this(cardIDs, null, null, null);
    }

    public GenericCardTestHelper(Map<String, String> cardIDs, Map<String, String> siteIDs, String ringBearerID, String ringID) throws CardNotFoundException, DecisionResultInvalidException {
        super();

        if(siteIDs == null || ringBearerID == null || ringID == null) {
            initializeSimplestGame();
        }
        else {
            Map<String, LotroDeck> decks = new HashMap<String, LotroDeck>();
            decks.put(P1, new LotroDeck(P1));
            decks.put(P2, new LotroDeck(P2));

            for(String name : siteIDs.keySet()) {
                String id = siteIDs.get(name);
                decks.get(P1).addSite(id);
                decks.get(P2).addSite(id);
            }

            decks.get(P1).setRingBearer(ringBearerID);
            decks.get(P2).setRingBearer(ringBearerID);

            decks.get(P1).setRing(ringID);
            decks.get(P2).setRing(ringID);

            initializeGameWithDecks(decks);
        }

        Cards.put(P1, new HashMap<>());
        Cards.put(P2, new HashMap<>());

        if(cardIDs != null) {
            for(String name : cardIDs.keySet()) {
                String id = cardIDs.get(name);
                PhysicalCardImpl card = CreateCard(P1, id);
                Cards.get(P1).put(name, card);
                FreepsMoveCardToDeck(card);

                card = CreateCard(P2, id);
                Cards.get(P2).put(name, card);
                FreepsMoveCardToDeck(card);
            }
        }
    }

    public PhysicalCardImpl CreateCard(String playerID, String cardID) throws CardNotFoundException {
        return new PhysicalCardImpl(LastCardID++, cardID, playerID, _library.getLotroCardBlueprint(cardID));
    }

    public void StartGame() throws DecisionResultInvalidException {
        skipMulligans();
    }

    public PhysicalCardImpl GetFreepsCard(String cardName) { return Cards.get(P1).get(cardName); }
    public PhysicalCardImpl GetShadowCard(String cardName) { return Cards.get(P2).get(cardName); }

    public PhysicalCardImpl GetFreepsSite(int siteNum) { return GetSite(P1, siteNum); }
    public PhysicalCardImpl GetShadowSite(int siteNum) { return GetSite(P2, siteNum); }
    public PhysicalCardImpl GetSite(String playerID, int siteNum)
    {
        List<PhysicalCardImpl> advDeck = (List<PhysicalCardImpl>)_game.getGameState().getAdventureDeck(playerID);
//        for (PhysicalCardImpl card : advDeck) {
//            if (card.getSiteNumber() == siteNum){
//                return card;
//            }
//        }
        return advDeck.stream().filter(x -> x.getBlueprint().getSiteNumber() == siteNum).findFirst().get();
    }

    public List<String> FreepsGetAvailableActions() { return GetAvailableActions(P1); }
    public List<String> ShadowGetAvailableActions() { return GetAvailableActions(P2); }
    public List<String> GetAvailableActions(String playerID) {
        AwaitingDecision decision = GetAwaitingDecision(playerID);
        if(decision == null) {
            return new ArrayList<String>();
        }
        return Arrays.asList((String[])decision.getDecisionParameters().get("actionText"));
    }

    public AwaitingDecision FreepsGetAwaitingDecision() { return GetAwaitingDecision(P1); }
    public AwaitingDecision ShadowGetAwaitingDecision() { return GetAwaitingDecision(P2); }
    public AwaitingDecision GetAwaitingDecision(String playerID) { return _userFeedback.getAwaitingDecision(playerID); }

    public Boolean FreepsDecisionAvailable(String text) { return DecisionAvailable(P1, text); }
    public Boolean ShadowDecisionAvailable(String text) { return DecisionAvailable(P2, text); }
    public Boolean DecisionAvailable(String playerID, String text)
    {
        AwaitingDecision ad = GetAwaitingDecision(playerID);
        String lowerText = text.toLowerCase();
        return ad.getText().toLowerCase().contains(lowerText);
    }

    public Boolean FreepsActionAvailable(String action) { return ActionAvailable(P1, action); }
    public Boolean FreepsCardActionAvailable(PhysicalCardImpl card) { return ActionAvailable(P1, "Use " + GameUtils.getFullName(card)); }
    public Boolean FreepsCardPlayAvailable(PhysicalCardImpl card) { return ActionAvailable(P1, "Play " + GameUtils.getFullName(card)); }
    public Boolean ShadowActionAvailable(String action) { return ActionAvailable(P2, action); }
    public Boolean ShadowCardActionAvailable(PhysicalCardImpl card) { return ActionAvailable(P2, "Use " + GameUtils.getFullName(card)); }
    public Boolean ShadowCardPlayAvailable(PhysicalCardImpl card) { return ActionAvailable(P2, "Play " + GameUtils.getFullName(card)); }
    public Boolean ActionAvailable(String player, String action) {
        List<String> actions = GetAvailableActions(player);
        String lowerAction = action.toLowerCase();
        return actions.stream().anyMatch(x -> x.toLowerCase().contains(lowerAction));
    }

    public Boolean FreepsAnyActionsAvailable() { return AnyActionsAvailable(P1); }
    public Boolean ShadowAnyActionsAvailable() { return AnyActionsAvailable(P2); }
    public Boolean AnyActionsAvailable(String player) {
        List<String> actions = GetAvailableActions(player);
        return actions.size() > 0;
    }

    public Boolean FreepsAnyDecisionsAvailable() { return AnyDecisionsAvailable(P1); }
    public Boolean ShadowAnyDecisionsAvailable() { return AnyDecisionsAvailable(P2); }
    public Boolean AnyDecisionsAvailable(String player) {
        AwaitingDecision ad = GetAwaitingDecision(player);
        return ad != null;
    }

    public List<String> FreepsGetCardChoices() { return GetADParamAsList(P1, "cardId"); }
    public List<String> ShadowGetCardChoices() { return GetADParamAsList(P2, "cardId"); }
    public List<String> FreepsGetMultipleChoices() { return GetADParamAsList(P1, "results"); }
    public List<String> ShadowGetMultipleChoices() { return GetADParamAsList(P2, "results"); }
    public List<String> FreepsGetADParamAsList(String paramName) { return GetADParamAsList(P1, paramName); }
    public List<String> ShadowGetADParamAsList(String paramName) { return GetADParamAsList(P2, paramName); }
    public List<String> GetADParamAsList(String playerID, String paramName) { return Arrays.asList(GetAwaitingDecisionParam(playerID, paramName)); }
    public Object FreepsGetADParam(String paramName) { return GetAwaitingDecisionParam(P1, paramName); }
    public Object ShadowGetADParam(String paramName) { return GetAwaitingDecisionParam(P2, paramName); }
    public String[] GetAwaitingDecisionParam(String playerID, String paramName) {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerID);
        return decision.getDecisionParameters().get(paramName);
    }

    public Map<String, String[]> GetAwaitingDecisionParams(String playerID) {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerID);
        return decision.getDecisionParameters();
    }

    public void FreepsUseCardAction(String name) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, name)); }
    public void FreepsUseCardAction(PhysicalCardImpl card) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, "Use " + GameUtils.getFullName(card))); }
    public void ShadowUseCardAction(String name) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, name)); }
    public void ShadowUseCardAction(PhysicalCardImpl card) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, "Use " + GameUtils.getFullName(card))); }

    public void FreepsPlayCard(String name) throws DecisionResultInvalidException { FreepsPlayCard(GetFreepsCard(name)); }
    public void FreepsPlayCard(PhysicalCardImpl card) throws DecisionResultInvalidException { playerDecided(P1, getCardActionId(P1, "Play " + GameUtils.getFullName(card))); }
    public void ShadowPlayCard(String name) throws DecisionResultInvalidException { ShadowPlayCard(GetShadowCard(name)); }
    public void ShadowPlayCard(PhysicalCardImpl card) throws DecisionResultInvalidException { playerDecided(P2, getCardActionId(P2, "Play " + GameUtils.getFullName(card))); }

    public int FreepsGetWoundsOn(String cardName) { return GetWoundsOn(GetFreepsCard(cardName)); }
    public int ShadowGetWoundsOn(String cardName) { return GetWoundsOn(GetShadowCard(cardName)); }
    public int GetWoundsOn(PhysicalCardImpl card) { return _game.getGameState().getWounds(card); }

    public int GetBurdens() { return _game.getGameState().getBurdens(); }

    public int GetFreepsHandCount() { return GetPlayerHandCount(P1); }
    public int GetShadowHandCount() { return GetPlayerHandCount(P2); }
    public int GetPlayerHandCount(String player)
    {
        return _game.getGameState().getHand(player).size();
    }

    public int GetFreepsDeckCount() { return GetPlayerDeckCount(P1); }
    public int GetShadowDeckCount() { return GetPlayerDeckCount(P2); }
    public int GetPlayerDeckCount(String player)
    {
        return _game.getGameState().getDeck(player).size();
    }

    public int GetFreepsDiscardCount() { return GetPlayerDiscardCount(P1); }
    public int GetShadowDiscardCount() { return GetPlayerDiscardCount(P2); }
    public int GetPlayerDiscardCount(String player)
    {
        return _game.getGameState().getDiscard(player).size();
    }

    public Phase GetCurrentPhase() { return _game.getGameState().getCurrentPhase(); }



    public void FreepsMoveCardToHand(String cardName) { MoveCardToZone(P1, GetFreepsCard(cardName), Zone.HAND); }
    public void FreepsMoveCardToHand(PhysicalCardImpl...cards) {
        for(PhysicalCardImpl card : cards) {
            MoveCardToZone(P1, card, Zone.HAND);
        }
    }
    public void ShadowMoveCardToHand(String cardName) { MoveCardToZone(P2, GetShadowCard(cardName), Zone.HAND); }
    public void ShadowMoveCardToHand(PhysicalCardImpl...cards) {
        for(PhysicalCardImpl card : cards) {
            MoveCardToZone(P2, card, Zone.HAND);
        }
    }


    public void AttachCard(PhysicalCardImpl card, PhysicalCardImpl bearer) { _game.getGameState().attachCard(_game, card, bearer); }

    public void FreepsMoveCardToDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> FreepsMoveCardToDeck(GetFreepsCard(cardName)));
    }
    public void FreepsMoveCardToDeck(PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.DECK));
    }
    public void ShadowMoveCardToDeck(String...cardNames) {
        Arrays.stream(cardNames).forEach(cardName -> ShadowMoveCardToDeck(GetShadowCard(cardName)));
    }
    public void ShadowMoveCardToDeck(PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.DECK));
    }

    public void FreepsMoveCharToTable(String cardName) { FreepsMoveCharToTable(GetFreepsCard(cardName)); }
    public void FreepsMoveCharToTable(PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.FREE_CHARACTERS));
    }
    public void ShadowMoveCharToTable(String cardName) { FreepsMoveCharToTable(GetShadowCard(cardName)); }
    public void ShadowMoveCharToTable(PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P2, card, Zone.SHADOW_CHARACTERS));
    }

    public void FreepsMoveCardToSupportArea(String cardName) { FreepsMoveCardToSupportArea(GetFreepsCard(cardName)); }
    public void FreepsMoveCardToSupportArea(PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.SUPPORT));
    }
    public void ShadowMoveCardToSupportArea(String cardName) { ShadowMoveCardToSupportArea(GetShadowCard(cardName)); }
    public void ShadowMoveCardToSupportArea(PhysicalCardImpl...cards) {
        Arrays.stream(cards).forEach(card -> MoveCardToZone(P1, card, Zone.SUPPORT));
    }

    public void MoveCardToZone(String player, PhysicalCardImpl card, Zone zone) {
        if(card.getZone() != null)
        {
            _game.getGameState().removeCardsFromZone(player, new ArrayList<PhysicalCard>() {{ add(card); }});
        }
        _game.getGameState().addCardToZone(_game, card, zone);
    }

    public void FreepsAddWoundsToChar(String cardName, int count) { AddWoundsToChar(GetFreepsCard(cardName), count); }
    public void ShadowAddWoundsToChar(String cardName, int count) { AddWoundsToChar(GetShadowCard(cardName), count); }
    public void AddWoundsToChar(PhysicalCardImpl card, int count) {
        for(int i = 0; i < count; i++)
        {
            _game.getGameState().addWound(card);
        }
    }

    public int GetTwilight() { return _game.getGameState().getTwilightPool(); }
    public void SetTwilight(int amount) { _game.getGameState().setTwilight(amount); }

    public PhysicalCardImpl GetRingBearer() { return (PhysicalCardImpl)_game.getGameState().getRingBearer(P1); }

    public PhysicalCardImpl GetCurrentSite() { return (PhysicalCardImpl)_game.getGameState().getCurrentSite(); }

    public void SkipToPhase(Phase target) throws DecisionResultInvalidException {
        for(int attempts = 1; attempts <= 20; attempts++)
        {
            Phase current = _game.getGameState().getCurrentPhase();
            if(current == target)
                break;

            SkipCurrentPhaseActions();

            if(attempts == 20)
            {
                throw new DecisionResultInvalidException("Could not arrive at target '" + target + "' after 20 attempts!");
            }
        }
    }

    public void SkipCurrentPhaseActions() throws DecisionResultInvalidException {
        FreepsSkipCurrentPhaseAction();
        ShadowSkipCurrentPhaseAction();
    }

    public void FreepsSkipCurrentPhaseAction() throws DecisionResultInvalidException {
        if(_userFeedback.getAwaitingDecision(P1) != null) {
            playerDecided(P1, "");
        }
    }

    public void ShadowSkipCurrentPhaseAction() throws DecisionResultInvalidException {
        if(_userFeedback.getAwaitingDecision(P2) != null) {
            playerDecided(P2, "");
        }
    }


    public void FreepsAssignToMinion(String name, String target) throws DecisionResultInvalidException { FreepsAssignToMinion(GetFreepsCard(name), GetShadowCard(target)); }
    public void FreepsAssignToMinion(PhysicalCardImpl comp, PhysicalCardImpl minion) throws DecisionResultInvalidException {
        playerDecided(P1, comp.getCardId() + " " + minion.getCardId());
    }
    public void FreepsAssignToMinions(PhysicalCardImpl[]... groups) throws DecisionResultInvalidException {
        String result = "";

        for (PhysicalCardImpl[] group : groups) {
            result += group[0].getCardId();
            for(int i = 1; i < group.length; i++)
            {
                result += " " + group[i].getCardId();
            }
            result += ",";
        }

        playerDecided(P1, result);
    }

    public List<PhysicalCardImpl> FreepsGetAttachedCards(String name) { return GetAttachedCards(GetFreepsCard(name)); }
    public List<PhysicalCardImpl> GetAttachedCards(PhysicalCardImpl card) {
        return (List<PhysicalCardImpl>)(List<?>)_game.getGameState().getAttachedCards(card);
    }

    public void FreepsResolveSkirmish(String name) throws DecisionResultInvalidException { FreepsResolveSkirmish(GetFreepsCard(name)); }
    public void FreepsResolveSkirmish(PhysicalCardImpl comp) throws DecisionResultInvalidException { FreepsChooseCard(comp); }
    public void FreepsChooseCard(PhysicalCardImpl card) throws DecisionResultInvalidException {
        playerDecided(P1, String.valueOf(card.getCardId()));
    }
    public void ShadowChooseCard(PhysicalCardImpl card) throws DecisionResultInvalidException {
        playerDecided(P2, String.valueOf(card.getCardId()));
    }
    public boolean FreepsCanChooseCharacter(PhysicalCardImpl card) { return FreepsGetCardChoices().contains(String.valueOf(card.getCardId())); }
    public boolean ShadowCanChooseCharacter(PhysicalCardImpl card) { return ShadowGetCardChoices().contains(String.valueOf(card.getCardId())); }

    public int FreepsCardChoiceCount() { return FreepsGetCardChoices().size(); }
    public int ShadowCardChoiceCount() { return ShadowGetCardChoices().size(); }

    public boolean IsCharAssigned(PhysicalCardImpl card) {
        List<Assignment> assigns = _game.getGameState().getAssignments();
        return assigns.stream().anyMatch(x -> x.getFellowshipCharacter() == card || x.getShadowCharacters().contains(card));
    }

    public boolean IsAttachedTo(PhysicalCardImpl card, PhysicalCardImpl bearer) {
        if(card.getZone() != Zone.ATTACHED) {
            return false;
        }

        return bearer == card.getAttachedTo();
    }


    public int FreepsGetStrength(String name) { return GetStrength(GetFreepsCard(name)); }
    public int ShadowGetStrength(String name) { return GetStrength(GetShadowCard(name)); }
    public int GetStrength(PhysicalCardImpl card)
    {
        return _game.getModifiersQuerying().getStrength(_game, card);
    }

    public boolean HasKeyword(PhysicalCardImpl card, Keyword keyword)
    {
        return _game.getModifiersQuerying().hasKeyword(_game, card, keyword);
    }


    public void InsertAdHocModifier(Modifier mod)
    {
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(mod);
    }

    public void FreepsChooseToMove() throws DecisionResultInvalidException { playerDecided(P1, "0"); }
    public void FreepsChooseToStay() throws DecisionResultInvalidException { playerDecided(P1, "1"); }

    public void FreepsAcceptOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P1, "0"); }
    public void FreepsDeclineOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P1, "1"); }
    public void ShadowAcceptOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P2, "0"); }
    public void ShadowDeclineOptionalTrigger() throws DecisionResultInvalidException { playerDecided(P2, "1"); }

    public void FreepsChooseMultipleChoiceOption(String option) throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P1, option); }
    public void ShadowChooseMultipleChoiceOption(String option) throws DecisionResultInvalidException { ChooseMultipleChoiceOption(P2, option); }
    public void ChooseMultipleChoiceOption(String playerID, String option) throws DecisionResultInvalidException { ChooseAction(playerID, "results", option); }
    public void ChooseAction(String playerID, String paramName, String option) throws DecisionResultInvalidException {
        List<String> choices = GetADParamAsList(playerID, paramName);
        for(String choice : choices){
            if(choice.toLowerCase().contains(option.toLowerCase())) {
                playerDecided(playerID, String.valueOf(choices.indexOf(choice)));
                return;
            }
        }
        //couldn't find an exact match, so maybe it's a direct index:
        playerDecided(playerID, option);
    }

    public void FreepsResolveActionOrder(String option) throws DecisionResultInvalidException { ChooseAction(P1, "actionText", option); }

}
