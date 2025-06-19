package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_138_ErrataTests
{
    protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new VirtualTableScenario(
                new HashMap<>() {{
                    put("boromir", "1_97");
                    put("event", "1_116");

                    put("snows", "51_138");
                    put("uruk", "1_151");
                    put("runner", "1_178");
                }}
        );
    }

    @Test
    public void SnowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 1E
         * Title: Saruman's Snows
         * Side: Shadow
         * Culture: Isengard
         * Twilight Cost: 2
         * Type: Condition
         * Errata Game Text: Spell.  Weather.  To play, exert an [ISENGARD] minion.  Plays on a site.
         * No player may play skirmish events or use skirmish special abilities during skirmishes involving an [ISENGARD]
         * minion at this site.
         * Discard this condition at the end of the turn.
         */

        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl snows = scn.GetFreepsCard("snows");

        assertFalse(snows.getBlueprint().isUnique());
        assertEquals(2, snows.getBlueprint().getTwilightCost());
        assertEquals(Side.SHADOW, snows.getBlueprint().getSide());
        assertEquals(Culture.ISENGARD, snows.getBlueprint().getCulture());
        assertEquals(CardType.CONDITION, snows.getBlueprint().getCardType());
        assertEquals(Culture.ISENGARD, snows.getBlueprint().getCulture());
        assertTrue(scn.HasKeyword(snows, Keyword.SPELL));
        assertTrue(scn.HasKeyword(snows, Keyword.WEATHER));
    }

    @Test
    public void ExertsMinionWhenPlayed() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl snows = scn.GetShadowCard("snows");
        PhysicalCardImpl uruk = scn.GetShadowCard("uruk");

        scn.MoveCardsToHand(snows);
        scn.MoveCardsToHand(uruk);

        scn.StartGame();

        scn.SetTwilight(10);

        scn.FreepsPassCurrentPhaseAction();
        assertFalse(scn.ShadowActionAvailable("Saruman's Snows"));
        scn.ShadowPlayCard(uruk);
        assertTrue(scn.ShadowActionAvailable("Saruman's Snows"));
        assertEquals(0, scn.GetWoundsOn(uruk));
        scn.ShadowPlayCard(snows);

        assertTrue(scn.ShadowDecisionAvailable("Attach"));
        scn.ShadowChoose(scn.ShadowGetCardChoices().get(1));

        assertEquals(1, scn.GetWoundsOn(uruk));
        assertEquals(Zone.ATTACHED, snows.getZone());
    }

    @Test
    public void DiscardsAtEndOfTurn() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl snows = scn.GetShadowCard("snows");
        PhysicalCardImpl uruk = scn.GetShadowCard("uruk");

        scn.MoveCardsToHand(snows);
        scn.MoveMinionsToTable(uruk);

        scn.StartGame();

        scn.SetTwilight(10);

        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowPlayCard(snows);
        scn.ShadowChoose(scn.ShadowGetCardChoices().get(1));

        scn.MoveCardsToDiscard(uruk);
        scn.SkipToPhase(Phase.REGROUP);
        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsChooseToMove();

        //After moving, Snows should still be attached
        assertEquals(Zone.ATTACHED, snows.getZone());

        scn.SkipToPhase(Phase.REGROUP);
        scn.SkipToPhase(Phase.FELLOWSHIP);

        //Now that it's the end of the turn, snows should be discarded
        assertEquals(Zone.DISCARD, snows.getZone());
    }

    @Test
    public void OnlyBlocksSkirmishStuffAgainstAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        VirtualTableScenario scn = GetScenario();

        PhysicalCardImpl snows = scn.GetShadowCard("snows");
        PhysicalCardImpl uruk = scn.GetShadowCard("uruk");
        PhysicalCardImpl runner = scn.GetShadowCard("runner");

        PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
        PhysicalCardImpl event = scn.GetFreepsCard("event");

        scn.MoveCardsToHand(snows);
        scn.MoveMinionsToTable(uruk);
        scn.MoveMinionsToTable(runner);

        scn.MoveCardsToHand(event);
        scn.MoveCompanionsToTable(boromir);

        scn.StartGame();

        scn.SetTwilight(10);

        scn.FreepsPassCurrentPhaseAction();
        scn.ShadowPlayCard(snows);
        scn.ShadowChoose(String.valueOf(scn.GetCurrentSite().getCardId())); //Should be site 2
        scn.ApplyAdHocModifier(new AddKeywordModifier(null, Filters.name("Uruk Savage"), null, Keyword.FIERCE));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(boromir, runner);

        //skip assigning the uruk
        scn.PassCurrentPhaseActions();

        //start goblin skirmish
        scn.FreepsResolveSkirmish(boromir);
        assertTrue(scn.FreepsActionAvailable("Play Swordarm"));
        assertTrue(scn.FreepsActionAvailable("Use Boromir"));
        scn.PassCurrentPhaseActions();

        //fierce skirmish
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(boromir, uruk);

        //actions should be disallowed by snows as the skirmish involves an isengard minion
        scn.FreepsResolveSkirmish(boromir);
        assertFalse(scn.FreepsActionAvailable("Play Swordarm"));
        assertFalse(scn.FreepsActionAvailable("Use Boromir"));
    }
}
