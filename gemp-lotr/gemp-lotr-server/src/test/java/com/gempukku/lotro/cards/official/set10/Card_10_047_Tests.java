package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("call", "10_47");
					// Desert Soldier: a Southron, cost 4, site 4; its optional trigger comes at the start of its own skirmish
					put("soldier", "4_220");
					put("explorer", "4_250");   // Southron Explorer: a Southron, cost 2, site 4; a regroup ability only
					// Easterling Axeman: a [raider] Man who is no Southron, cost 2, site 4; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("call", "10_47");
					// Sauron's Hatred, a [sauron] support-area condition: "Shadow: Remove a threat to play a
					// [sauron] Orc without paying any roaming penalty."
					put("hatred", "7_310");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2; its text answers a weapon played on it

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that 2 threats can lie
					// Sting, Elven Long Knife, a hand weapon on Bilbo or Frodo: "Regroup: Exert bearer to remove
					// a threat."
					put("sting", "18_113");
					// Tom Bombadil, The Master, a [shire] ally: "To play, remove 2 burdens or 2 threats."
					put("tom", "9_52");
					// Hornblower Leaf, a [shire] condition (pipeweed): "Regroup: Discard a pipeweed from play to remove
					// a threat."  A Free Peoples card that is neither a character nor a possession.
					put("leaf", "17_108");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path with City Gates as its site 5: "When the fellowship moves to this site, add 3 threats. When the
	// fellowship moves from this site, remove 3 threats."  A site is a card of neither side.
	protected VirtualTableScenario GetCityGatesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("call", "10_47");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North, and
					put("knight", "5_35");      // Gondorian Knight ("Knight." is its whole text): three companions hold 3 threats
					// Tom Bombadil, The Master, a [shire] ally: "To play, remove 2 burdens or 2 threats."
					put("tom", "9_52");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.KingSites);
					put("site5", "7_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallyingCallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Rallying Call
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.
		 * Threats cannot be removed by Free Peoples cards.
		 * <b>Shadow:</b> Remove (1) and play a Southron to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("call");

		assertEquals("Rallying Call", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForNothingToTheSupportAreaWithASouthronToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var call = scn.GetShadowCard("call");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(call);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for the site
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(call));

		scn.ShadowPlayCard(call);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, call);
		assertNull(call.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var call = scn.GetShadowCard("call");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(call, axeman);

		scn.StartGame();
		// A [raider] minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// 4 set + 2 companions + 2 for the site
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(call));

		// An Easterling is a [raider] Man: 2 + 2 roaming.
		scn.ShadowPlayCard(axeman);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(call));

		scn.ShadowPlayCard(call);
		assertInZone(Zone.SUPPORT, call);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionRemoves1AndPlaysASouthronFromHandToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Two companions: there is room for 2 threats.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var call = scn.GetShadowCard("call");
		var soldier = scn.GetShadowCard("soldier");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(call);
		scn.MoveCardsToHand(soldier, explorer, axeman);

		scn.StartGame();
		scn.SetTwilight(6);
		scn.FreepsPass();

		// 6 set + 2 companions + 2 for the site
		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(call));

		scn.ShadowUseCardAction(call);
		// The (1) is removed first.  Either Southron may be played; the Easterling, a [raider] Man, may not.
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(soldier);
		// 9 - 4 for the Soldier - 2 roaming (site number 4 at site 2)
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertInZone(Zone.HAND, explorer, axeman);
		assertInZone(Zone.SUPPORT, call);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWhenThePoolCanPayForTheSouthronAfterThe1AndMayThenEmptyIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var call = scn.GetShadowCard("call");
		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(call);
		scn.MoveCardsToHand(soldier, axeman);

		scn.StartGame();
		scn.FreepsPass();

		// The Soldier costs 4 + 2 roaming: with 6 he can be played from hand, and so can the Easterling (2 + 2),
		// but there is nothing left over for the (1), and the Easterling is no Southron to play in his place.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(soldier));
		assertTrue(scn.ShadowPlayAvailable(axeman));
		assertFalse(scn.ShadowActionAvailable(call));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(call));
		assertEquals(0, scn.GetThreats());

		// The Soldier is the only Southron in hand, so he is played without asking: 7 - 1 - 4 - 2 roaming.
		scn.ShadowUseCardAction(call);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertInZone(Zone.HAND, axeman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileItIsInPlayAFreePeoplesCardRemovesNoThreatAndAShadowCardStillDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var tom = scn.GetFreepsCard("tom");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(leaf);
		scn.MoveCardsToHand(tom);

		var call = scn.GetShadowCard("call");
		var hatred = scn.GetShadowCard("hatred");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(call, hatred);
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		// Two companions hold the 2 threats.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		// A Free Peoples card that must remove threats to be played: there are no burdens to remove in their
		// place, and Tom Bombadil is not offered.
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, tom);
		assertFalse(scn.FreepsPlayAvailable(tom));

		// A Shadow card removes one as its cost, and what the threat buys happens.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(hatred));
		scn.ShadowUseCardAction(hatred);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// Nobody fights the Orc: on to the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// A Free Peoples condition whose effect removes a threat: the Leaf is discarded for it, and the threat stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, leaf);
		scn.FreepsUseCardAction(leaf, "Remove a threat");
		assertInZone(Zone.DISCARD, leaf);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPass();

		// A Free Peoples possession whose effect removes a threat: Frodo exerts for Sting, and the threat stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithoutItAFreePeoplesCardRemovesAThreatAndOneThatMustRemoveThreatsToBePlayedIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var tom = scn.GetFreepsCard("tom");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(leaf);
		scn.MoveCardsToHand(tom);

		scn.StartGame();
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		// The same board with no Rallying Call in play: Tom Bombadil can pay with the 2 threats.
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(tom));

		// The Leaf's regroup action discards it and removes a threat.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SUPPORT, leaf);
		scn.FreepsUseCardAction(leaf, "Remove a threat");
		assertInZone(Zone.DISCARD, leaf);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPass();

		// Sting's exerts Frodo and removes the other.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASiteStillRemovesThreatsWhileItIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCityGatesScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var tom = scn.GetFreepsCard("tom");
		scn.MoveCompanionsToTable(aragorn, knight);

		var call = scn.GetShadowCard("call");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(tom);
		scn.FreepsRefreshDecision();

		// City Gates added 3 threats as the fellowship arrived, and Tom Bombadil can pay with 2 of them (there is
		// no burden).
		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(tom));

		// With Rallying Call in the Shadow player's support area he is no longer offered.
		scn.MoveCardsToSupportArea(call);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(tom));

		// The fellowship moves on, and the site, which is no Free Peoples card, removes its 3 threats.
		scn.FreepsPass();
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, call);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
