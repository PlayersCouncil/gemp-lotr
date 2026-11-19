package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "10_45");
					put("chieftain2", "10_45");
					// Mûmak: a [raider] mount, cost 2, strength +3; "Bearer must be a Southron."  Bearer is fierce.
					put("mumak", "5_73");
					put("warmumak", "5_78");    // War Mûmak: a [raider] mount, cost 3, on a Southron; fierce, an overwhelm trigger
					put("mumakil", "8_64");     // Mûmakil: a [raider] mount, cost 3, on a Southron; fierce, damage +1 at initiative or 4 threats
					put("spear", "4_255");      // Southron Spear: a [raider] hand weapon a Southron bears, no mount
					put("explorer", "4_250");   // Southron Explorer: another Southron, cost 2; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Mûmak Chieftain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>When you play this minion, you may play a mount on him from your discard pile. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chieftain");

		assertEquals("Mûmak Chieftain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AMountCostingTwoIsPlayedOnHimFromTheDiscardPileForNothingWithAnEmptyPool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToDiscard(mumak);
		assertInDiscard(mumak);

		scn.StartGame();
		// 4 set + 1 companion + 2 for the site make 7: the 5 he costs and 2 for roaming, a site 4 minion at site 2.
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(chieftain));
		scn.ShadowPlayCard(chieftain);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInDiscard(mumak);

		// The pool is empty and the mount costs 2 - 2: it is offered, and played for nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		assertAttachedTo(mumak, chieftain);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedHePlaysAMountOnHimselfFromYourDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var warmumak = scn.GetShadowCard("warmumak");
		var mumakil = scn.GetShadowCard("mumakil");
		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToDiscard(warmumak, mumakil, spear);
		// The Free Peoples player's own Mûmak, in his own discard pile: a title in none of the Shadow player's piles.
		var freepsMumak = scn.GetFreepsCard("mumak");
		scn.MoveCardsToDiscard(freepsMumak);
		assertInDiscard(warmumak, mumakil, spear, freepsMumak);

		scn.StartGame();
		// Another Southron who could bear a mount.
		scn.MoveMinionsToTable(explorer);
		// 6 set + 1 companion + 2 for the site make 9
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals(9, scn.GetTwilight());
		scn.ShadowPlayCard(chieftain);
		// 9 - 5 he costs - 2 for roaming
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(warmumak, mumakil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, freepsMumak));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(warmumak);

		// On him, with no choice of bearer though the Explorer could bear it.
		assertAttachedTo(warmumak, chieftain);
		assertInDiscard(mumakil, spear, freepsMumak);
		// 2 - (3 - 2) for the War Mûmak
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheMountInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var warmumak = scn.GetShadowCard("warmumak");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToDiscard(warmumak);
		assertInDiscard(warmumak);

		scn.StartGame();
		scn.SetTwilight(6);
		scn.FreepsPass();
		scn.ShadowPlayCard(chieftain);
		// 9 - 5 he costs - 2 for roaming
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(warmumak);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMountInTheDiscardPileNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(chieftain);
		// A possession he could bear, but no mount.
		scn.MoveCardsToDiscard(spear);
		assertInDiscard(spear);

		scn.StartGame();
		scn.SetTwilight(6);
		scn.FreepsPass();
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertInDiscard(spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTooLittleTwilightForTheMountEvenAtTwoLessNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var warmumak = scn.GetShadowCard("warmumak");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToDiscard(warmumak);
		assertInDiscard(warmumak);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();
		scn.ShadowPlayCard(chieftain);
		// 7 - 5 he costs - 2 for roaming leave nothing for the War Mûmak's 3 - 2.
		assertEquals(0, scn.GetTwilight());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertInDiscard(warmumak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherSouthronPlayedAfterHimIsGivenNoMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var explorer = scn.GetShadowCard("explorer");
		var warmumak = scn.GetShadowCard("warmumak");
		scn.MoveCardsToHand(explorer);
		scn.MoveCardsToDiscard(warmumak);
		assertInHand(explorer);
		assertInDiscard(warmumak);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(6);
		scn.FreepsPass();

		scn.ShadowPlayCard(explorer);
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertInDiscard(warmumak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithASecondChieftainInPlayOnlyTheOnePlayedTakesAMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		var warmumak = scn.GetShadowCard("warmumak");
		var mumakil = scn.GetShadowCard("mumakil");
		scn.MoveCardsToHand(chieftain);
		scn.MoveCardsToDiscard(warmumak, mumakil);
		assertInHand(chieftain);
		assertInDiscard(warmumak, mumakil);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain2);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain2);
		scn.SetTwilight(6);
		scn.FreepsPass();
		scn.ShadowPlayCard(chieftain);

		// Only the copy just played answers its own play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(chieftain));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(chieftain2));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCardBPFromSelection(warmumak);
		assertAttachedTo(warmumak, chieftain);
		assertEquals(0, scn.GetAttachedCards(chieftain2).size());
		assertInDiscard(mumakil);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
