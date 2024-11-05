package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_032_ErrataTests
{
    protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
        return new GenericCardTestHelper(
                new HashMap<>() {{
                    put("brand", "52_32");
                    put("brand2", "52_32");
                    put("arwen", "1_30");
                    put("boromir", "1_97");
                    put("aragorn", "1_89");

                    put("nazgul", "1_229");
                    put("runner", "1_178");
                }}
        );
    }

    @Test
    public void FlamingBrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

        /**
         * Set: 2
         * Title: Flaming Brand
         * Unique: False
         * Side: FREE_PEOPLE
         * Culture: Gondor
         * Twilight Cost: 1
         * Type: possession
         * Subtype: Hand Weapon
         * Strength: 1
         * Game Text: Bearer must be a ranger. This weapon may be borne in addition to 1 other hand weapon.
         * 	Skirmish: Make bearer strength +2 and <b>damage +1</b> while skirmishing a Nazgûl until the regroup phase
         * 	(limit once per turn). Discard this possession at the start of the regroup phase.
         */

        //Pre-game setup
        var scn = GetScenario();

        var brand = scn.GetFreepsCard("brand");

        assertFalse(brand.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, brand.getBlueprint().getSide());
        assertEquals(Culture.GONDOR, brand.getBlueprint().getCulture());
        assertEquals(CardType.POSSESSION, brand.getBlueprint().getCardType());
        assertTrue(brand.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
        assertEquals(1, brand.getBlueprint().getTwilightCost());
        assertEquals(1, brand.getBlueprint().getStrength());
    }

    @Test
    public void CanBeBorneByRangers() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
        PhysicalCardImpl boromir = scn.GetFreepsCard("boromir");
        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl brand = scn.GetFreepsCard("brand");

        scn.FreepsMoveCharToTable(arwen);
        scn.FreepsMoveCharToTable(boromir);
        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(brand);

        scn.StartGame();


        assertTrue(scn.FreepsPlayAvailable(brand));

        scn.FreepsPlayCard(brand);

        //There are 3 companions in play, but only 2 rangers, so we should only see 2 options
        assertEquals(2, scn.FreepsGetADParamAsList("cardId").size());
        scn.FreepsChooseCard(aragorn);
        assertTrue(scn.IsAttachedTo(brand, aragorn));

    }

    @Test
    public void CanBeBorneTwice() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl brand = scn.GetFreepsCard("brand");
        PhysicalCardImpl brand2 = scn.GetFreepsCard("brand2");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(brand);
        scn.FreepsMoveCardToHand(brand2);

        scn.StartGame();

        scn.FreepsPlayCard(brand);
        assertTrue(scn.FreepsPlayAvailable(brand2));
        scn.FreepsPlayCard(brand2);

        assertEquals(2, scn.GetAttachedCards(aragorn).size());
    }

    @Test
    public void SkirmishAbilityAvailableWhenSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl brand = scn.GetFreepsCard("brand");
        PhysicalCardImpl runner = scn.GetShadowCard("runner");
        PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsMoveCardToHand(brand);

        scn.ShadowMoveCharToTable(runner);
        scn.ShadowMoveCharToTable(nazgul);

        scn.StartGame();

        assertEquals(8, scn.GetStrength(aragorn));
        scn.FreepsPlayCard(brand);
        assertEquals(9, scn.GetStrength(aragorn));

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, runner);
        //skip assigning the nazgul
        scn.PassCurrentPhaseActions();

        //start goblin skirmish
        scn.FreepsResolveSkirmish(aragorn);

        scn.PassCurrentPhaseActions();

        //assignment for fierce skirmish
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, nazgul);
        scn.FreepsResolveSkirmish(aragorn);

        assertTrue(scn.FreepsAnyActionsAvailable());
        assertTrue(scn.FreepsActionAvailable(brand));

        assertFalse(scn.hasKeyword(aragorn, Keyword.DAMAGE));
        scn.FreepsUseCardAction(brand);
        assertEquals(11, scn.GetStrength(aragorn));
        assertTrue(scn.hasKeyword(aragorn, Keyword.DAMAGE));
        assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
    }

    @Test
    public void SkirmishAbilityLastsUntilRegroupAndSelfDiscards() throws DecisionResultInvalidException, CardNotFoundException {
        //Pre-game setup
        GenericCardTestHelper scn = GetScenario();

        PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
        PhysicalCardImpl brand = scn.GetFreepsCard("brand");
        PhysicalCardImpl nazgul = scn.GetShadowCard("nazgul");

        scn.FreepsMoveCharToTable(aragorn);
        scn.FreepsAttachCardsTo(aragorn, brand);

        scn.ShadowMoveCharToTable(nazgul);

        scn.StartGame();

        scn.SkipToPhase(Phase.ASSIGNMENT);
        scn.PassCurrentPhaseActions();
        scn.FreepsAssignToMinions(aragorn, nazgul);

        scn.FreepsResolveSkirmish(aragorn);

        assertTrue(scn.FreepsActionAvailable(brand));
        assertEquals(9, scn.GetStrength(aragorn));
        assertFalse(scn.hasKeyword(aragorn, Keyword.DAMAGE));
        scn.FreepsUseCardAction(brand);
        assertEquals(11, scn.GetStrength(aragorn));
        assertTrue(scn.hasKeyword(aragorn, Keyword.DAMAGE));

        scn.ShadowPassCurrentPhaseAction();
        scn.FreepsPassCurrentPhaseAction();

        //assignment for fierce skirmish
        scn.PassCurrentPhaseActions();

        scn.FreepsAssignToMinions(aragorn, nazgul);
        scn.FreepsResolveSkirmish(aragorn);
        assertTrue(scn.IsAttachedTo(brand, aragorn));
        assertEquals(11, scn.GetStrength(aragorn));
        assertTrue(scn.hasKeyword(aragorn, Keyword.DAMAGE));

        scn.PassCurrentPhaseActions();

        assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
        assertEquals(Zone.DISCARD, brand.getZone());
    }
}
