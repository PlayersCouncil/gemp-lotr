package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "6_55");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], vitality 4; a maneuver ability used only on purpose
					// Aragorn, Defender of Rohan: the Aragorn who is [rohan].  His text needs the Ring-bearer assigned.
					put("rohanaragorn", "17_93");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, vitality 2; "To play, spot a Dwarf."
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] companion, vitality 3; her text is about Nazgûl
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a [dwarven] ally, vitality 2; a maneuver ability never used

					// Stout and Strong: [dwarven] support-area condition that carries [dwarven] tokens; its trigger
					// answers a Dwarf winning a skirmish, and its skirmish ability is used only on purpose.
					put("stout", "4_57");
					// Garrison of Osgiliath: [gondor] support-area condition that carries [gondor] tokens; its
					// trigger answers its own play, and its skirmish ability is used only on purpose.
					put("garrison", "6_52");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofBarahirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Ring of Barahir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Game Text: Bearer must be Aragorn.
		 * 	At the start of each of your turns, you may spot a culture token to heal a companion of that culture.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Barahir", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAragornAndGoesOnHimOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(ring);

		scn.StartGame();

		// Boromir is a [gondor] Man, and no Aragorn.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ring));

		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(ring));

		scn.FreepsPlayCard(ring);
		assertAttachedTo(ring, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnTheAragornWhoIsRohan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(ring);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(ring));
		scn.FreepsPlayCard(ring);
		assertAttachedTo(ring, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheTurnHealsACompanionOfACultureThatHasAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var arwen = scn.GetFreepsCard("arwen");
		var thrarin = scn.GetFreepsCard("thrarin");
		var stout = scn.GetFreepsCard("stout");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, guard, arwen);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, ring);
		scn.MoveCardsToSupportArea(stout, garrison);
		scn.AddTokensToCard(stout, 1);
		scn.AddTokensToCard(garrison, 1);
		// Two wounds on Gimli, so that one heal is told from two.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(thrarin, 1);

		// The turn begins, and the game stops at the offer.
		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();

		// A [dwarven] and a [gondor] token can be spotted: the companions of either culture.  Not Arwen, of a
		// culture with no token, nor Thrarin, a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, aragorn, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		// The token is only spotted.
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoCultureTokenInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, ring);
		scn.MoveCardsToSupportArea(stout);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(stout));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoWoundedCompanionIsOfACultureThatHasAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, ring);
		scn.MoveCardsToSupportArea(stout);
		scn.AddTokensToCard(stout, 1);
		// A [dwarven] token; the wounded are a [gondor] companion and a [dwarven] ally.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(thrarin, 1);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedItHealsNobodyAndIsOfferedAgainAtTheStartOfYourNextTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, ring);
		scn.MoveCardsToSupportArea(stout);
		scn.AddTokensToCard(stout, 1);
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn begins with his fellowship phase: nothing is offered and nobody is healed.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertEquals(2, scn.GetWoundsOn(gimli));

		// The owner's second turn: offered again, and taken.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsAcceptOptionalTrigger();

		// Gimli, the one wounded candidate, is healed of one wound without a choice being put.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
