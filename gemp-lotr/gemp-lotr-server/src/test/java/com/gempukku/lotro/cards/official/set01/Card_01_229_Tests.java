package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_229_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "1_229");
					put("cantea", "1_230");     // Ulaire Cantea, Lieutenant of Dol Guldur: another Nazgul, strength 10; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Attëa, Keeper of Dol Guldur
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.
		 * <b>Skirmish:</b> Remove (1) to make Úlairë Attëa strength +1 (limit +5).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Keeper of Dol Guldur", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesOneTwilightToMakeHerStrengthPlusOneUpToPlusFiveInThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, cantea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, attea}, new PhysicalCardImpl[]{frodo, cantea});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(7);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, attea));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(10, scn.GetStrength(cantea));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);
		assertEquals(6, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(attea));
		// Another Nazgul, waiting for his own skirmish, is untouched.
		assertEquals(10, scn.GetStrength(cantea));

		for (int use = 2; use <= 5; use++) {
			scn.FreepsPass();
			scn.ShadowUseCardAction(attea);
		}
		// 7 - 5 used; 12 + 5
		assertEquals(2, scn.GetTwilight());
		assertEquals(17, scn.GetStrength(attea));

		// At the limit the ability still has a cost that can be paid, so it is offered; the (1) is paid for nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);
		assertEquals(1, scn.GetTwilight());
		assertEquals(17, scn.GetStrength(attea));
		assertEquals(10, scn.GetStrength(cantea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyTwilightPoolTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(attea));

		// One twilight in the pool, and the same decision offers it.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(attea));
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusEndsWithItsSkirmishPhaseAndTheLimitIsCountedAfreshInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);

		// One use in the first skirmish: 13 against Aragorn's 8, a win short of overwhelming him.
		scn.FreepsPass();
		scn.ShadowUseCardAction(attea);
		assertEquals(13, scn.GetStrength(attea));
		scn.FreepsPass();
		// Still in force at the last decision of that skirmish phase.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(attea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPass();

		// She won, and is fierce: the fierce round.  The bonus is gone with the phase it was made in.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertEquals(12, scn.GetStrength(attea));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, attea));
		assertEquals(9, scn.GetTwilight());

		// Five more uses go through in the fierce skirmish: the +5 is counted again in a new skirmish phase.
		for (int use = 1; use <= 5; use++) {
			scn.FreepsPass();
			scn.ShadowUseCardAction(attea);
		}
		assertEquals(4, scn.GetTwilight());
		assertEquals(17, scn.GetStrength(attea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
