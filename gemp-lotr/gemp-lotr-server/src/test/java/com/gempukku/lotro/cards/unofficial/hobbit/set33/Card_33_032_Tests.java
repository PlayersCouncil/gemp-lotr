package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_032_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (strength 3) as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jails", "33_32");
					put("jails2", "33_32");

					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc; a Shadow ability not used
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc; a Maneuver ability not used
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; a Regroup ability that needs an ally of site 1
					put("runner", "31_30");     // Goblin Runner: a [moria] Orc; a trigger on its own play only

					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion, strength 6; a Skirmish ability not used
					put("kili", "30_12");       // Kili, Brother of Fili: [dwarven] companion, strength 6; an Archery ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("fili", "30_10");       // Fili, Brother of Kili: [dwarven] companion; laid only stacked
					// Thrarin, Dwarven Smith: a [dwarven] ally, strength 4; a Maneuver ability not used
					put("thrarin", "1_27");
					// Ancestral Knowledge: [dwarven] Maneuver event, exert a Dwarf character to discard a condition
					put("knowledge", "30_1");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ElvenJailsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Elven Jails
		 * Unique: False
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When this condition is discarded, cards stacked here return to their owner's hand.
		 * 	Regroup: Discard a [gundabad] Orc from play and remove (4) to stack a [dwarven] companion with the lowest strength here.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jails");

		assertEquals("Elven Jails", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenDiscardedTheCardsStackedHereReturnToTheirOwnersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jails = scn.GetShadowCard("jails");
		var jails2 = scn.GetShadowCard("jails2");
		var yazneg = scn.GetShadowCard("yazneg");
		var fili = scn.GetFreepsCard("fili");
		var kili = scn.GetFreepsCard("kili");
		var balin = scn.GetFreepsCard("balin");
		var knowledge = scn.GetFreepsCard("knowledge");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(jails, jails2);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		// Each copy holds a Dwarf, as its Regroup ability puts him there.
		scn.StackCardsOn(jails, fili);
		scn.StackCardsOn(jails2, kili);
		assertTrue(scn.GetStackedCards(jails).contains(fili));
		assertTrue(scn.GetStackedCards(jails2).contains(kili));
		scn.MoveMinionsToTable(yazneg);
		scn.SkipToPhase(Phase.MANEUVER);
		int freepsHand = scn.GetFreepsHandCount();
		int shadowHand = scn.GetShadowHand().size();

		scn.FreepsPlayCard(knowledge);
		// Balin, the one Dwarf, exerts unasked; the Free Peoples player discards one copy.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(jails, jails2));
		scn.FreepsChooseCard(jails);

		assertInDiscard(jails, knowledge);
		// Fili goes to the hand of his owner, the Free Peoples player, not to the Jails' owner's.
		assertTrue(scn.GetFreepsHand().contains(fili));
		// the event left the hand and Fili came into it
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(shadowHand, scn.GetShadowHand().size());
		// The other copy keeps its Dwarf.
		assertInZone(Zone.SUPPORT, jails2);
		assertTrue(scn.GetStackedCards(jails2).contains(kili));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void RegroupDiscardsAGundabadOrcAndRemoves4ToStackTheWeakestDwarvenCompanionHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jails = scn.GetShadowCard("jails");
		var jails2 = scn.GetShadowCard("jails2");
		var watchful = scn.GetShadowCard("watchful");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		var gloin = scn.GetFreepsCard("gloin");
		var kili = scn.GetFreepsCard("kili");
		var balin = scn.GetFreepsCard("balin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gloin, kili, balin);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(jails, jails2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, jails, jails2);
		// With no minion on the table the game goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(watchful, yazneg, ferny, runner);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(jails));
		scn.ShadowUseCardAction(jails);
		// [gundabad] Orcs of two titles; not a [gundabad] Man, not a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(watchful, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);
		assertInDiscard(yazneg);
		assertEquals(1, scn.GetTwilight());

		// The weakest [dwarven] companions, at 6; not Balin at 7, not Bilbo at 3 (no [dwarven] card), not
		// Thrarin at 4 (an ally).
		assertTrue(scn.ShadowHasCardChoiceAvailable(gloin, kili));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, bilbo, thrarin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gloin);

		assertInZone(Zone.STACKED, gloin);
		assertTrue(scn.GetStackedCards(jails).contains(gloin));
		assertTrue(scn.GetStackedCards(jails2).isEmpty());
		assertInZone(Zone.FREE_CHARACTERS, kili, balin);
		assertInZone(Zone.SHADOW_CHARACTERS, watchful, ferny, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoDwarvenCompanionTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jails = scn.GetShadowCard("jails");
		var yazneg = scn.GetShadowCard("yazneg");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetRingBearer();
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(jails);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, jails);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(yazneg);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		// An ability with a cost is offered whatever its effect will do.
		assertTrue(scn.ShadowActionAvailable(jails));
		scn.ShadowUseCardAction(jails);
		assertInDiscard(yazneg);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.GetStackedCards(jails).isEmpty());
		assertInZone(Zone.FREE_CHARACTERS, bilbo);
		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnly3Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jails = scn.GetShadowCard("jails");
		var yazneg = scn.GetShadowCard("yazneg");
		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCompanionsToTable(gloin);
		scn.MoveCardsToSupportArea(jails);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, jails);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(yazneg);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(jails));
	}

	@Test
	public void NotOfferedWithNoGundabadOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var jails = scn.GetShadowCard("jails");
		var ferny = scn.GetShadowCard("ferny");
		var runner = scn.GetShadowCard("runner");
		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCompanionsToTable(gloin);
		scn.MoveCardsToSupportArea(jails);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, jails);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ferny, runner);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(jails));
	}
}
