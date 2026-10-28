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

public class Card_07_276_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ransacker", "7_276");
					put("ransacker2", "7_276");
					put("ransacker3", "7_276");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him
					put("axeman", "7_271");     // Gorgoroth Axeman: [sauron] Orc besieger, strength 9; a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), strength 9; damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), strength 5; his text needs 2 wounds on the Ring-bearer
					put("teeth", "5_110");      // Teeth of Mordor: a [sauron] card that is no minion, a condition that play stacks a [sauron] Orc on
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("hand1", "1_7");        // Dwarf Guard: cards for the Free Peoples hand, which decides initiative
					put("hand2", "1_7");
					put("hand3", "1_7");
					put("hand4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothRansackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Ransacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Skirmish:</b> If this minion is stacked on a site you control, discard him to make a [sauron] Orc strength +5 (or +10 if you have initiative).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ransacker");

		assertEquals("Gorgoroth Ransacker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithoutInitiativeItIsDiscardedFromHisSiteToMakeASauronOrcStrengthPlusFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var ransacker2 = scn.GetShadowCard("ransacker2");
		var chieftain = scn.GetShadowCard("chieftain");
		var axeman = scn.GetShadowCard("axeman");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, ransacker, ransacker2);
		assertInZone(Zone.STACKED, ransacker, ransacker2);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveMinionsToTable(chieftain, axeman, guard, hound);
		scn.MoveCardsToSupportArea(teeth);
		// Four cards in the Free Peoples hand: the Shadow has no initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, hound});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ransacker));
		scn.ShadowUseCardAction(ransacker);

		// The one used is discarded; the other stays on the site.
		assertInDiscard(ransacker);
		assertTrue(scn.GetStackedCards(site1).contains(ransacker2));
		assertFalse(scn.GetStackedCards(site1).contains(ransacker));

		// The [sauron] Orc in the skirmish and the one in none; not the [sauron] Uruk-hai, the [wraith] Orc or the
		// [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound, teeth));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		// 9 + 5
		assertEquals(14, scn.GetStrength(axeman));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithInitiativeItIsPlusTenUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var chieftain = scn.GetShadowCard("chieftain");
		var axeman = scn.GetShadowCard("axeman");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, ransacker);
		assertInZone(Zone.STACKED, ransacker);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveMinionsToTable(chieftain, axeman, hound);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, hound});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.FreepsHasInitiative());
		assertEquals(9, scn.GetStrength(axeman));
		scn.ShadowUseCardAction(ransacker);
		scn.ShadowChooseCard(axeman);

		// 9 + 10
		assertInDiscard(ransacker);
		assertEquals(19, scn.GetStrength(axeman));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		// Aragorn won against the Chieftain.
		assertEquals(1, scn.GetWoundsOn(chieftain));

		scn.FreepsResolveSkirmish(gimli);
		// The next skirmish is a phase of its own: the Axeman is back to his printed 9.
		assertEquals(9, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnlyARansackerStackedOnASiteItsOwnerControlsOffersItsSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ransacker = scn.GetShadowCard("ransacker");
		var ransacker2 = scn.GetShadowCard("ransacker2");
		var ransacker3 = scn.GetShadowCard("ransacker3");
		var teeth = scn.GetShadowCard("teeth");
		var chieftain = scn.GetShadowCard("chieftain");
		var theirRansacker = scn.GetFreepsCard("ransacker");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 is the other player's (he took it as the Shadow player of an earlier turn), with a Ransacker of his
		// own on it; site 2 is the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.StackCardsOn(site1, theirRansacker);
		scn.StackCardsOn(site2, ransacker);
		// A Ransacker stacked on a condition, and one in play.
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, ransacker2);
		assertInZone(Zone.STACKED, theirRansacker, ransacker, ransacker2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(chieftain, ransacker3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ransacker));
		assertFalse(scn.ShadowActionAvailable(ransacker2));
		assertFalse(scn.ShadowActionAvailable(ransacker3));
		assertFalse(scn.ShadowActionAvailable(theirRansacker));
	}
}
