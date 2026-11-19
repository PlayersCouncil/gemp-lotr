package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_274_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("officer", "7_274");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, site 6, no besieger; his text answers a weapon played on him
					put("axeman", "7_271");     // Gorgoroth Axeman: [sauron] Orc besieger, cost 3, site 5; a skirmish ability only
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc; his texts answer a minion played and two Hobbits spotted
					put("patrol", "8_99");      // Gorgoroth Patrol: [sauron] Orc besieger; his text works only on a site he is stacked on
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai); damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]); his text needs 2 wounds on the Ring-bearer
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
					put("runner", "1_178");     // Goblin Runner: a minion for a second skirmish; his text answers his own play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("hand1", "1_7");        // Dwarf Guard: cards for the Shadow hand to pay with
					put("hand2", "1_7");
					put("hand3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothOfficerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Officer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Skirmish:</b> Discard 2 cards from hand to play a [sauron] Orc stacked on a site you control. That Orc is <b>fierce</b> and strength +6 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("officer");

		assertEquals("Gorgoroth Officer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsTwoCardsToPlayASauronOrcStackedOnHisSiteFierceAndPlusSixUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var officer = scn.GetShadowCard("officer");
		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var axeman = scn.GetShadowCard("axeman");
		var patrol = scn.GetShadowCard("patrol");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");
		var theirAssassin = scn.GetFreepsCard("assassin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		var hand3 = scn.GetShadowCard("hand3");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player (with a [sauron] Orc of his own on it), sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, theirAssassin);
		scn.StackCardsOn(site2, chieftain, axeman, guard, hound);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, patrol);
		assertInZone(Zone.STACKED, theirAssassin, chieftain, axeman, guard, hound, patrol);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveMinionsToTable(officer, runner);
		scn.SetShadowHand(hand1, hand2, hand3);
		assertEquals(3, scn.GetShadowHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, officer}, new PhysicalCardImpl[]{gimli, runner});
		scn.FreepsResolveSkirmish(aragorn);
		// Enough twilight for any of the stacked cards, so that only the text keeps one out.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(12, scn.GetStrength(officer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(officer));
		scn.ShadowUseCardAction(officer);

		// Two cards of three.
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(hand1, hand2);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(hand1, hand2);

		// The [sauron] Orcs on his site, besieger or not; not the [sauron] Uruk-hai or the [wraith] Orc there, the
		// [sauron] Orc on his condition or the one on the Free Peoples player's site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound, patrol, theirAssassin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.STACKED, axeman, guard, hound);
		// 7 + 6, and fierce; the Officer is not "that Orc".
		assertEquals(13, scn.GetStrength(chieftain));
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(officer));
		assertFalse(scn.HasKeyword(officer, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force in the next skirmish, and in the fierce round.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(13, scn.GetStrength(chieftain));
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		assertEquals(13, scn.GetStrength(chieftain));
		assertTrue(scn.HasKeyword(chieftain, Keyword.FIERCE));

		// Gone with the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(7, scn.GetStrength(chieftain));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var officer = scn.GetShadowCard("officer");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, chieftain);
		assertInZone(Zone.STACKED, chieftain);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(officer);
		scn.SetShadowHand(hand1);
		assertEquals(1, scn.GetShadowHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, officer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(officer));

		// A second card in hand, and it is offered.
		scn.SetShadowHand(hand1, hand2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(officer));
	}

	@Test
	public void IsNotOfferedWithoutASauronOrcOnHisSiteThatHeCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var officer = scn.GetShadowCard("officer");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var patrol = scn.GetShadowCard("patrol");
		var teeth = scn.GetShadowCard("teeth");
		var theirChieftain = scn.GetFreepsCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// On his own site only cards the text does not take; a [sauron] Orc on his condition and on the other
		// player's site.
		scn.StackCardsOn(site1, theirChieftain);
		scn.StackCardsOn(site2, guard, hound);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, patrol);
		assertInZone(Zone.STACKED, theirChieftain, guard, hound, patrol);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(officer);
		scn.SetShadowHand(hand1, hand2);
		assertEquals(2, scn.GetShadowHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, officer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(officer));

		// A [sauron] Orc on his site, with one twilight too few for him: 2 + 2 roaming at site 4.
		scn.StackCardsOn(site2, chieftain);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.STACKED, chieftain);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(officer));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(officer));
	}
}
