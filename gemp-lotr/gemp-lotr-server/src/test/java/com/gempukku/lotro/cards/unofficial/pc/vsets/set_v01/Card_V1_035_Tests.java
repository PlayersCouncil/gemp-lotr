package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("darkness", "101_35");
					put("darkness2", "101_35");

					// The Balrog, Terror of Flame and Shadow: cost 14, or 11 while a [moria] minion can be spotted.
					// Unlike the Balrogs of set 2 it is not discarded away from an underground site and discards
					// nobody else.
					put("balrog", "6_76");
					// The Balrog, The Terror of Khazad-dum: another card of that title.  Its text is about wounds
					// and exertions at an underground site.
					put("balrog2", "12_79");

					// [moria] items.  Goblin Spear and Goblin Scimitar are possessions; The Balrog's Sword and Whip
					// of Many Thongs are artifacts.
					put("spear", "1_182");
					put("spear2", "1_182");
					put("scimitar", "1_180");
					put("sword", "2_50");
					put("whip", "2_74");

					// [moria] cards that are not items.  Goblin Runner: "When you play this minion, you may add (2)";
					// it is never played.  Goblin Patrol Troop has no text.
					put("runner", "1_178");
					put("troop", "1_177");

					// Uruk-hai Sword: an [isengard] possession.
					put("uruksword", "1_160");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheDarknessGrewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: The Darkness Grew
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Shadow: Stack a [moria] item from hand here.
		 * 	Shadow: Spot The Balrog to take a [moria] card stacked here into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("darkness");

		assertEquals("The Darkness Grew", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetShadowCard("darkness");
		scn.MoveCardsToHand(darkness);

		scn.StartGame();
		scn.FreepsPass();

		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, darkness);
		assertTrue(scn.ShadowPlayAvailable(darkness));

		scn.ShadowPlayCard(darkness);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, darkness);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StacksAChosenMoriaItemFromHandOnTheCopyThatWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetShadowCard("darkness");
		var darkness2 = scn.GetShadowCard("darkness2");
		var spear = scn.GetShadowCard("spear");
		var sword = scn.GetShadowCard("sword");
		var runner = scn.GetShadowCard("runner");
		var uruksword = scn.GetShadowCard("uruksword");
		var scimitar = scn.GetShadowCard("scimitar");
		var spear2 = scn.GetShadowCard("spear2");
		var troop = scn.GetShadowCard("troop");
		var whip = scn.GetShadowCard("whip");
		scn.MoveCardsToSupportArea(darkness, darkness2);
		// A [moria] card is stacked here already.  There is no Balrog, so it cannot be taken back.
		scn.StackCardsOn(darkness, whip);
		scn.MoveCardsToHand(spear, sword, runner, uruksword);
		scn.MoveCardsToDiscard(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear2);
		scn.FreepsPass();

		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(1, scn.GetStackedCards(darkness).size());
		assertEquals(0, scn.GetStackedCards(darkness2).size());
		assertTrue(scn.ShadowActionAvailable(darkness));

		scn.ShadowUseCardAction(darkness);

		// From hand: a [moria] possession and a [moria] artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, sword));
		// Not the [moria] minion or the [isengard] possession in hand, nor the [moria] items that are in the
		// discard pile, borne by a minion, and stacked here already.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, uruksword, scimitar, spear2, whip));

		scn.ShadowChooseCard(sword);

		assertInZone(Zone.STACKED, sword);
		assertEquals(2, scn.GetStackedCards(darkness).size());
		assertTrue(scn.GetStackedCards(darkness).contains(sword));
		assertEquals(0, scn.GetStackedCards(darkness2).size());
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, spear, runner, uruksword);
		// Stacking is not playing: the Sword's cost of 1 is not paid.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TakesAChosenMoriaCardStackedHereIntoHandWhileTheBalrogIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetShadowCard("darkness");
		var darkness2 = scn.GetShadowCard("darkness2");
		var balrog = scn.GetShadowCard("balrog");
		var spear = scn.GetShadowCard("spear");
		var sword = scn.GetShadowCard("sword");
		var runner = scn.GetShadowCard("runner");
		var uruksword = scn.GetShadowCard("uruksword");
		var scimitar = scn.GetShadowCard("scimitar");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(darkness, darkness2);
		// The text takes back "a [moria] card", not "a [moria] item": the Runner stands for a card that some
		// other effect stacked here.  The Uruk-hai Sword stands for one that is not [moria].
		scn.StackCardsOn(darkness, spear, sword, runner, uruksword);
		scn.StackCardsOn(darkness2, scimitar);
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(4, scn.GetStackedCards(darkness).size());
		assertEquals(1, scn.GetStackedCards(darkness2).size());
		assertTrue(scn.ShadowActionAvailable(darkness));

		scn.ShadowUseCardAction(darkness);

		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, sword, runner));
		// Not the [isengard] card stacked here, nor the [moria] cards stacked on the other copy and in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruksword, scimitar, troop));

		scn.ShadowChooseCard(runner);

		assertInZone(Zone.HAND, runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(3, scn.GetStackedCards(darkness).size());
		assertFalse(scn.GetStackedCards(darkness).contains(runner));
		assertEquals(1, scn.GetStackedCards(darkness2).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotTakeACardBackUntilTheBalrogIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetShadowCard("darkness");
		var balrog = scn.GetShadowCard("balrog");
		var balrog2 = scn.GetShadowCard("balrog2");
		var whip = scn.GetShadowCard("whip");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var uruksword = scn.GetShadowCard("uruksword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(darkness);
		scn.MoveCardsToHand(balrog, whip, spear, runner, uruksword);
		scn.MoveCardsToDiscard(balrog2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertEquals(0, scn.GetStackedCards(darkness).size());
		assertTrue(scn.ShadowActionAvailable(darkness));

		scn.ShadowUseCardAction(darkness);
		assertTrue(scn.ShadowHasCardChoiceAvailable(whip, spear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balrog, runner, uruksword));
		scn.ShadowChooseCard(whip);

		assertEquals(1, scn.GetStackedCards(darkness).size());
		assertInZone(Zone.STACKED, whip);

		// The Spear is the only item left in hand.
		scn.ShadowUseCardAction(darkness);

		assertEquals(2, scn.GetStackedCards(darkness).size());
		assertInZone(Zone.STACKED, spear);

		// Two [moria] cards are stacked here.  What is left in hand cannot be stacked: two [moria] minions and
		// an [isengard] possession.  And The Balrog is in hand and in the discard pile, not in play, where the
		// only [moria] minion is an Orc.
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(darkness));

		scn.ShadowPlayCard(balrog);

		// 14 to play - 3 for the [moria] minion it can spot + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertTrue(scn.ShadowActionAvailable(darkness));

		// With nothing in hand that can be stacked, using it is taking a card back.
		scn.ShadowUseCardAction(darkness);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(whip, spear));
		scn.ShadowChooseCard(whip);

		assertInZone(Zone.HAND, whip);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(1, scn.GetStackedCards(darkness).size());
		assertInZone(Zone.STACKED, spear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheBalrogInPlayAndItemsInHandEitherAbilityMayBeUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetShadowCard("darkness");
		var darkness2 = scn.GetShadowCard("darkness2");
		var balrog2 = scn.GetShadowCard("balrog2");
		var spear = scn.GetShadowCard("spear");
		var scimitar = scn.GetShadowCard("scimitar");
		var sword = scn.GetShadowCard("sword");
		var whip = scn.GetShadowCard("whip");
		scn.MoveCardsToSupportArea(darkness, darkness2);
		scn.StackCardsOn(darkness, sword);
		// The other copy has a card of its own throughout, which is nothing to this one.
		scn.StackCardsOn(darkness2, whip);
		scn.MoveCardsToHand(spear, scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog2);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetStackedCards(darkness).size());
		assertTrue(scn.ShadowActionAvailable(darkness));

		// The game offers the two abilities as one action and then asks which.
		scn.ShadowUseCardAction(darkness);
		assertTrue(scn.ShadowChoiceAvailable("Stack"));
		assertTrue(scn.ShadowChoiceAvailable("Take"));
		scn.ShadowChooseOption("Take");

		// The Sword is the only card stacked here.
		assertInZone(Zone.HAND, sword);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetStackedCards(darkness).size());

		// With nothing stacked here any longer, what is left is to stack.
		scn.ShadowUseCardAction(darkness);
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, scimitar, sword));
		scn.ShadowChooseCard(spear);

		assertInZone(Zone.STACKED, spear);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetStackedCards(darkness).size());

		// Both are possible again, and this time the Shadow player stacks.
		scn.ShadowUseCardAction(darkness);
		scn.ShadowChooseOption("Stack");
		assertTrue(scn.ShadowHasCardChoiceAvailable(scimitar, sword));
		scn.ShadowChooseCard(scimitar);

		assertInZone(Zone.STACKED, spear, scimitar);
		assertInZone(Zone.HAND, sword);
		assertEquals(2, scn.GetStackedCards(darkness).size());
		assertEquals(1, scn.GetStackedCards(darkness2).size());
		assertTrue(scn.GetStackedCards(darkness2).contains(whip));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NeitherAbilityIsOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkness = scn.GetShadowCard("darkness");
		var balrog = scn.GetShadowCard("balrog");
		var spear = scn.GetShadowCard("spear");
		var whip = scn.GetShadowCard("whip");
		scn.MoveCardsToSupportArea(darkness);
		scn.StackCardsOn(darkness, whip);
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.FreepsPass();

		// An item in hand to stack, and The Balrog in play with a [moria] card stacked here to take.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(darkness));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInZone(Zone.HAND, spear);
		assertEquals(1, scn.GetStackedCards(darkness).size());
		assertFalse(scn.ShadowActionAvailable(darkness));
	}
}
