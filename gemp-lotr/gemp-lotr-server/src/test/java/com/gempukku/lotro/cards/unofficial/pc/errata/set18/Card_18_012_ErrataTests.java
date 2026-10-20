package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_012_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gilgalad", "68_12");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; stronger only against a Nazgul
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2; an archery ability only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an [elven] Elf who is an ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("savage", "1_151");     // Uruk Savage: a minion, so that there is a maneuver phase

					put("defiance", "1_37");    // Defiance: [elven] Skirmish event
					put("defenses", "1_32");    // Border Defenses: [elven] Skirmish event
					put("swordwall", "4_84");   // Sword-wall: [elven] event printed "Skirmish or Regroup"
					put("valor", "4_87");       // Valor: [elven] Skirmish event, for the piles that are not this player's discard pile
					put("elfsong", "1_39");     // Elf-song: [elven] Fellowship event
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] Skirmish event

					put("supplies", "18_10");   // Elven Supplies: [elven] condition that holds [elven] tokens; fellowship and skirmish abilities only
					put("strands", "9_22");     // Strands of Elven Hair: [elven] possession that holds [elven] tokens; a fellowship ability only
					put("shake", "8_13");       // Shake Off the Shadow: [elven] condition that can hold [elven] tokens; fellowship and archery abilities only
					put("wish", "8_8");         // Wish For Our Kinfolk: [dwarven] condition that holds [dwarven] tokens; fellowship and skirmish abilities only
					put("wish2", "8_8");
					put("mallorn", "1_54");     // Mallorn-trees: [elven] condition
					put("future", "11_26");     // Uncertain Future: [elven] condition
					put("eyes", "1_43");        // Far-seeing Eyes: [elven] condition, for the piles that are not this player's discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GilgaladStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gil-galad, High King of the Noldor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: To play, spot 2 [elven] companions.
		 * 	<b>Maneuver:</b> Add a threat to take an [elven] skirmish event into hand from your discard pile.
		 * 	<b>Regroup:</b> Remove an [elven] token to take an [elven] condition into hand from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gilgalad");

		assertEquals("Gil-galad", card.getBlueprint().getTitle());
		assertEquals("High King of the Noldor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilTwoElvenCompanionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(gilgalad, legolas);

		scn.StartGame();

		// One [elven] companion, an [elven] ally and a [gondor] companion.
		assertFalse(scn.FreepsPlayAvailable(gilgalad));

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.FreepsPlayAvailable(gilgalad));

		scn.FreepsPlayCard(gilgalad);
		assertInZone(Zone.FREE_CHARACTERS, gilgalad);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityAddsAThreatToTakeAnElvenSkirmishEventIntoHandFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var defenses = scn.GetFreepsCard("defenses");
		var swordwall = scn.GetFreepsCard("swordwall");
		var elfsong = scn.GetFreepsCard("elfsong");
		var swordarm = scn.GetFreepsCard("swordarm");
		var legolas = scn.GetFreepsCard("legolas");
		var valor = scn.GetFreepsCard("valor");
		var opponentsValor = scn.GetShadowCard("valor");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gilgalad, arwen);
		// Three [elven] events with the Skirmish word, an [elven] Fellowship event, a [gondor] Skirmish event
		// and an [elven] card that is no event. Valor, an [elven] Skirmish event, is in this player's draw
		// deck and in the opponent's discard pile.
		scn.MoveCardsToDiscard(defiance, defenses, swordwall, elfsong, swordarm, legolas, opponentsValor);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, valor);
		assertTrue(scn.FreepsActionAvailable(gilgalad));

		scn.FreepsUseCardAction(gilgalad);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, defenses, swordwall));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elfsong, swordarm, legolas, valor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(defenses);
		assertInHand(defenses);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertInDiscard(defiance, swordwall, elfsong, swordarm, legolas);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoElvenSkirmishEventInTheDiscardPileTheThreatIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var arwen = scn.GetFreepsCard("arwen");
		var elfsong = scn.GetFreepsCard("elfsong");
		var swordarm = scn.GetFreepsCard("swordarm");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gilgalad, arwen);
		scn.MoveCardsToDiscard(elfsong, swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		// The ability has a cost, so it is offered whatever it will find.
		assertTrue(scn.FreepsActionAvailable(gilgalad));

		scn.FreepsUseCardAction(gilgalad);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInDiscard(elfsong, swordarm);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AtTheThreatLimitTheManeuverAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var defenses = scn.GetFreepsCard("defenses");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gilgalad, arwen);
		scn.MoveCardsToDiscard(defiance, defenses);

		scn.StartGame();
		// Frodo, Gil-galad and Arwen: no more than 3 threats.
		scn.AddThreats(2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(gilgalad));

		scn.FreepsUseCardAction(gilgalad);
		scn.FreepsChooseCards(defiance);
		assertEquals(3, scn.GetThreats());
		assertInHand(defiance);
		scn.ShadowPass();

		// Another [elven] skirmish event lies in the discard pile, and no threat can be added.
		assertInDiscard(defenses);
		assertFalse(scn.FreepsActionAvailable(gilgalad));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesAnElvenTokenToTakeAnElvenConditionIntoHandFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var supplies = scn.GetFreepsCard("supplies");
		var strands = scn.GetFreepsCard("strands");
		var shake = scn.GetFreepsCard("shake");
		var wish = scn.GetFreepsCard("wish");
		var mallorn = scn.GetFreepsCard("mallorn");
		var future = scn.GetFreepsCard("future");
		var wish2 = scn.GetFreepsCard("wish2");
		var legolas = scn.GetFreepsCard("legolas");
		var defiance = scn.GetFreepsCard("defiance");
		var eyes = scn.GetFreepsCard("eyes");
		var opponentsEyes = scn.GetShadowCard("eyes");
		scn.MoveCompanionsToTable(gilgalad);
		// A condition and a possession holding [elven] tokens, an [elven] condition holding none, and a
		// [dwarven] condition holding [dwarven] tokens.
		scn.MoveCardsToSupportArea(supplies, strands, shake, wish);
		scn.AddTokensToCard(supplies, 1);
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(wish, 2);
		// Two [elven] conditions, a [dwarven] condition, an [elven] companion and an [elven] event. Far-seeing
		// Eyes, an [elven] condition, is in this player's draw deck and in the opponent's discard pile.
		scn.MoveCardsToDiscard(mallorn, future, wish2, legolas, defiance, opponentsEyes);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, eyes);
		assertTrue(scn.FreepsActionAvailable(gilgalad));

		scn.FreepsUseCardAction(gilgalad);
		assertTrue(scn.FreepsHasCardChoiceAvailable(supplies, strands));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shake, wish));
		scn.FreepsChooseCard(strands);
		assertEquals(1, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(2, scn.GetCultureTokensOn(wish));

		assertTrue(scn.FreepsHasCardChoiceAvailable(mallorn, future));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wish2, legolas, defiance, eyes));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(future);
		assertInHand(future);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertInDiscard(mallorn, wish2, legolas, defiance);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoElvenConditionInTheDiscardPileTheTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var supplies = scn.GetFreepsCard("supplies");
		var wish2 = scn.GetFreepsCard("wish2");
		var legolas = scn.GetFreepsCard("legolas");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(gilgalad);
		scn.MoveCardsToSupportArea(supplies);
		scn.AddTokensToCard(supplies, 2);
		scn.MoveCardsToDiscard(wish2, legolas, defiance);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The ability has a cost, so it is offered whatever it will find.
		assertTrue(scn.FreepsActionAvailable(gilgalad));

		scn.FreepsUseCardAction(gilgalad);
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInDiscard(wish2, legolas, defiance);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoElvenTokenTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var supplies = scn.GetFreepsCard("supplies");
		var shake = scn.GetFreepsCard("shake");
		var wish = scn.GetFreepsCard("wish");
		var mallorn = scn.GetFreepsCard("mallorn");
		var future = scn.GetFreepsCard("future");
		scn.MoveCompanionsToTable(gilgalad);
		scn.MoveCardsToSupportArea(supplies, shake, wish);
		scn.AddTokensToCard(supplies, 1);
		scn.AddTokensToCard(wish, 2);
		scn.MoveCardsToDiscard(mallorn, future);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(gilgalad));

		// The one [elven] token is the one removed.
		scn.FreepsUseCardAction(gilgalad);
		assertEquals(0, scn.GetCultureTokensOn(supplies));
		assertEquals(2, scn.GetCultureTokensOn(wish));
		scn.FreepsChooseCards(mallorn);
		assertInHand(mallorn);
		scn.ShadowPass();

		// Another [elven] condition lies in the discard pile and two [dwarven] tokens on the table, and no
		// [elven] token is left to remove.
		assertInDiscard(future);
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertFalse(scn.FreepsActionAvailable(gilgalad));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
