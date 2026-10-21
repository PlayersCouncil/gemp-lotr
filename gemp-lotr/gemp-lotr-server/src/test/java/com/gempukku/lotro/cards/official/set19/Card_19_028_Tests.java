package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_028_Tests
{

	// Frodo, Little Master is the Ring-bearer in every scenario: no other Frodo can stand beside him.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three companions beside Frodo, so that the dead pile can hold four threats.
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6; a skirmish ability only
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: strength 6; her text is about skirmishing a fierce minion
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability only
					// Tower of Barad-dur: [sauron] condition.  "Each time the fellowship moves, the Free Peoples player must
					// discard a card from hand": a Shadow card that is no character and no item.
					put("tower", "3_104");
					// Uruk Rager: [isengard] Uruk-hai, strength 9, damage +1.  "Each time this minion wins a skirmish, the
					// Free Peoples player must discard the top 2 cards of his or her draw deck": a Shadow character.
					put("rager", "1_150");
					// Uruk-hai Sword: [isengard] hand weapon, strength +2, on an Uruk-hai.  "Each time bearer wins a
					// skirmish, the Free Peoples player must discard the top card of his or her draw deck": a Shadow item.
					put("sword", "1_160");
					// Goblin Runners: cards for the hand, never played
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				"19_28",
				VirtualTableScenario.RulingRing
		);
	}

	// The same, with Midgewater Moors as site 2: "Each time a minion is played, the Free Peoples player discards a
	// card from hand", a discard made by a site, which is no Shadow card.
	protected VirtualTableScenario GetMoorsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("arwen", "7_16");
					put("boromir", "1_97");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, cost 1; "When you play this minion, you may add (2)"
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_333");
				}},
				"19_28",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Frodo, Little Master
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Ring-bearer: True
		 * Game Text: <b>Ring-bearer.</b> <b>Ring-bound</b>. <b>Fellowship</b>.<br>While you cannot spot 4 threats, Shadow cards cannot discard cards from your hand or from the top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Little Master", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BEARER));
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertTrue(card.getBlueprint().canStartWithRing());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
	}

	@Test
	public void WithThreeThreatsAShadowConditionCannotMakeYouDiscardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var spare1 = scn.GetFreepsCard("runner1");
		var spare2 = scn.GetFreepsCard("runner2");
		var tower = scn.GetShadowCard("tower");
		scn.MoveCompanionsToTable(gimli, arwen, boromir);
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(spare1, spare2);

		scn.StartGame();
		// Three threats, one short of four; a burden beside them is no threat.
		scn.AddThreats(3);
		scn.AddBurdens(1);
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, tower);
		assertInHand(spare1, spare2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The move sets off the Tower: no card can be discarded, so none is asked for.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(spare1, spare2);
		assertEquals(0, scn.GetFreepsDiscardCount());
	}

	@Test
	public void WithFourThreatsAShadowConditionMakesYouDiscardAChosenCardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var spare1 = scn.GetFreepsCard("runner1");
		var spare2 = scn.GetFreepsCard("runner2");
		var tower = scn.GetShadowCard("tower");
		scn.MoveCompanionsToTable(gimli, arwen, boromir);
		scn.MoveCardsToSupportArea(tower);
		scn.MoveCardsToHand(spare1, spare2);

		scn.StartGame();
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.SUPPORT, tower);
		assertInHand(spare1, spare2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The move sets off the Tower, and the Free Peoples player chooses which card he discards.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(spare1, spare2));
		scn.FreepsChooseCard(spare1);
		assertInZone(Zone.DISCARD, spare1);
		assertInHand(spare2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// The Rager, with the Sword, is strength 9 + 2 against Boromir's 7 and damage +1: it wins, and wounds him twice.
	@Test
	public void WithThreeThreatsAShadowMinionAndAShadowPossessionDiscardNothingFromTheTopOfYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var rager = scn.GetShadowCard("rager");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(gimli, arwen, boromir);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.FreepsPass();
		scn.MoveMinionsToTable(rager);
		scn.AttachCardsTo(rager, sword);
		assertAttachedTo(sword, rager);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, rager);
		scn.FreepsResolveSkirmish(boromir);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 3);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.PassSkirmishActions();
		// The wound for losing and the two triggers come at the same moment.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk-hai Sword");
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk Rager");

		// Boromir lost: the Rager and the Sword's bearer have won, and the top of the deck is as it was.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetThreats());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourThreatsAShadowMinionAndAShadowPossessionDiscardFromTheTopOfYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var rager = scn.GetShadowCard("rager");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(gimli, arwen, boromir);

		scn.StartGame();
		scn.AddThreats(4);
		assertEquals(4, scn.GetThreats());
		scn.FreepsPass();
		scn.MoveMinionsToTable(rager);
		scn.AttachCardsTo(rager, sword);
		assertAttachedTo(sword, rager);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, rager);
		scn.FreepsResolveSkirmish(boromir);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 3);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.PassSkirmishActions();
		// The wound for losing and the two triggers come at the same moment.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk-hai Sword");
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk Rager");

		// Boromir lost: 2 cards for the Rager and 1 for the Sword come off the top of the deck.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeThreatsASiteStillMakesYouDiscardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMoorsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var spare1 = scn.GetFreepsCard("runner1");
		var spare2 = scn.GetFreepsCard("runner2");
		var runner = scn.GetShadowCard("runner3");
		scn.MoveCompanionsToTable(gimli, arwen, boromir);
		scn.MoveCardsToHand(spare1, spare2, runner);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertInHand(spare1, spare2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals("Midgewater Moors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion is played, and the site makes the Free Peoples player discard: a site is no Shadow card.
		// The Runner's own "add (2)" is required as built (F-0009), and the Free Peoples player orders the two.
		scn.ShadowPlayCard(runner);
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Midgewater Moors");
		assertTrue(scn.FreepsHasCardChoiceAvailable(spare1, spare2));
		scn.FreepsChooseCard(spare1);
		assertInZone(Zone.DISCARD, spare1);
		assertInHand(spare2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
