package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strands", "9_22");
					// Gimli, Son of Glóin: [dwarven] Dwarf companion, cost 2; a skirmish ability used only on purpose
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability used only on purpose
					put("thrarin", "1_27");
					// Legolas, Greenleaf: [elven] Elf companion who is no Dwarf; an archery ability used only on purpose
					put("legolas", "1_50");
					// Final Count: another [elven] support-area condition, which gives itself [elven] tokens when
					// Legolas wins a skirmish (no skirmish here)
					put("count", "4_69");
					// Strength of Arms: an [elven] support-area condition to play after this card; its trigger answers
					// an Elf winning a skirmish, and its skirmish ability is used only on purpose
					put("arms", "4_82");
					// Dwarven Axe: a [dwarven] possession, for the top of a draw deck
					put("axe", "1_9");
					// Lórien Elf: an [elven] companion, for the top of a draw deck
					put("elf", "1_53");
					// Armor: a [gondor] possession, for the top of a draw deck
					put("armor", "1_92");
					// Goblin Patrol Troop: [moria] Orc with no text, so that there is a maneuver phase
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrandsofElvenHairStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Strands of Elven Hair
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: To play, spot a Dwarf.
		 * 	When you play this possession, add 2 [elven] tokens here.
		 * 	<b>Fellowship:</b> Discard this possession or remove an [elven] token from here to reveal the top card of your draw deck. If it is a [dwarven] or [elven] card, you may take it into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strands");

		assertEquals("Strands of Elven Hair", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilADwarfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetFreepsCard("strands");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(strands, gimli);

		scn.StartGame();

		// An Elf companion is no Dwarf.
		assertInHand(strands);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(strands));

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.FreepsPlayAvailable(strands));

		scn.FreepsPlayCard(strands);
		assertInZone(Zone.SUPPORT, strands);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedBesideADwarfAllyItAddsTwoTokensHereAndAnotherCardPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetFreepsCard("strands");
		var thrarin = scn.GetFreepsCard("thrarin");
		var count = scn.GetFreepsCard("count");
		var arms = scn.GetFreepsCard("arms");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(count);
		scn.MoveCardsToHand(strands, arms);

		scn.StartGame();
		scn.AddTokensToCard(count, 1);

		assertEquals(1, scn.GetCultureTokensOn(count));
		assertInHand(strands, arms);
		// The ally is the only Dwarf there is.
		assertTrue(scn.FreepsPlayAvailable(strands));

		scn.FreepsPlayCard(strands);
		// No "may": the tokens are added unasked, here and nowhere else.
		assertInZone(Zone.SUPPORT, strands);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another card played once this one is in play is not "this possession".
		scn.FreepsPlayCard(arms);
		assertInZone(Zone.SUPPORT, arms);
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(0, scn.GetCultureTokensOn(arms));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovingATokenRevealsTheTopCardAndADwarvenOrElvenCardMayBeTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetFreepsCard("strands");
		var gimli = scn.GetFreepsCard("gimli");
		var count = scn.GetFreepsCard("count");
		var axe = scn.GetFreepsCard("axe");
		var elf = scn.GetFreepsCard("elf");
		var shadowAxe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(strands, count);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(count, 1);
		// His deck: a [dwarven] possession on top, an [elven] companion beneath it.  The Shadow deck has a
		// [dwarven] card on top as well, which "your draw deck" leaves alone.
		scn.MoveCardsToTopOfDeck(elf, axe);
		scn.MoveCardsToTopOfDeck(shadowAxe);

		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertSame(elf, scn.GetFromTopOfFreepsDeck(2));
		assertSame(shadowAxe, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, axe, elf);
		assertInZone(Zone.SUPPORT, strands);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(strands));

		scn.FreepsUseCardAction(strands);
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this possession"));
		assertTrue(scn.FreepsChoiceAvailable("token from here"));
		scn.FreepsChooseOption("token from here");

		assertEquals(1, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(count));
		// The top card is shown to both players: listed, and not to be chosen.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, false));
		scn.DismissRevealedCards();

		// A [dwarven] card: he may take it.
		assertTrue(scn.FreepsDecisionAvailable("take the revealed card"));
		scn.FreepsChooseYes();
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(shadowAxe, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Used again, with the last token: an [elven] card of another type is taken as well.
		assertSame(elf, scn.GetFreepsTopOfDeck());
		scn.FreepsUseCardAction(strands);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("token from here");
		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertInZone(Zone.SUPPORT, strands);
		scn.DismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("take the revealed card"));
		scn.FreepsChooseYes();
		assertInHand(axe, elf);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTokenHereItIsDiscardedUnaskedAndACardOfAnotherCultureStaysOnTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetFreepsCard("strands");
		var gimli = scn.GetFreepsCard("gimli");
		var count = scn.GetFreepsCard("count");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(strands, count);

		scn.StartGame();
		scn.AddTokensToCard(count, 1);
		scn.MoveCardsToTopOfDeck(armor);

		assertEquals(0, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.SUPPORT, strands);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(strands));

		scn.FreepsUseCardAction(strands);
		// No token here (the one on Final Count is not "here"): only the discard can be paid, and nothing is asked.
		assertInDiscard(strands);
		assertEquals(1, scn.GetCultureTokensOn(count));
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// A [gondor] card: nothing is offered, and it stays where it was.
		assertFalse(scn.FreepsDecisionAvailable("take the revealed card"));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesTheRevealedCardOnTopOfTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetFreepsCard("strands");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToTopOfDeck(axe);

		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(strands);
		scn.FreepsChooseOption("token from here");
		assertEquals(1, scn.GetCultureTokensOn(strands));
		scn.DismissRevealedCards();

		assertTrue(scn.FreepsDecisionAvailable("take the revealed card"));
		scn.FreepsChooseNo();
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strands = scn.GetFreepsCard("strands");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(strands);

		scn.StartGame();
		scn.AddTokensToCard(strands, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(strands));
	}
}
