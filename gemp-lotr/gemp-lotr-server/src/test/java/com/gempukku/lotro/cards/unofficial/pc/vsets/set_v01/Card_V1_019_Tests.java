package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_019_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "101_19");

					// Elves of twilight cost 2 or less.
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: companion, cost 2
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: ally, cost 2
					put("lorien", "1_53");      // Lorien Elf: companion, cost 1; to play, spot an Elf
					put("legolas", "1_50");     // Legolas, Greenleaf: companion, cost 2
					put("orophin", "1_56");     // Orophin, Lorien Bowman: ally, cost 2
					// A second Lorien Elf, on the table: the Elf the first is played by spotting, and a
					// second companion for Aragorn's skirmish tests.  Strength 4, vitality 2, no text in play.
					put("lorien2", "1_53");

					put("galadriel", "1_45");   // Galadriel, Lady of Light: an Elf ally of cost 3
					put("bow", "1_41");         // Elven Bow: an [elven] possession of cost 1, borne by an Elf
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion of cost 2
					put("defiance", "1_37");    // Defiance: an [elven] skirmish event
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion

					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Aragorn, Estel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: When you play Aragorn (except in your starting fellowship), you may play an Elf with a
		 * twilight cost of 2 or less from your draw deck.
		 * Skirmish: Discard an [elven] card from hand to make Aragorn strength +2 or <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Estel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandAsACompanionForFourTwilightAndDecliningPlaysNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCardsToHand(aragorn);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("lorien2"));

		scn.StartGame();

		assertInHand(aragorn);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(aragorn));
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(aragorn);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(4, scn.GetTwilight());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, arwen, celeborn, lorien);
		// The draw deck was never looked through, so it is not shuffled.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAragornFromHandMayPlayAnElfOfCostTwoOrLessFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var lorien = scn.GetFreepsCard("lorien");
		var galadriel = scn.GetFreepsCard("galadriel");
		var bow = scn.GetFreepsCard("bow");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		// An Elf of cost 2 in hand and another in the discard pile; everything else is in the draw deck.
		scn.MoveCardsToHand(aragorn, legolas);
		scn.MoveCardsToDiscard(orophin);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("lorien2"));

		scn.StartGame();

		assertInZone(Zone.DECK, arwen, celeborn, lorien, galadriel, bow, gimli);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(aragorn);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// An Elf companion of cost 2, an Elf ally of cost 2 and an Elf companion of cost 1.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn, lorien));
		// An Elf of cost 3; an [elven] card of cost 1 that is no Elf; a companion of cost 2 that is no Elf;
		// an Elf of cost 2 in hand; an Elf of cost 2 in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel, bow, gimli, legolas, orophin));
		assertEquals(3, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCardBPFromSelection(lorien);
		assertInZone(Zone.FREE_CHARACTERS, lorien);
		// 4 for Aragorn + 1 for the Lorien Elf, who is played and so paid for
		assertEquals(5, scn.GetTwilight());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, arwen, celeborn);
		assertInHand(legolas);
		assertInDiscard(orophin);
		// The draw deck has been looked through, so it is shuffled, once.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyPlayedFromTheDrawDeckGoesToTheSupportAreaAndIsPaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		assertInZone(Zone.DECK, celeborn);

		scn.FreepsPlayCard(aragorn);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		scn.FreepsChooseCardBPFromSelection(celeborn);

		assertInZone(Zone.SUPPORT, celeborn);
		// 4 for Aragorn + 2 for Celeborn
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.DECK, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedInTheStartingFellowshipAragornOffersNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");

		// The game has not been started: both players have yet to choose their starting fellowships.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, aragorn, arwen, celeborn);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// Nothing is offered: with Aragorn's 4 spent, the game goes on to the other player's fellowship.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, arwen, celeborn);
	}

	@Test
	public void SkirmishActionDiscardsAnElvenCardFromHandToMakeAragornStrengthPlusTwoForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("lorien2");
		var defiance = scn.GetFreepsCard("defiance");
		var bow = scn.GetFreepsCard("bow");
		var boromir = scn.GetFreepsCard("boromir");
		var orophin = scn.GetFreepsCard("orophin");
		var raiders = scn.GetShadowCard("raiders");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, elf);
		// An [elven] event, an [elven] possession and a [gondor] companion in hand; an [elven] ally in the
		// discard pile.
		scn.MoveCardsToHand(defiance, bow, boromir);
		scn.MoveCardsToDiscard(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment action.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, raiders }, new PhysicalCardImpl[] { elf, runner });
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, bow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, orophin, elf));
		scn.FreepsChooseCard(defiance);
		assertInDiscard(defiance);
		assertInHand(bow, boromir);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsChoose("strength");
		assertEquals(10, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));

		// Aragorn's 10 beats the Raiding Party's 9, where his 8 would have lost to it.  With no damage bonus
		// the Raiding Party takes 1 wound.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(raiders));

		// The Lorien Elf's skirmish, later in the same turn: the +2 has ended with the skirmish it was made in.
		scn.FreepsResolveSkirmish(elf);
		assertEquals(8, scn.GetStrength(aragorn));

		// Aragorn need not be in the skirmish for the action to be used.  The Elven Bow is the one [elven]
		// card left in hand.
		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertInDiscard(bow);
		scn.FreepsChoose("strength");
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(boromir);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionCanInsteadMakeAragornDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("lorien2");
		var defiance = scn.GetFreepsCard("defiance");
		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, elf);
		// An [elven] event, an [elven] possession and an [elven] companion in hand.
		scn.MoveCardsToHand(defiance, bow, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, savage }, new PhysicalCardImpl[] { elf, runner });
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, bow, legolas));
		scn.FreepsChooseCard(bow);
		assertInDiscard(bow);
		assertInHand(defiance, legolas);

		scn.FreepsChoose("damage");
		assertTrue(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));

		// Aragorn's 8 beats the Uruk Savage's 5: 1 wound for losing + 1 for damage +1.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetWoundsOn(savage));

		// In the next skirmish of the turn the damage bonus is gone.
		scn.FreepsResolveSkirmish(elf);
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoElvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("lorien2");
		var boromir = scn.GetFreepsCard("boromir");
		var defiance = scn.GetFreepsCard("defiance");
		var savage = scn.GetShadowCard("savage");
		// A [gondor] card in hand; the [elven] cards are in the discard pile and on the table.
		scn.MoveCompanionsToTable(aragorn, elf);
		scn.MoveCardsToHand(boromir);
		scn.MoveCardsToDiscard(defiance);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
