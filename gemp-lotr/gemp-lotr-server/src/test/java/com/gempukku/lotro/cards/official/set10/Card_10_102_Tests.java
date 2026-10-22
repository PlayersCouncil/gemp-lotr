package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "10_102");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 only
					put("sentinel", "10_83");   // Cirith Ungol Sentinel: [sauron] Uruk-hai, strength 7; a discount on its own cost only
					put("scout", "2_47");       // Uruk Scout: [isengard] Uruk-hai; a roaming discount and a response to ranger events

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Uruk Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [sauron] minion.<br>If bearer is an Uruk-hai, it is strength +2.
		*/

		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");

		assertEquals("Uruk Axe", axe.getBlueprint().getTitle());
		assertNull(axe.getBlueprint().getSubtitle());
		assertFalse(axe.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, axe.getBlueprint().getSide());
		assertEquals(Culture.SAURON, axe.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, axe.getBlueprint().getCardType());
		assertTrue(axe.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, axe.getBlueprint().getTwilightCost());
		assertEquals(1, axe.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnASauronMinionAnUrukHaiBearerIsStrengthPlusTwoBesideItsPrintedPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var sentinel = scn.GetShadowCard("sentinel");
		var scout = scn.GetShadowCard("scout");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(axe);
		assertInHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, sentinel, scout);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(sentinel));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);

		// [sauron] minions of either race and of two titles; not the [isengard] Uruk-hai, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, guard, sentinel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scout, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertAttachedTo(axe, guard);
		// 9 + 1 printed + 2 for an Uruk-hai bearer.
		assertEquals(12, scn.GetStrength(guard));
		// Another [sauron] Uruk-hai, who does not bear it.
		assertEquals(7, scn.GetStrength(sentinel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByASauronOrcItGivesOnlyItsPrintedPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));

		scn.AttachCardsTo(soldier, axe);
		assertAttachedTo(axe, soldier);

		// An Orc, no Uruk-hai: 7 + 1 printed, and nothing more.  The Uruk-hai beside him gets nothing either.
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
