package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizard", "103_40");
					put("saruman", "3_69");
					put("uruk1", "1_151");
					put("uruk2", "1_151");
					put("uruk3", "1_151");
					put("hollowing", "3_54");
					put("hollowing2", "3_54");

					put("aragorn", "1_89"); // Cost 4
					put("boromir", "1_97"); // Cost 3
					put("legolas", "1_50"); // Cost 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWizardKnowsBetterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: A Wizard Knows Better
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Restore 2 [Isengard] cards.  Then you may exert X [isengard] minions (or spot Saruman and spot X [isengard] minions) to hinder an unbound companion costing up to X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wizard");

		assertEquals("A Wizard Knows Better", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void AWizardKnowsBetterRestoresTwoIsengardCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var hollowing = scn.GetShadowCard("hollowing");
		var hollowing2 = scn.GetShadowCard("hollowing2");
		var uruk1 = scn.GetShadowCard("uruk1");

		scn.MoveCardsToHand(wizard);
		scn.MoveCardsToSupportArea(hollowing, hollowing2);
		scn.MoveMinionsToTable(uruk1);

		scn.StartGame();

		scn.HinderCard(hollowing);
		scn.HinderCard(hollowing2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);

		assertFalse(scn.IsHindered(hollowing));
		assertFalse(scn.IsHindered(hollowing2));
	}

	@Test
	public void AWizardKnowsBetterExertsMinionsToHinderCompanionByTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var uruk3 = scn.GetShadowCard("uruk3");
		var aragorn = scn.GetFreepsCard("aragorn"); // Cost 4
		var boromir = scn.GetFreepsCard("boromir"); // Cost 3
		var legolas = scn.GetFreepsCard("legolas"); // Cost 2

		scn.MoveCardsToHand(wizard);
		scn.MoveMinionsToTable(uruk1, uruk2, uruk3);
		scn.MoveCompanionsToTable(aragorn, boromir, legolas);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(uruk1, uruk2, uruk3);

		assertEquals(1, scn.GetWoundsOn(uruk1));
		assertEquals(1, scn.GetWoundsOn(uruk2));
		assertEquals(1, scn.GetWoundsOn(uruk3));

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(boromir, legolas));
		assertFalse(scn.ShadowHasCardChoicesAvailable(aragorn));
		scn.ShadowChooseCard(boromir);

		assertTrue(scn.IsHindered(boromir));
	}

	@Test
	public void AWizardKnowsBetterWithSarumanSpotsMinionsInsteadOfExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var saruman = scn.GetShadowCard("saruman");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var uruk3 = scn.GetShadowCard("uruk3");
		var boromir = scn.GetFreepsCard("boromir"); // Cost 3

		scn.MoveCardsToHand(wizard);
		scn.MoveMinionsToTable(saruman, uruk1, uruk2, uruk3);
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(uruk1, uruk2, uruk3);

		// Minions NOT exerted (spotting, not exerting)
		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(uruk2));
		assertEquals(0, scn.GetWoundsOn(uruk3));

		assertTrue(scn.IsHindered(boromir));
	}

	@Test
	public void AWizardKnowsBetterCanDeclineHinderingCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetShadowCard("wizard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToHand(wizard);
		scn.MoveMinionsToTable(uruk1);
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(wizard);
		scn.ShadowChooseNo();

		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertFalse(scn.IsHindered(boromir));
	}

}
