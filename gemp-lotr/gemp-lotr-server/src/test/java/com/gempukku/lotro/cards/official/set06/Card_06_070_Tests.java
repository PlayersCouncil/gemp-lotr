package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tender", "6_70");
					put("tender2", "6_70");
					// [isengard] cards to stack it on. Gnawing, Biting, Hacking, Burning: a condition whose stack
					// trigger answers a regroup action's discard of an [isengard] Orc from play, and whose Shadow ability
					// is never used. Scaffolding: a possession with the same stack trigger, and a Response to a wound on
					// an [isengard] Orc, which no test sets off.
					put("gnawing", "6_63");
					put("scaffold", "6_73");
					// Web: a [gollum] possession, not [isengard]. "Regroup: If there are fewer than 3 cards stacked here,
					// spot your Orc or [gollum] minion to stack that minion here": play can stack the Tender on it.
					put("web", "8_30");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion, Damage +1 and no other text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardTenderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Isengard Tender
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> If stacked on an [isengard] card, discard this card and remove (2) to exhaust a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tender");

		assertEquals("Isengard Tender", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StackedOnAnIsengardConditionItDiscardsItselfAndRemovesTwoToExhaustACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		// Gimli, vitality 3, is exhausted: he cannot be exhausted (F-0005).
		scn.AddWoundsToChar(gimli, 2);

		var tender = scn.GetShadowCard("tender");
		var tender2 = scn.GetShadowCard("tender2");
		var gnawing = scn.GetShadowCard("gnawing");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(gnawing);
		scn.StackCardsOn(gnawing, tender, tender2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.STACKED, tender, tender2);
		assertEquals(2, scn.GetStackedCards(gnawing).size());
		scn.MoveMinionsToTable(savage);
		// Nobody is assigned to the Savage, so it is in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tender));
		scn.ShadowUseCardAction(tender);

		// This copy leaves the stack for the discard pile, and (2) is removed; the other copy stays stacked.
		assertInZone(Zone.DISCARD, tender);
		assertInZone(Zone.STACKED, tender2);
		assertEquals(1, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.GetStackedCards(gnawing).contains(tender2));
		assertEquals(1, scn.GetTwilight());

		// The Shadow player chooses: a companion who can be exhausted, and no ally or minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, barliman, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Vitality 4 exhausted: 3 wounds, where an exertion twice would place 2.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedStackedOnACardThatIsNotIsengardNorWhileInPlayAsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tender = scn.GetShadowCard("tender");
		var tender2 = scn.GetShadowCard("tender2");
		var web = scn.GetShadowCard("web");
		scn.MoveCardsToSupportArea(web);
		scn.StackCardsOn(web, tender);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender);
		scn.MoveMinionsToTable(tender2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Stacked on a [gollum] possession, and in play: neither copy is offered, with (5) in the pool.
		assertInZone(Zone.SHADOW_CHARACTERS, tender2);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tender));
		assertFalse(scn.ShadowActionAvailable(tender2));
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void OnAnIsengardPossessionItWantsTwoInThePoolAndWithNoCompanionToExhaustIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		// Frodo, vitality 4, the only companion, is exhausted: nobody can be exhausted.
		scn.AddWoundsToChar(frodo, 3);

		var tender = scn.GetShadowCard("tender");
		var scaffold = scn.GetShadowCard("scaffold");
		scn.MoveCardsToSupportArea(scaffold);
		scn.StackCardsOn(scaffold, tender);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInZone(Zone.STACKED, tender);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// (1) cannot pay the removal of (2).
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tender));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(tender));
		scn.ShadowUseCardAction(tender);

		assertInZone(Zone.DISCARD, tender);
		assertEquals(0, scn.GetStackedCards(scaffold).size());
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
