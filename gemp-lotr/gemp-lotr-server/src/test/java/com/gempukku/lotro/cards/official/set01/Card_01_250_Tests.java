package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_250_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hate", "1_250");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [sauron] ([moria]), no text
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally, a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hate
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Maneuver: Exert a [sauron] Orc to wound a companion (except the Ring-bearer).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hate");

		assertEquals("Hate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcToWoundACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, guard, troop);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hate));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.ShadowPlayCard(hate);
		// The cost: either [sauron] Orc; not the [sauron] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		// The wound: a companion other than the Ring-bearer, Ring-bound or not; not an ally, nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, grimir, chieftain, guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInDiscard(hate);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileNoSauronOrcCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(hate);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, troop);
		// Vitality 2: one wound exhausts the only [sauron] Orc.  The Uruk-hai and the [moria] Orc could exert.
		scn.AddWoundsToChar(soldier, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hate));
		assertInHand(hate);
	}
}
