package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illturn", "103_38");
					put("saruman", "3_69"); // Saruman, Servant of the Eye
					put("uruk1", "1_151");
					put("uruk2", "1_151");
					put("hollowing", "3_54"); // Shadow condition
					put("hollowing2", "3_54"); // Shadow condition

					put("gandalf", "1_72");
					put("aragorn", "1_89");
					put("sleep", "51_84"); // Sleep, Caradhras - discards/hinders conditions
					put("youcannotenter", "103_9"); // You Cannot Enter Here - hinders minions
					put("lordmoria", "1_21"); // Lord of Moria - FP condition
					put("lordmoria2", "1_21"); // Lord of Moria - FP condition
					put("leaving", "7_24"); // Leaving Forever - Discards a condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneIllTurnDeservesAnotherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: One Ill Turn Deserves Another
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [isengard] minions (or Saruman).
		* 	Each time your minion is hindered or discarded by a Free Peoples card, hinder a companion.
		* 	Each time your condition is hindered or discarded by a Free Peoples card, hinder a Free Peoples condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("illturn");

		assertEquals("One Ill Turn Deserves Another", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OneIllTurnCanPlayWithTwoIsengardMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.MoveCardsToHand(illturn);
		scn.MoveMinionsToTable(uruk1, uruk2);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(illturn));
		scn.ShadowPlayCard(illturn);

		assertInZone(Zone.SUPPORT, illturn);
	}

	@Test
	public void OneIllTurnCanPlayWithSaruman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCardsToHand(illturn);
		scn.MoveMinionsToTable(saruman);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(illturn));
		scn.ShadowPlayCard(illturn);

		assertInZone(Zone.SUPPORT, illturn);
	}

	@Test
	public void OneIllTurnCannotPlayWithoutSarumanOrTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var uruk1 = scn.GetShadowCard("uruk1");

		scn.MoveCardsToHand(illturn);
		scn.MoveMinionsToTable(uruk1);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(illturn));
	}

	@Test
	public void OneIllTurnHindersCompanionWhenMinionHinderedByFP() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var youcannotenter = scn.GetFreepsCard("youcannotenter");

		scn.MoveCardsToHand(youcannotenter);
		scn.MoveCardsToSupportArea(illturn);
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(gandalf));

		// Use You Cannot Enter Here to hinder a minion
		scn.FreepsPlayCard(youcannotenter);
		scn.FreepsChooseCard(uruk1);

		assertTrue(scn.IsHindered(uruk1));

		// One Ill Turn triggers - choose companion to hinder
		scn.ShadowChooseCard(gandalf);

		assertTrue(scn.IsHindered(gandalf));
	}

	@Test
	public void OneIllTurnHindersCompanionWhenOtherConditionHinderedByFP() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var uruk1 = scn.GetShadowCard("uruk1");
		var hollowing = scn.GetShadowCard("hollowing");
		var gandalf = scn.GetFreepsCard("gandalf");
		var youcannotenter = scn.GetFreepsCard("youcannotenter");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var lordmoria2 = scn.GetFreepsCard("lordmoria2");

		scn.MoveCardsToHand(youcannotenter);
		scn.MoveCardsToSupportArea(illturn, hollowing, lordmoria, lordmoria2);
		scn.MoveMinionsToTable(uruk1);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(gandalf));

		// Use You Cannot Enter Here to hinder another condition
		scn.FreepsPlayCard(youcannotenter);
		scn.FreepsChooseCard(hollowing);

		assertTrue(scn.IsHindered(hollowing));

		// One Ill Turn triggers - choose condition to hinder
		scn.ShadowChooseCard(lordmoria);

		assertTrue(scn.IsHindered(lordmoria));
	}

	@Test
	public void OneIllTurnHindersCompanionWhenSelfHinderedByFP() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var uruk1 = scn.GetShadowCard("uruk1");
		var hollowing = scn.GetShadowCard("hollowing");
		var gandalf = scn.GetFreepsCard("gandalf");
		var youcannotenter = scn.GetFreepsCard("youcannotenter");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var lordmoria2 = scn.GetFreepsCard("lordmoria2");

		scn.MoveCardsToHand(youcannotenter);
		scn.MoveCardsToSupportArea(illturn, hollowing, lordmoria, lordmoria2);
		scn.MoveMinionsToTable(uruk1);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(gandalf));

		// Use You Cannot Enter Here to hinder another condition
		scn.FreepsPlayCard(youcannotenter);
		scn.FreepsChooseCard(illturn);

		assertTrue(scn.IsHindered(illturn));

		// One Ill Turn triggers - choose condition to hinder
		scn.ShadowChooseCard(lordmoria);

		assertTrue(scn.IsHindered(lordmoria));
	}

	@Test
	public void OneIllTurnHindersFPConditionWhenOtherConditionDiscardedByFP() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var hollowing = scn.GetShadowCard("hollowing");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leaving = scn.GetFreepsCard("leaving");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var lordmoria2 = scn.GetFreepsCard("lordmoria2");

		scn.MoveCardsToSupportArea(illturn, hollowing, lordmoria, lordmoria2, leaving);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsChooseAny();
		scn.FreepsChoose("threat"); //Cost for Leaving Forever
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.IsHindered(lordmoria));

		// You Cannot Enter Here - discards 1
		scn.FreepsUseCardAction(leaving);
		scn.FreepsChoose("discard");
		scn.FreepsChooseCard(hollowing);

		// One Ill Turn triggers for condition discarded - hinder FP condition
		scn.ShadowChooseCard(lordmoria);
		assertTrue(scn.IsHindered(lordmoria));
	}

	@Test
	public void OneIllTurnHindersFPConditionWhenSelfDiscardedByFP() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illturn = scn.GetShadowCard("illturn");
		var hollowing = scn.GetShadowCard("hollowing");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leaving = scn.GetFreepsCard("leaving");
		var lordmoria = scn.GetFreepsCard("lordmoria");
		var lordmoria2 = scn.GetFreepsCard("lordmoria2");

		scn.MoveCardsToSupportArea(illturn, hollowing, lordmoria, lordmoria2, leaving);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsChooseAny();
		scn.FreepsChoose("threat"); //Cost for Leaving Forever
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.IsHindered(lordmoria));

		// You Cannot Enter Here - discards 1
		scn.FreepsUseCardAction(leaving);
		scn.FreepsChoose("discard");
		scn.FreepsChooseCard(illturn);

		// One Ill Turn triggers for condition discarded - hinder FP condition
		scn.ShadowChooseCard(lordmoria);
		assertTrue(scn.IsHindered(lordmoria));
	}

}
