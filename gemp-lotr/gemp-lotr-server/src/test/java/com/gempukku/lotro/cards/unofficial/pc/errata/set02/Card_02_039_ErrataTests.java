package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_02_039_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beyond", "52_39");
					put("uruk", "1_151");    // Uruk Lieutenant
					put("helm", "1_15");     // Gimli's Helm
					put("armor", "1_8");     // Dwarven Armor
					put("shield", "1_107");  // Great Shield
					put("sword", "1_47");    // Glamdring (FP possession, not armor/helm/shield)
					put("hsword1", "1_299"); // Hobbit Sword (non-unique FP possession)
					put("hsword2", "1_299");
					put("hsword3", "1_299");
					put("hsword4", "1_299");

					put("runner", "1_178");  // Goblin Runner (filler shadow minion)
					put("demands1", "2_40"); // Demands of the Sackville-Bagginses (Isengard SA condition)
					put("demands2", "2_40");
					put("demands3", "2_40");
					put("demands4", "2_40");
					put("demands5", "2_40");
					put("demands6", "2_40");
					put("demands7", "2_40");

					put("guard1", "1_7");    // Dwarf Guard (for companion count)
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondtheHeightofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Beyond the Height of Men
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert an Uruk-hai to discard a Free Peoples armor, helm, or shield.
		 * Hinder up to 4 Free Peoples possessions if you can spot 6 companions or 8 [Isengard] cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beyond");

		assertEquals("Beyond the Height of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BeyondExertsUrukAndDiscardsOnlyArmorHelmOrShield() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		var helm = scn.GetFreepsCard("helm");
		var armor = scn.GetFreepsCard("armor");
		var shield = scn.GetFreepsCard("shield");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(beyond);
		scn.AttachCardsTo(frodo, helm, armor, shield, sword);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(beyond));
		scn.ShadowPlayCard(beyond);

		// Cost: exert an Uruk-hai (auto-selected, only 1)
		assertEquals(1, scn.GetWoundsOn(uruk));

		// Only the armor/helm/shield should be valid targets, not Glamdring
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowCanChooseCharacter(helm));
		assertTrue(scn.ShadowCanChooseCharacter(armor));
		assertTrue(scn.ShadowCanChooseCharacter(shield));
		assertFalse(scn.ShadowCanChooseCharacter(sword));

		scn.ShadowChooseCard(helm);
		assertInDiscard(helm);

		// No hinder prompt (only 1 companion, 1 Isengard card) -- event resolves
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BeyondDoesNotHinderWith5CompanionsAnd7IsengardCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		var helm = scn.GetFreepsCard("helm");
		var armor = scn.GetFreepsCard("armor");

		// 5 companions: Frodo + 4 Dwarf Guards
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4);

		// 7 Isengard cards: uruk + 6 Demands
		var demands1 = scn.GetShadowCard("demands1");
		var demands2 = scn.GetShadowCard("demands2");
		var demands3 = scn.GetShadowCard("demands3");
		var demands4 = scn.GetShadowCard("demands4");
		var demands5 = scn.GetShadowCard("demands5");
		var demands6 = scn.GetShadowCard("demands6");
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(demands1, demands2, demands3, demands4, demands5, demands6);
		scn.MoveCardsToHand(beyond);
		scn.AttachCardsTo(frodo, helm, armor);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(beyond);
		scn.ShadowChooseCard(helm);
		assertInDiscard(helm);

		// 5 companions (not 6) and 7 Isengard cards (not 8) -- no hinder
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BeyondHindersUpTo4PossessionsWith6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		var helm = scn.GetFreepsCard("helm");
		var sword = scn.GetFreepsCard("sword");
		var hsword1 = scn.GetFreepsCard("hsword1");
		var hsword2 = scn.GetFreepsCard("hsword2");
		var hsword3 = scn.GetFreepsCard("hsword3");
		var hsword4 = scn.GetFreepsCard("hsword4");

		// 6 companions: Frodo + 5 Dwarf Guards
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");
		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4, guard5);

		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(beyond);
		// helm + 5 other possessions; after helm is discarded, 5 remain (more than the max of 4)
		scn.AttachCardsTo(frodo, helm, sword, hsword1, hsword2, hsword3, hsword4);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(beyond);

		// Helm is the only armor/helm/shield, auto-discarded
		assertInDiscard(helm);

		// Should now get hinder count prompt with min 0, max 4
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		// Choose to hinder 2 possessions
		scn.ShadowChooseCards(sword, hsword1);

		assertTrue(scn.IsHindered(sword));
		assertTrue(scn.IsHindered(hsword1));
		assertFalse(scn.IsHindered(hsword2));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BeyondHindersUpTo4PossessionsWith8IsengardCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		var helm = scn.GetFreepsCard("helm");
		var sword = scn.GetFreepsCard("sword");
		var hsword1 = scn.GetFreepsCard("hsword1");
		var hsword2 = scn.GetFreepsCard("hsword2");

		// 8 Isengard cards: uruk + 7 Demands
		var demands1 = scn.GetShadowCard("demands1");
		var demands2 = scn.GetShadowCard("demands2");
		var demands3 = scn.GetShadowCard("demands3");
		var demands4 = scn.GetShadowCard("demands4");
		var demands5 = scn.GetShadowCard("demands5");
		var demands6 = scn.GetShadowCard("demands6");
		var demands7 = scn.GetShadowCard("demands7");
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(demands1, demands2, demands3, demands4, demands5, demands6, demands7);
		scn.MoveCardsToHand(beyond);
		scn.AttachCardsTo(frodo, helm, sword, hsword1, hsword2);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(beyond);

		// Helm is the only armor/helm/shield, auto-discarded
		assertInDiscard(helm);

		// Should get hinder prompt (3 possessions remain, so max is clamped to 3)
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		// Hinder just 1 to prove "up to" works
		scn.ShadowChooseCard(sword);

		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(hsword1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
