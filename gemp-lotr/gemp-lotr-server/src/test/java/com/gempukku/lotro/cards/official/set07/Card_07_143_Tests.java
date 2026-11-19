package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("footman", "7_143");
					put("footman2", "7_143");
					// Easterling Skirmisher: a [raider] Easterling Man; its only text is a "when you play" trigger
					put("skirmisher", "4_229");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl who is an Easterling; his texts need 3 and 5 burdens
					put("khamul", "103_61");
					// Desert Soldier: a [raider] Man who is a Southron and no Easterling; his text is a skirmish trigger
					put("soldier", "4_220");

					// For the hand: a Shadow minion, a Free Peoples possession and a Free Peoples event
					put("runner", "1_178");     // Goblin Runner
					put("armor", "1_8");        // Dwarven Armor
					put("axestrike", "1_3");    // Axe Strike
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingFootmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Footman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br><b>Maneuver:</b> If you cannot spot 2 burdens, spot another Easterling and discard a card from hand to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("footman");

		assertEquals("Easterling Footman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityDiscardsACardFromHandToAddABurdenUntilTwoBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var runner = scn.GetShadowCard("runner");
		var armor = scn.GetShadowCard("armor");
		var axestrike = scn.GetShadowCard("axestrike");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, skirmisher);
		scn.MoveCardsToHand(runner, armor, axestrike);
		assertInHand(runner, armor, axestrike);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowActionAvailable(footman));

		scn.ShadowUseCardAction(footman);

		// Any card from hand: a minion, a possession and an event, of either side.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, armor, axestrike));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);
		assertInDiscard(runner);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetBurdens());

		// With 1 burden he still cannot spot 2.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(footman));
		scn.ShadowUseCardAction(footman);
		scn.ShadowChooseCard(armor);
		assertInDiscard(armor);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetBurdens());

		// 2 burdens: no longer offered, with a card still in hand and the Skirmisher still there.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(footman));
		assertInHand(axestrike);
		assertInZone(Zone.SHADOW_CHARACTERS, skirmisher);
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, skirmisher);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(footman));
	}

	@Test
	public void AnotherEasterlingIsOneOtherThanHimselfAndNoOtherRaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var soldier = scn.GetShadowCard("soldier");
		var khamul = scn.GetShadowCard("khamul");
		var axestrike = scn.GetShadowCard("axestrike");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, soldier);
		scn.MoveCardsToHand(axestrike);
		assertInHand(axestrike);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Himself and a [raider] Man who is no Easterling: not offered.
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(footman));

		// An Easterling who is a Nazgûl and no Man.
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(footman));
		scn.ShadowUseCardAction(footman);
		assertInDiscard(axestrike);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherCopyOfThisMinionIsAnotherEasterling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var footman2 = scn.GetShadowCard("footman2");
		var axestrike = scn.GetShadowCard("axestrike");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, footman2);
		scn.MoveCardsToHand(axestrike);
		assertInHand(axestrike);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(footman));
		assertTrue(scn.ShadowActionAvailable(footman2));

		scn.ShadowUseCardAction(footman);
		assertInDiscard(axestrike);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
