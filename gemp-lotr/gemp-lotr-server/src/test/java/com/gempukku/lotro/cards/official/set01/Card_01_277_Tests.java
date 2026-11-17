package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_277_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reach", "1_277");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; adds (2) only when a weapon is played on him
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, an Orc who is not [sauron]; no text

					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion to bear the [shire] cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: bears The Weight of a Legacy
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1
					put("tale", "1_317");       // There and Back Again: [shire] condition borne by a Hobbit companion; a maneuver ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, a Free Peoples item that is no condition; no text but its bearer
					put("defenses", "1_274");   // Sauron's Defenses: [sauron] condition in the Shadow support area; acts only beside 3 dead companions
					put("weight", "1_282");     // The Weight of a Legacy: [sauron] condition borne by Aragorn, strength -1; no text but its cost and bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowsReachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Shadow's Reach
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Shadow: Exert a [sauron] Orc to discard a Free Peoples condition.
		*/

		var scn = GetScenario();

		var reach = scn.GetFreepsCard("reach");

		assertEquals("Shadow's Reach", reach.getBlueprint().getTitle());
		assertNull(reach.getBlueprint().getSubtitle());
		assertFalse(reach.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, reach.getBlueprint().getSide());
		assertEquals(Culture.SAURON, reach.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, reach.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(reach, Timeword.SHADOW));
		assertEquals(1, reach.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var defenses = scn.GetShadowCard("defenses");
		var weight = scn.GetShadowCard("weight");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var tale = scn.GetFreepsCard("tale");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(lord);
		scn.AttachCardsTo(sam, tale, sword);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, guard, troop);
		scn.MoveCardsToSupportArea(defenses);
		scn.AttachCardsTo(aragorn, weight);
		assertInZone(Zone.SUPPORT, lord, defenses);
		assertAttachedTo(tale, sam);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(reach));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.ShadowPlayCard(reach);
		// The exertion: either [sauron] Orc; not the [sauron] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		// The discard: a Free Peoples condition in the support area or borne; not a Free Peoples possession, nor a
		// Shadow condition in the support area or on a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, tale));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, defenses, weight));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tale);

		assertInDiscard(tale);
		assertInZone(Zone.SUPPORT, lord, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInDiscard(reach);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard, troop);
		// Vitality 2: one wound exhausts the only [sauron] Orc.  The Uruk-hai and the [moria] Orc could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, lord);
		assertInHand(reach);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(reach));
		assertInHand(reach);
	}

	@Test
	public void WithNoFreePeoplesConditionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var soldier = scn.GetShadowCard("soldier");
		var defenses = scn.GetShadowCard("defenses");
		var weight = scn.GetShadowCard("weight");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(reach);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToSupportArea(defenses);
		scn.AttachCardsTo(aragorn, weight);
		assertInZone(Zone.SUPPORT, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(reach));
		assertEquals(0, scn.GetWoundsOn(soldier));

		// The one [sauron] Orc exerts unasked; the event's target is no requirement (CR 4.2 "effect").
		scn.ShadowPlayCard(reach);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SUPPORT, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInDiscard(reach);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
