package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_252_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "51_252");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; adds (2) when a weapon is played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, so a [sauron] minion who is not an Orc; Damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its when-played (2) does nothing when cheated in

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer; Fellowship: exert Sam to remove a burden
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not Ring-bound; a maneuver ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, so a character who is not a companion; an ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheIrresistibleShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Irresistible Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [sauron] Orc.
		 * 	The Ring-bearer is strength +1 for each burden you can spot.
		 * 	If you cannot spot another companion, the Ring-bearer is resistance -2 for each burden you can spot.
		 */

		var scn = GetScenario();

		var shadow = scn.GetFreepsCard("shadow");

		assertEquals("The Irresistible Shadow", shadow.getBlueprint().getTitle());
		assertNull(shadow.getBlueprint().getSubtitle());
		assertTrue(shadow.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shadow.getBlueprint().getSide());
		assertEquals(Culture.SAURON, shadow.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, shadow.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(shadow, Keyword.SUPPORT_AREA));
		assertEquals(0, shadow.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcToPlayToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, sentry, runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(shadow));

		scn.ShadowPlayCard(shadow);
		// Both [sauron] Orcs are offered; the [sauron] Uruk-hai and the [moria] Orc are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(runner));
		// Costs 0.
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, shadow);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, runner);
		// Vitality 2 with 1 wound: exhausted.  The Uruk-hai and the [moria] Orc beside it could exert.
		scn.AddWoundsToChar(soldier, 1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shadow));
		assertInHand(shadow);
	}

	@Test
	public void RingBearerIsStrengthPlusOneForEachBurdenAndSamIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var shadow = scn.GetShadowCard("shadow");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(4, scn.GetStrength(frodo)); // 3 base + 1 from The Ruling Ring
		assertEquals(3, scn.GetStrength(sam));

		scn.AddBurdens(1);
		assertEquals(5, scn.GetStrength(frodo)); // 4 + 1 burden
		assertEquals(3, scn.GetStrength(sam));

		scn.AddBurdens(2);
		assertEquals(7, scn.GetStrength(frodo)); // 4 + 3 burdens
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(frodo)); // 4 + 2 burdens
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AloneTheRingBearerIsResistanceMinusTwoForEachBurdenUntilAnotherCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var celeborn = scn.GetFreepsCard("celeborn");
		var shadow = scn.GetShadowCard("shadow");
		scn.MoveCardsToHand(sam);
		// An ally is not a companion, so he does not switch the penalty off.
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));

		scn.AddBurdens(1);
		assertEquals(7, scn.GetResistance(frodo)); // 10 - 1 burden - 2

		scn.AddBurdens(2);
		assertEquals(1, scn.GetResistance(frodo)); // 10 - 3 burdens - 6

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(7, scn.GetResistance(frodo)); // 10 - 3 burdens
		assertEquals(2, scn.GetResistance(sam));   // 5 - 3 burdens
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnyOtherCompanionSwitchesThePenaltyOffNotOnlyARingBoundOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadow = scn.GetShadowCard("shadow");
		scn.MoveCardsToHand(aragorn);
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();

		scn.AddBurdens(3);
		assertEquals(1, scn.GetResistance(frodo)); // 10 - 3 burdens - 6

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(7, scn.GetResistance(frodo)); // 10 - 3 burdens
		assertEquals(3, scn.GetResistance(aragorn)); // 6 - 3 burdens
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
