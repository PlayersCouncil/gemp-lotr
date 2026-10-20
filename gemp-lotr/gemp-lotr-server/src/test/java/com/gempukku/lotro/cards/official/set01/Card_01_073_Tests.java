package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cart", "1_73");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("staff", "2_22");       // Gandalf's Staff: a Free Peoples artifact; bearer must be Gandalf; makes [gandalf] spells cheaper
					put("narya", "3_34");       // Narya: a Free Peoples artifact; bearer must be Gandalf; a start-of-turn trigger never reached here
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession; bearer must be a Hobbit; no other text
					put("sword2", "1_299");
					put("sword3", "1_299");
					put("toby", "1_305");       // Old Toby: a Free Peoples possession to the support area; when played, may draw a card
					put("scimitar", "1_180");   // Goblin Scimitar: a Shadow possession; when played, may draw a card
					put("balrogsword", "2_50"); // The Balrog's Sword: a Shadow artifact; bearer must be The Balrog
					put("smoke", "18_27");      // Ship of Smoke: a Free Peoples condition; answers a pipeweed played beside Gandalf
					put("shards", "3_44");      // The Shards of Narsil: a [gondor] artifact that stacks [gondor] cards from hand on itself
					put("aragornspipe", "1_91");// Aragorn's Pipe: a [gondor] possession, stacked on the Shards
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsCartStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gandalf's Cart
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Fellowship:</b> Stack a Free Peoples artifact (or possession) from hand on this card, or play a card stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cart");

		assertEquals("Gandalf's Cart", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StacksAFreePeoplesArtifactOrPossessionFromHandOnTheCart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var staff = scn.GetFreepsCard("staff");
		var narya = scn.GetFreepsCard("narya");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var scimitar = scn.GetFreepsCard("scimitar");
		var balrogsword = scn.GetFreepsCard("balrogsword");
		var smoke = scn.GetFreepsCard("smoke");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(cart);
		// Narya cannot be played with no Gandalf in play, so nothing stacked here can be played.
		scn.StackCardsOn(cart, narya);
		scn.AttachCardsTo(frodo, sword2);
		scn.MoveCardsToHand(staff, sword, scimitar, balrogsword, smoke);

		scn.StartGame();

		assertInHand(staff, sword, scimitar, balrogsword, smoke);
		assertAttachedTo(sword2, frodo);
		assertInZone(Zone.STACKED, narya);
		assertEquals(1, scn.GetStackedCards(cart).size());
		assertTrue(scn.FreepsActionAvailable(cart));

		// With nothing here to play, the stacking is done with no question.
		scn.FreepsUseCardAction(cart);
		// A Free Peoples artifact and a Free Peoples possession; not a Shadow possession, a Shadow artifact,
		// a Free Peoples condition, or a Free Peoples possession that is in play and not in hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(staff, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scimitar, balrogsword, smoke, sword2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(staff);

		assertInZone(Zone.STACKED, staff, narya);
		assertTrue(scn.GetStackedCards(cart).contains(staff));
		assertEquals(2, scn.GetStackedCards(cart).size());
		assertInHand(sword, scimitar, balrogsword, smoke);
		assertAttachedTo(sword2, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysACardStackedHereAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var toby = scn.GetFreepsCard("toby");
		var sword = scn.GetFreepsCard("sword");
		var shards = scn.GetFreepsCard("shards");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(cart, shards);
		// Frodo is the one Hobbit and already bears a hand weapon, so the Hobbit Sword stacked here has no bearer
		// to be played on.
		scn.AttachCardsTo(frodo, sword2);
		scn.StackCardsOn(cart, staff, toby, sword3);
		scn.StackCardsOn(shards, aragornspipe);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertInZone(Zone.STACKED, staff, toby, sword3, aragornspipe);
		assertInHand(sword);
		assertTrue(scn.FreepsActionAvailable(cart));

		// A card in hand to stack and cards here to play: the player is asked which.
		scn.FreepsUseCardAction(cart);
		assertTrue(scn.FreepsChoiceAvailable("Stack"));
		assertTrue(scn.FreepsChoiceAvailable("Play"));
		scn.FreepsChoose("Play a card stacked here");

		// An artifact and a possession stacked here that can be played; not the card stacked on the Shards, nor
		// the Hobbit Sword stacked here that has no bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(staff, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragornspipe, sword3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(toby);

		assertInZone(Zone.SUPPORT, toby);
		// Played as if from hand, so its own "when you play" trigger answers.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.STACKED, staff, sword3, aragornspipe);
		assertEquals(2, scn.GetStackedCards(cart).size());
		assertInHand(sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StacksAnArtifactAloneAndThenAPossessionAloneWhenNothingHereCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var narya = scn.GetFreepsCard("narya");
		var staff = scn.GetFreepsCard("staff");
		var sword = scn.GetFreepsCard("sword");
		var scimitar = scn.GetFreepsCard("scimitar");
		var smoke = scn.GetFreepsCard("smoke");
		scn.MoveCardsToSupportArea(cart);
		// Narya cannot be played with no Gandalf in play, so the offer rests on what is in hand.
		scn.StackCardsOn(cart, narya);
		scn.MoveCardsToHand(staff, scimitar, smoke);

		scn.StartGame();

		assertInHand(staff, scimitar, smoke);
		assertInZone(Zone.STACKED, narya);
		assertEquals(1, scn.GetStackedCards(cart).size());
		assertTrue(scn.FreepsActionAvailable(cart));

		// The artifact is the one card in hand that can be stacked.
		scn.FreepsUseCardAction(cart);
		assertInZone(Zone.STACKED, staff, narya);
		assertEquals(2, scn.GetStackedCards(cart).size());
		assertInHand(scimitar, smoke);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// Neither Narya nor the Staff can be played without Gandalf, and nothing left in hand can be stacked.
		assertFalse(scn.FreepsActionAvailable(cart));

		// Now a possession is the one card in hand that can be stacked.
		scn.MoveCardsToHand(sword);
		scn.FreepsRefreshDecision();
		assertInHand(sword, scimitar, smoke);
		assertTrue(scn.FreepsActionAvailable(cart));
		scn.FreepsUseCardAction(cart);
		assertInZone(Zone.STACKED, sword, staff, narya);
		assertEquals(3, scn.GetStackedCards(cart).size());
		assertInHand(scimitar, smoke);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileNothingStackedHereCanBePlayedAndNothingInHandCanBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cart = scn.GetFreepsCard("cart");
		var gandalf = scn.GetFreepsCard("gandalf");
		var narya = scn.GetFreepsCard("narya");
		var smoke = scn.GetFreepsCard("smoke");
		var scimitar = scn.GetFreepsCard("scimitar");
		scn.MoveCardsToSupportArea(cart);
		scn.StackCardsOn(cart, narya);
		scn.MoveCardsToHand(smoke, scimitar, gandalf);

		scn.StartGame();

		// Narya needs Gandalf to bear it, and neither a Free Peoples condition nor a Shadow possession can be
		// stacked here.
		assertInZone(Zone.STACKED, narya);
		assertInHand(smoke, scimitar, gandalf);
		assertFalse(scn.FreepsActionAvailable(cart));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsActionAvailable(cart));

		// Nothing in hand can be stacked: Narya is played on Gandalf with no question.
		scn.FreepsUseCardAction(cart);
		assertAttachedTo(narya, gandalf);
		assertEquals(0, scn.GetStackedCards(cart).size());
		assertInHand(smoke, scimitar);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
