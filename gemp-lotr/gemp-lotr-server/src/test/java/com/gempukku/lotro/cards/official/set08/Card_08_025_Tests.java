package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "8_25");
					put("web", "8_30");         // Web: [gollum] support-area possession, cost 1; a regroup ability stacks your Orc or [gollum] minion here
					put("web2", "8_30");
					put("poison", "8_28");      // Spider Poison: [gollum] support-area possession, cost 1; abilities used only on purpose
					put("fens", "5_22");        // Evil-smelling Fens: [gollum] support-area condition, cost 1 (no possession)
					put("armor", "17_41");      // Ceremonial Armor: [men] support-area possession, cost 2; stacks [men] minions discarded in the maneuver phase
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition; stacks a [sauron] Orc
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; a regroup ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its (2) answers only its own play
					put("runner2", "1_178");
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc
					put("column", "11_72");     // Column of Easterlings: [men] Man minion
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Shelob, Eater of Light
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: Fierce.
		 * 	When you play Shelob, you may play a [gollum] possession from your draw deck.
		 * 	Shelob is strength +3 for each minion stacked on a [gollum] possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shelob");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Eater of Light", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedSheMayPlayAGollumPossessionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var poison = scn.GetShadowCard("poison");
		var fens = scn.GetShadowCard("fens");
		var armor = scn.GetShadowCard("armor");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(shelob);

		scn.StartGame();
		// Enough for Shelob (6 + 2 roaming at Ettenmoors) and then any card in the deck.
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInZone(Zone.DECK, web, poison, fens, armor, gollum);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowPlayCard(shelob);

		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowAcceptOptionalTrigger();
		// The deck is shown first (pitfalls, "Searching a deck shows the deck first").
		scn.ShadowDismissRevealedCards();

		// A play from a draw deck is 0 to 1 (Q-0019).
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(web, poison));
		// A [gollum] condition, a possession of another culture, and a [gollum] minion are not offered,
		// though the pool could pay for each.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fens, armor, gollum));
		scn.ShadowChooseCardBPFromSelection(poison);

		assertInZone(Zone.SUPPORT, poison);
		assertInZone(Zone.DECK, web, fens, armor, gollum);
		// The deck is looked through, shuffled as the card leaves it, and shuffled again after (CR 4.2 "draw deck").
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsPlayedAndAMinionPlayedAfterHerDoesNotSetHerOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var poison = scn.GetShadowCard("poison");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(shelob, gollum);
		assertInZone(Zone.HAND, gollum);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInZone(Zone.DECK, poison);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(shelob);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DECK, poison);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [gollum] minion played after her: no offer.
		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoGollumPossessionInTheDrawDeckTheLookPlaysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var fens = scn.GetShadowCard("fens");
		var armor = scn.GetShadowCard("armor");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(shelob);

		scn.StartGame();
		scn.SetShadowDrawDeck(fens, armor, gollum);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInZone(Zone.DECK, fens, armor, gollum);
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(shelob);

		// A play from the draw deck is offered whatever the deck holds (Q-0019, Q-0104).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowAcceptOptionalTrigger();
		// The deck is shown with nothing in it that can be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(fens, false));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fens, armor, gollum));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DECK, fens, armor, gollum);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SheIsStrengthPlus3ForEachMinionStackedOnAGollumPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var web2 = scn.GetShadowCard("web2");
		var armor = scn.GetShadowCard("armor");
		var teeth = scn.GetShadowCard("teeth");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var gollum = scn.GetShadowCard("gollum");
		var assassin = scn.GetShadowCard("assassin");
		var column = scn.GetShadowCard("column");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(web, web2, armor, teeth);
		// Two minions on one Web; a minion on a possession of another culture, and one on a condition.
		scn.StackCardsOn(web, runner, runner2);
		scn.StackCardsOn(armor, column);
		scn.StackCardsOn(teeth, assassin);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, troop);

		assertEquals(2, scn.GetStackedCards(web).size());
		assertEquals(1, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(teeth).size());
		// 8 + 3 for each of the two minions on the Web
		assertEquals(14, scn.GetStrength(shelob));
		assertEquals(13, scn.GetStrength(troop));

		// A minion on the other Web counts too.
		scn.StackCardsOn(web2, gollum);
		assertEquals(1, scn.GetStackedCards(web2).size());
		// 8 + 3 x 3
		assertEquals(17, scn.GetStrength(shelob));
		// Another minion is left as it was.
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void AMinionStackedOnAWebByItsOwnAbilityMakesHerStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(web);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner);

		// Nobody is assigned in either round, so both minions are in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(web).size());
		assertEquals(8, scn.GetStrength(shelob));

		scn.ShadowUseCardAction(web);
		scn.ShadowChooseCard(runner);

		assertInZone(Zone.STACKED, runner);
		assertEquals(11, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
