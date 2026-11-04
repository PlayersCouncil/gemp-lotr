package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chamber", "15_3");
					put("chamber2", "15_3");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion; a Skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a Fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability used only on purpose
					// From the Armory: another [dwarven] support-area condition, cost 0; a Skirmish ability only
					put("armory", "4_47");
					// For the top of a draw deck:
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession
					put("strike", "1_3");       // Axe Strike: a [dwarven] event
					put("armor", "1_92");       // Armor: a [gondor] possession
					put("elf", "1_53");         // Lórien Elf: an [elven] companion
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChamberofRecordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Chamber of Records
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this, you may spot a Dwarf to place 2 [dwarven] tokens here.<br><b>Fellowship:</b> Discard this from play or remove a [dwarven] token from here to reveal the top card of your draw deck. If it is a [dwarven] card, take it into your hand, otherwise, place it in your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chamber");

		assertEquals("Chamber of Records", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedBesideADwarfCompanionItMayPlaceTwoTokensOnThisCopyAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var chamber2 = scn.GetFreepsCard("chamber2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(chamber2);
		scn.MoveCardsToHand(chamber);

		scn.StartGame();
		scn.AddTokensToCard(chamber2, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetCultureTokensOn(chamber2));
		assertInHand(chamber);

		scn.FreepsPlayCard(chamber);

		// One offer, the copy just played; the copy already in play does not answer this play.
		assertInZone(Zone.SUPPORT, chamber);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chamber));
		scn.FreepsAcceptOptionalTrigger();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetCultureTokensOn(chamber2));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(chamber);

		scn.StartGame();

		scn.FreepsPlayCard(chamber);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chamber));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarfToSpotNoTokensAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(chamber);

		scn.StartGame();

		scn.FreepsPlayCard(chamber);

		// Aragorn is a companion and no Dwarf.
		assertInZone(Zone.SUPPORT, chamber);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chamber));
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(chamber);

		scn.StartGame();

		scn.FreepsPlayCard(chamber);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(chamber));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, chamber);
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherDwarvenConditionPlayedAfterItPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var gimli = scn.GetFreepsCard("gimli");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(chamber);
		scn.MoveCardsToHand(armory);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(chamber));
		scn.FreepsPlayCard(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(chamber));
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		assertEquals(0, scn.GetCultureTokensOn(armory));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachUseRevealsTheTopCardAndADwarvenCardOfEachKindIsTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var chamber2 = scn.GetFreepsCard("chamber2");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var strike = scn.GetFreepsCard("strike");
		var shadowAxe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(chamber, chamber2);

		scn.StartGame();
		scn.AddTokensToCard(chamber, 2);
		scn.AddTokensToCard(chamber2, 1);
		// His deck: a [dwarven] possession, a [dwarven] character and a [dwarven] event on top.  The Shadow deck has
		// a [dwarven] card on top as well, which "your draw deck" leaves alone.
		scn.MoveCardsToTopOfDeck(strike, grimir, axe);
		scn.MoveCardsToTopOfDeck(shadowAxe);
		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetCultureTokensOn(chamber2));
		assertSame(axe, scn.GetFreepsTopOfDeck());
		assertSame(grimir, scn.GetFromTopOfFreepsDeck(2));
		assertSame(strike, scn.GetFromTopOfFreepsDeck(3));
		assertSame(shadowAxe, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, axe, grimir, strike);
		assertInZone(Zone.SUPPORT, chamber, chamber2);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(chamber));
		scn.FreepsUseCardAction(chamber);

		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this"));
		assertTrue(scn.FreepsChoiceAvailable("token from here"));
		scn.FreepsChooseOption("token from here");
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(1, scn.GetCultureTokensOn(chamber2));

		// The top card is shown to both players: listed, and not to be chosen.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, false));
		scn.DismissRevealedCards();

		// A [dwarven] possession: into his hand, with nothing asked.
		assertInHand(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The last token here: a [dwarven] character is taken as well.
		scn.FreepsUseCardAction(chamber);
		scn.FreepsChooseOption("token from here");
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		scn.DismissRevealedCards();
		assertInHand(axe, grimir);
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// No token here, and the other copy's token is not "here": the discard is the one cost that can be paid,
		// and is paid unasked.  A [dwarven] event, neither character nor item, is taken too.
		assertInZone(Zone.SUPPORT, chamber);
		scn.FreepsUseCardAction(chamber);
		assertEquals(0, scn.FreepsGetMultipleChoiceCount());
		assertInDiscard(chamber);
		assertEquals(1, scn.GetCultureTokensOn(chamber2));
		assertInZone(Zone.SUPPORT, chamber2);
		scn.DismissRevealedCards();
		assertInHand(axe, grimir, strike);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertSame(shadowAxe, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfAnotherCultureIsPlacedInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(chamber);

		scn.StartGame();
		scn.AddTokensToCard(chamber, 2);
		// A [gondor] possession on top, an [elven] companion beneath it.
		scn.MoveCardsToTopOfDeck(elf, armor);
		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertSame(elf, scn.GetFromTopOfFreepsDeck(2));
		assertInZone(Zone.DECK, armor, elf);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(chamber);
		scn.FreepsChooseOption("token from here");
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		scn.DismissRevealedCards();

		// A possession like the Dwarven Axe, of another culture: to his discard pile.
		assertInDiscard(armor);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(chamber);
		scn.FreepsChooseOption("token from here");
		assertEquals(0, scn.GetCultureTokensOn(chamber));
		scn.DismissRevealedCards();

		assertInDiscard(armor, elf);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chamber = scn.GetFreepsCard("chamber");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(chamber);

		scn.StartGame();
		scn.AddTokensToCard(chamber, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertTrue(scn.FreepsActionAvailable(chamber));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(chamber));
		assertEquals(2, scn.GetCultureTokensOn(chamber));
	}
}
