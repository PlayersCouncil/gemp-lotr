package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_149_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("driven", "4_149");
					put("troop", "4_182");      // Uruk Crossbow Troop: [isengard] Uruk-hai archer; a "when you play" trigger only
					put("bowmen", "4_138");     // Band of Uruk Bowmen: [isengard] Uruk-hai archer, damage +1; +1 to the total only while its player controls a site
					put("crossbow", "4_183");   // Uruk Crossbowman: [isengard] Uruk-hai archer; archery ability: the Free Peoples player wounds an unbound companion
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer, Archer is its whole text
					put("lurtz", "18_118");     // Lurtz, Halfling Hunter: [uruk-hai] Uruk-hai archer; maneuver and assignment abilities used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no archer; damage +1 only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound companion, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, for the dead pile
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally, for the dead pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrivenBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Driven Back
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Spot 2 [isengard] archers to make the minion archery total +1 for each card in the dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven Back", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoIsengardArchersTheMinionArcheryTotalIsPlusOneForEachCardInTheDeadPileForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var troop = scn.GetShadowCard("troop");
		var bowmen = scn.GetShadowCard("bowmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(driven);
		assertInHand(driven);
		// A companion and an ally in the Free Peoples player's dead pile.
		scn.MoveCardsToDeadPile(gimli, maggot);
		assertEquals(2, scn.GetFreepsDeadCount());

		scn.StartGame();
		scn.MoveMinionsToTable(troop, bowmen);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertInHand(driven);
		// Two archers, each adding 1.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(driven));

		scn.ShadowPlayCard(driven);
		assertInZone(Zone.DISCARD, driven);
		// 2 archers + 2 cards in the dead pile
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Still in force as the archery wounds are placed: four of them.
		scn.PassArcheryActions();
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(boromir);
		scn.FreepsChooseCard(boromir);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// Gone with the archery phase: the two archers alone.
		assertEquals(2, scn.GetShadowArcheryTotal());
	}

	@Test
	public void CannotBePlayedWithOneIsengardArcherBesideArchersOfOtherCulturesAndAnIsengardMinionWhoIsNoArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(driven);
		assertInHand(driven);
		scn.MoveCardsToDeadPile(gimli);
		assertEquals(1, scn.GetFreepsDeadCount());

		scn.StartGame();
		scn.MoveMinionsToTable(troop, marksman, lurtz, savage);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertInHand(driven);
		// Three archers, but only the Crossbow Troop is an [isengard] one; the Savage is [isengard] and no archer.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertFalse(scn.ShadowPlayAvailable(driven));
	}

	@Test
	public void CountsOnlyTheFreePeoplesDeadPileAndNotTheShadowPlayersOrADiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var troop = scn.GetShadowCard("troop");
		var bowmen = scn.GetShadowCard("bowmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var shadowMaggot = scn.GetShadowCard("maggot");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(driven);
		// One card in the Free Peoples dead pile; the Shadow player's own Maggot in his own dead pile (Q-0131: "the
		// dead pile" is the Free Peoples player's); a companion in the Free Peoples discard pile.
		scn.MoveCardsToDeadPile(gimli, shadowMaggot);
		scn.MoveCardsToDiscard(boromir);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInZone(Zone.DEAD, shadowMaggot);
		assertInZone(Zone.DISCARD, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, bowmen);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(driven);
		// 2 archers + 1 card in the Free Peoples dead pile
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheDeadPileIsCountedAsTheEventIsPlayedSoACompanionKilledAfterItAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var troop = scn.GetShadowCard("troop");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(driven);
		scn.MoveCardsToDeadPile(gimli);
		assertEquals(1, scn.GetFreepsDeadCount());

		scn.StartGame();
		scn.MoveMinionsToTable(troop, crossbow);
		// Vitality 3 with 2 wounds: Boromir is exhausted, and a wound kills him.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPlayCard(driven);
		// 2 archers + 1 card in the dead pile
		assertEquals(3, scn.GetShadowArcheryTotal());

		scn.FreepsPass();
		scn.ShadowUseCardAction(crossbow);
		scn.FreepsChooseCard(boromir);
		assertInZone(Zone.DEAD, boromir);
		assertEquals(2, scn.GetFreepsDeadCount());
		// The Crossbowman drops out of the total for using his ability, and the dead pile is not counted again:
		// 3 - 1 = 2, where a count kept up to date would read 3.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
