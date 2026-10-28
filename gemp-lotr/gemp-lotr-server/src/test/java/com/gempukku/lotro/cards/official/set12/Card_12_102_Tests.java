package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblins", "12_102");
					put("goblins2", "12_102");
					put("entrapping", "11_120"); // Entrapping Orc: [orc] Orc, cost 3, site 4; an assignment ability only
					// Ordnance Grunt: [orc] Troll, no Orc (cheated in; its other texts are on its play)
					put("grunt", "13_117");
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc; adds (2), unasked, when played

					put("spear", "12_96");       // Orc Spear: [orc] hand weapon, cost 1, on an [orc] MINION; a play trigger at cost 4 or more
					put("hammer", "11_130");     // Orc Hammer: [orc] hand weapon, cost 2, on an [orc] Orc; a play trigger only
					// Bladed Gauntlets: [orc] armor, no weapon, cost 1, on an [orc] minion
					put("gauntlets", "11_109");
					// Goblin Scimitar (The Hobbit): [moria] hand weapon, cost 0, on ANY Orc (to play, spot a [moria] card)
					put("scimitar", "31_31");
					put("orkishax", "18_84");    // Orkish Ax: [orc] hand weapon, cost 1, on an [orc] Orc
					put("mordorscimitar", "11_128"); // Mordor Scimitar: [orc] hand weapon, cost 2, on an [orc] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScavengingGoblinsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Scavenging Goblins
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may play an [orc] weapon from your discard pile on your [orc] Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("goblins");

		assertEquals("Scavenging Goblins", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayPlayAnOrcWeaponFromItsOwnersDiscardPileOnAnOrcOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblins = scn.GetShadowCard("goblins");
		var entrapping = scn.GetShadowCard("entrapping");
		var grunt = scn.GetShadowCard("grunt");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		var hammer = scn.GetShadowCard("hammer");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var scimitar = scn.GetShadowCard("scimitar");
		var orkishax = scn.GetShadowCard("orkishax");
		// An [orc] weapon in the other player's discard pile, of a title in none of the Shadow player's piles
		var mordorscimitar = scn.GetFreepsCard("mordorscimitar");
		scn.MoveCardsToHand(goblins, orkishax);
		scn.MoveCardsToDiscard(spear, hammer, gauntlets, scimitar, mordorscimitar);
		assertInHand(goblins, orkishax);
		assertInZone(Zone.DISCARD, spear, hammer, gauntlets, scimitar, mordorscimitar);

		scn.StartGame();
		// The Goblin Runner is the [moria] card the Scimitar's play needs.
		scn.MoveMinionsToTable(entrapping, grunt, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(goblins);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblins));
		scn.ShadowAcceptOptionalTrigger();

		// Two [orc] weapons from his own discard pile; not the [orc] armor, the [moria] weapon an Orc may bear, the
		// [orc] weapon in his hand or the one in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, hammer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gauntlets, scimitar, orkishax, mordorscimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(spear);

		// The Spear may be borne by any [orc] minion: the two [orc] Orcs are offered, the [orc] Troll is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblins, entrapping));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grunt, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(entrapping);

		assertAttachedTo(spear, entrapping);
		assertEquals(0, scn.GetAttachedCards(goblins).size());
		assertEquals(0, scn.GetAttachedCards(grunt).size());
		assertInZone(Zone.DISCARD, hammer, gauntlets, scimitar, mordorscimitar);
		assertInHand(orkishax);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheWeaponInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblins = scn.GetShadowCard("goblins");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(goblins);
		scn.MoveCardsToDiscard(spear);
		assertInZone(Zone.DISCARD, spear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(goblins);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblins));

		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, spear);
		assertEquals(0, scn.GetAttachedCards(goblins).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcOrcPlayedIsNoCauseAndAGoblinsInPlayDoesNotAnswerTheSecondCopysPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblins = scn.GetShadowCard("goblins");
		var goblins2 = scn.GetShadowCard("goblins2");
		var entrapping = scn.GetShadowCard("entrapping");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(entrapping, goblins2);
		scn.MoveCardsToDiscard(spear);
		assertInHand(entrapping, goblins2);
		assertInZone(Zone.DISCARD, spear);

		scn.StartGame();
		scn.MoveMinionsToTable(goblins);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Nothing is offered for an [orc] Orc that is not this minion.
		scn.ShadowPlayCard(entrapping);
		assertInZone(Zone.SHADOW_CHARACTERS, entrapping);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Only the copy played makes the offer.
		scn.ShadowPlayCard(goblins2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(goblins2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(goblins));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolCannotPayForAnyWeaponInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblins = scn.GetShadowCard("goblins");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(goblins);
		scn.MoveCardsToDiscard(spear);
		assertInZone(Zone.DISCARD, spear);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for Ettenmoors: exactly the Goblins' 3 and its 2 roaming
		assertEquals(5, scn.GetTwilight());
		scn.ShadowPlayCard(goblins);

		// Nothing left for the Spear's 1: playing it is the effect's price, and the offer is not made.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
