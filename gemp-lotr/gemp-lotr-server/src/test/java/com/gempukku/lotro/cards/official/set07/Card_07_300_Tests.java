package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_300_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fanatic", "7_300");
					put("fanatic2", "7_300");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc, strength 10; his text works only while he is stacked
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai), strength 8; damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), strength 5; his text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: a companion with no text once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcFanaticStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Fanatic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Discard this minion to make another [sauron] Orc strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fanatic");

		assertEquals("Orc Fanatic", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityDiscardsItselfToMakeAnotherSauronOrcStrengthPlus3UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var fanatic2 = scn.GetShadowCard("fanatic2");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(aragorn, guard);
		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, fanatic2, chieftain, soldier, pillager, sentry, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, fanatic}, new PhysicalCardImpl[]{guard, soldier});
		// The second Fanatic and the Chieftain join Aragorn's skirmish; the Pillager, the Sentry and the Hound stay out of any.
		scn.ShadowAssignToMinions(aragorn, fanatic2, chieftain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(fanatic2));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowActionAvailable(fanatic));

		scn.ShadowUseCardAction(fanatic);

		// The copy used is the one discarded; the other stays, and is another [sauron] Orc.
		assertInDiscard(fanatic);
		assertInZone(Zone.SHADOW_CHARACTERS, fanatic2);
		// [sauron] Orcs in this skirmish, in the other one and in none; not the [sauron] Uruk-hai nor the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, fanatic2, soldier, pillager));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertEquals(10, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(fanatic2));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still +3 at the next decision of this skirmish.
		scn.FreepsPass();
		assertEquals(10, scn.GetStrength(chieftain));
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(guard);
		// The next skirmish is a phase of its own: the Chieftain is back to his printed 7.
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherSauronOrcItIsStillOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, sentry, hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fanatic);
		// The Sentry joins the skirmish, so that it goes on once the Fanatic is gone.
		scn.ShadowAssignToMinions(aragorn, sentry);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowActionAvailable(fanatic));

		scn.ShadowUseCardAction(fanatic);

		assertInDiscard(fanatic);
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, chieftain);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowDecisionAvailable("Play Assignment action or Pass"));
		assertFalse(scn.ShadowActionAvailable(fanatic));
	}
}
