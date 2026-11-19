package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illnews", "18_70");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil, vitality 3
					put("ted", "18_75");        // Ted Sandyman: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("soldier", "4_220");    // Desert Soldier: a [raider] Man minion; his trigger is at the start of a skirmish
					put("worker", "15_135");    // Rohan Worker: a [rohan] follower; its texts answer a transfer and a skirmish won
					put("army", "18_47");       // Elendil's Army: a [gondor] follower whose Aid exerts a [gondor] companion
					put("saruman", "17_38");    // Saruman, Servant of Sauron: an [isengard] follower, the Shadow player's own
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally in the support area, no follower
					put("footman", "13_92");    // Gríma, Footman of Saruman: a [men] Gríma; his trigger answers a Free Peoples character killed
					put("galmod", "4_153");     // Gríma, Son of Galmód: an [isengard] Gríma; strength -1 for unbound companions bearing a Shadow condition
					put("grimasdagger", "17_44"); // Gríma's Dagger: a [men] hand weapon whose title begins with Gríma; its response cancels a skirmish event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IllNewsisanIllGuestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ill News is an Ill Guest
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Exert a [men] minion to discard a follower from play (if you can spot Gríma, also draw a card).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("illnews");

		assertEquals("Ill News is an Ill Guest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAMenMinionToDiscardAFollowerOfEitherPlayerAndWithoutGrimaDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illnews = scn.GetShadowCard("illnews");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var soldier = scn.GetShadowCard("soldier");
		var saruman = scn.GetShadowCard("saruman");
		var grimasdagger = scn.GetShadowCard("grimasdagger");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToSupportArea(worker, army, saruman);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(illnews);

		scn.StartGame();
		// Laid in the fellowship phase, to stand in the Shadow phase that follows.
		scn.MoveMinionsToTable(legion, ted, soldier);
		// The nearest miss of "Gríma": a card whose title begins with the name.
		scn.AttachCardsTo(legion, grimasdagger);
		assertAttachedTo(grimasdagger, legion);
		assertInZone(Zone.SUPPORT, worker, army, saruman, grimir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(ted));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowPlayAvailable(illnews));
		scn.ShadowPlayCard(illnews);

		// The cost: a [men] minion of either race, not a [raider] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);
		assertEquals(1, scn.GetWoundsOn(ted));

		// The target: a follower of either culture and either side, not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, army, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grimir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(army);

		assertInDiscard(army, illnews);
		assertInZone(Zone.SUPPORT, worker, saruman, grimir);
		// No Gríma to spot: the event has left the hand and nothing is drawn.
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMenGrimaToSpotAlsoDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illnews = scn.GetShadowCard("illnews");
		var legion = scn.GetShadowCard("legion");
		var footman = scn.GetShadowCard("footman");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCardsToSupportArea(worker);
		scn.MoveCardsToHand(illnews);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, footman);
		assertInZone(Zone.SUPPORT, worker);
		scn.FreepsPass();

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(illnews);
		// Gríma is a [men] minion too, and could pay.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, footman));
		scn.ShadowChooseCard(legion);
		// The one follower is discarded unasked.
		assertInDiscard(worker, illnews);

		// The event leaves the hand and a card is drawn: hand - 1 + 1.
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnIsengardGrimaToSpotAlsoDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illnews = scn.GetShadowCard("illnews");
		var legion = scn.GetShadowCard("legion");
		var galmod = scn.GetShadowCard("galmod");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCardsToSupportArea(worker);
		scn.MoveCardsToHand(illnews);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, galmod);
		assertInZone(Zone.SUPPORT, worker);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(legion));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(illnews);
		// Legion is the only [men] minion, and exerts unasked; so is the follower discarded.
		assertEquals(1, scn.GetWoundsOn(legion));
		assertInDiscard(worker, illnews);

		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyMenMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illnews = scn.GetShadowCard("illnews");
		var legion = scn.GetShadowCard("legion");
		var soldier = scn.GetShadowCard("soldier");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCardsToSupportArea(worker);
		scn.MoveCardsToHand(illnews);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, soldier);
		// Vitality 3 with 2 wounds: exhausted.  The [raider] Man beside him could exert, but is no [men] minion.
		scn.AddWoundsToChar(legion, 2);
		assertEquals(2, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SUPPORT, worker);
		assertInHand(illnews);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(illnews);
		assertFalse(scn.ShadowPlayAvailable(illnews));
	}

	@Test
	public void WithNoFollowerInPlayItIsPlayedAndTheExertionPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illnews = scn.GetShadowCard("illnews");
		var legion = scn.GetShadowCard("legion");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(illnews);

		scn.StartGame();
		scn.MoveMinionsToTable(legion);
		assertInZone(Zone.SUPPORT, grimir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(legion));
		assertTrue(scn.ShadowPlayAvailable(illnews));
		scn.ShadowPlayCard(illnews);

		assertEquals(1, scn.GetWoundsOn(legion));
		assertInDiscard(illnews);
		assertInZone(Zone.SUPPORT, grimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithGrimaToSpotAndNoFollowerToDiscardItStillDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illnews = scn.GetShadowCard("illnews");
		var legion = scn.GetShadowCard("legion");
		var footman = scn.GetShadowCard("footman");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(illnews);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, footman);
		assertInZone(Zone.SUPPORT, grimir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(legion));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowPlayAvailable(illnews));
		scn.ShadowPlayCard(illnews);
		scn.ShadowChooseCard(legion);
		assertEquals(1, scn.GetWoundsOn(legion));

		// Nothing to discard; the draw goes by the spot alone (CR 4.2 "effect": as much as can be done).
		assertInDiscard(illnews);
		assertInZone(Zone.SUPPORT, grimir);
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
