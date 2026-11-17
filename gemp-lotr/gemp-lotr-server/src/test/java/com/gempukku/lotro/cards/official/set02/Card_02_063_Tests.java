package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reinforcements", "2_63");
					put("reinforcements2", "2_63");
					// Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop", "1_177");
					// Cave Troll of Moria: [moria] Troll, vitality 4; damage +1, fierce, and only a to-play
					// requirement and a discount, which a cheat into play passes by
					put("troll", "1_165");
					// Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("soldier", "1_271");
					// Goblin Marksman: [moria] Orc archer, twilight 3; no other text
					put("marksman", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinReinforcementsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Reinforcements
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may heal a [moria] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reinforcements");

		assertEquals("Goblin Reinforcements", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedItMayHealAMoriaMinionOfEitherRaceAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reinforcements = scn.GetShadowCard("reinforcements");
		var reinforcements2 = scn.GetShadowCard("reinforcements2");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveCardsToHand(reinforcements);
		assertInHand(reinforcements);
		scn.FreepsPass(); // to site 2: the Shadow phase
		// A second copy already in play: only the copy played makes the offer.
		scn.MoveMinionsToTable(reinforcements2, troop, troll, soldier);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(troll, 1);
		scn.AddWoundsToChar(soldier, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(1, scn.GetWoundsOn(soldier));

		scn.ShadowPlayCard(reinforcements);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reinforcements));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(reinforcements2));
		scn.ShadowAcceptOptionalTrigger();

		// A [moria] Orc and a [moria] Troll are offered; a [sauron] Orc is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier));
		// The offer was the "may"; once it is taken, one minion is to be healed.
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		// One wound of two is healed.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWoundedMoriaMinionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reinforcements = scn.GetShadowCard("reinforcements");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveCardsToHand(reinforcements);
		assertInHand(reinforcements);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(troop, soldier);
		scn.AddWoundsToChar(soldier, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(soldier));

		// The only wounded minion is no [moria] minion, so the heal would do nothing.
		scn.ShadowPlayCard(reinforcements);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMoriaMinionPlayedAfterItDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reinforcements = scn.GetShadowCard("reinforcements");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCardsToHand(marksman);
		assertInHand(marksman);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(reinforcements, troop);
		scn.AddWoundsToChar(troop, 2);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(troop));

		scn.ShadowPlayCard(marksman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reinforcements = scn.GetShadowCard("reinforcements");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveCardsToHand(reinforcements);
		assertInHand(reinforcements);
		scn.FreepsPass(); // to site 2: the Shadow phase
		scn.MoveMinionsToTable(troop, troll);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(troll, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(troll));

		scn.ShadowPlayCard(reinforcements);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(reinforcements));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
