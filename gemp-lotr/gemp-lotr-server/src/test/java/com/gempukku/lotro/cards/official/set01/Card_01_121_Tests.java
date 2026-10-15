package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bred", "1_121");
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; his skirmish ability is used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, damage +1 only
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, vitality 3; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BredforBattleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bred for Battle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert an Uruk-hai to make it strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bred");

		assertEquals("Bred for Battle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukHaiOfAnyCultureInOrOutOfTheSkirmishToMakeThatOneStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var bred = scn.GetShadowCard("bred");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(bred);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		// The Sentry and the Worker stay out of any skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.ShadowPlayAvailable(bred));

		scn.ShadowPlayCard(bred);
		// Uruk-hai of two cultures, in the skirmish and out of it; not the [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertInDiscard(bred);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(11, scn.GetStrength(sentry));
		// The Uruk-hai that was not exerted is not made stronger either.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsTheSkirmishItWasPlayedInAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var bred = scn.GetShadowCard("bred");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveCardsToHand(bred);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { boromir, savage },
				new PhysicalCardImpl[] { gimli, sentry }
		);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPlayCard(bred);
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// The next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(savage));

		scn.PassSkirmishActions();
		// 8 against 7: the Savage wins.
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage));
	}

	@Test
	public void CannotBePlayedWhenTheOnlyUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var bred = scn.GetShadowCard("bred");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(bred);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		// Vitality 3 with 2 wounds: exhausted. The Worker, no Uruk-hai, could still exert.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bred));
	}
}
