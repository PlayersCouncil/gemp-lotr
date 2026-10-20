package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("narya", "9_19");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("cirdan", "10_8");      // Cirdan, The Shipwright: [elven] Elf companion, vitality 4; a skirmish ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is no Cirdan
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("garrison", "6_52");    // Garrison of Osgiliath: [gondor] support-area condition that holds [gondor] tokens; a skirmish ability only
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession that holds [raider] tokens; Shadow and Regroup abilities used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaryaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Narya, Ring of Fire
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: 1
		 * Game Text: Bearer must be Círdan or Gandalf.<br><b>Fellowship:</b> Remove X culture tokens, where X is equal to bearer's vitality. Discard this artifact.
		*/

		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");

		assertEquals("Narya", narya.getBlueprint().getTitle());
		assertEquals("Ring of Fire", narya.getBlueprint().getSubtitle());
		assertTrue(narya.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, narya.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, narya.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, narya.getBlueprint().getCardType());
		assertTrue(narya.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, narya.getBlueprint().getTwilightCost());
		assertEquals(1, narya.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnCirdanOrGandalfAndOnNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var cirdan = scn.GetFreepsCard("cirdan");
		var arwen = scn.GetFreepsCard("arwen");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, cirdan, arwen, avenger);
		scn.MoveCardsToHand(narya);
		assertInHand(narya);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(narya));
		scn.FreepsPlayCard(narya);

		// Cirdan and Gandalf are offered; an Elf who is no Cirdan and a [gandalf] companion who is no Gandalf are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, cirdan));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, avenger, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(cirdan);

		assertAttachedTo(narya, cirdan);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNeitherCirdanNorGandalfAndGoesOnGandalfOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(arwen, avenger);
		scn.MoveCardsToHand(narya);
		assertInHand(narya);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(narya));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(narya));
		scn.FreepsPlayCard(narya);

		// Gandalf is the one bearer it can go on: nothing is asked.
		assertAttachedTo(narya, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionRemovesAsManyCultureTokensAsBearersVitalityAndDiscardsNarya() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var garrison = scn.GetFreepsCard("garrison");
		var ships = scn.GetShadowCard("ships");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToSupportArea(garrison);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		// Six culture tokens of two cultures, on a Free Peoples card and on the Shadow player's card.
		scn.AddTokensToCard(garrison, 3);
		scn.AddTokensToCard(ships, 3);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertAttachedTo(narya, gandalf);
		assertEquals(3, scn.GetCultureTokensOn(garrison));
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		// Gandalf's 4 and Narya's 1.
		assertEquals(5, scn.GetVitality(gandalf));

		assertTrue(scn.FreepsActionAvailable(narya));
		scn.FreepsUseCardAction(narya);

		// Each token is taken from a card the player chooses, a token of either culture and either player.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, ships));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, gandalf, narya));
		scn.FreepsChooseCard(ships);
		scn.FreepsChooseCard(ships);
		scn.FreepsChooseCard(garrison);
		scn.FreepsChooseCard(garrison);
		scn.FreepsChooseCard(garrison);

		// Five removed, one left; the wound and the burden are no culture tokens.
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(narya);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneWoundOnGandalfItRemovesFourCultureTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var garrison = scn.GetFreepsCard("garrison");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToSupportArea(garrison);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.AddTokensToCard(garrison, 3);
		scn.AddTokensToCard(ships, 3);
		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsRefreshDecision();

		assertAttachedTo(narya, gandalf);
		assertEquals(3, scn.GetCultureTokensOn(garrison));
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Each wound depletes his vitality by 1 (CR 4.2 "vitality"): 4 + 1 - 1.
		assertEquals(4, scn.GetVitality(gandalf));

		scn.FreepsUseCardAction(narya);
		scn.FreepsChooseCard(garrison);
		scn.FreepsChooseCard(garrison);
		scn.FreepsChooseCard(ships);
		scn.FreepsChooseCard(ships);

		// Four removed, two left.
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(narya);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCultureTokenInPlayTheActionOnlyDiscardsNarya() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var garrison = scn.GetFreepsCard("garrison");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToSupportArea(garrison);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();

		assertAttachedTo(narya, gandalf);
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(ships));

		// Its last sentence always does something, so the action is offered.
		assertTrue(scn.FreepsActionAvailable(narya));
		scn.FreepsUseCardAction(narya);

		assertInDiscard(narya);
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var garrison = scn.GetFreepsCard("garrison");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToSupportArea(garrison);

		scn.StartGame();
		scn.AddTokensToCard(garrison, 2);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertAttachedTo(narya, gandalf);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(narya));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
	}
}
