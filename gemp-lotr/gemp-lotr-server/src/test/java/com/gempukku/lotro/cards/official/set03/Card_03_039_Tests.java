package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "3_39");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, strength 7, vitality 3; no ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, strength 3; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1, on a [gondor] Man; lets the Merchant exert
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, it adds nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BanneroftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Banner of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Tale</b>.
		 * 	<b>Maneuver:</b> Exert a [gondor] companion to make that companion strength +3 and <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");

		assertEquals("Banner of the White Tree", banner.getBlueprint().getTitle());
		assertNull(banner.getBlueprint().getSubtitle());
		assertFalse(banner.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, banner.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, banner.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, banner.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(banner, Keyword.TALE));
		assertTrue(scn.HasTimeword(banner, Timeword.MANEUVER));
		assertEquals(0, banner.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGondorCompanionToMakeThatCompanionStrengthPlus3AndDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(banner);
		assertAttachedTo(narsil, merchant);
		assertInHand(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// The Merchant has vitality 2 with Narsil: he could exert, and is left out only for being an ally.
		assertEquals(2, scn.GetVitality(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(7, scn.GetStrength(boromir));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsPlayAvailable(banner));

		scn.FreepsPlayCard(banner);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		// That companion alone: Pippin and Aragorn are as they were.
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetStrength(pippin));
		assertFalse(scn.HasKeyword(pippin, Keyword.DEFENDER));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(banner);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still in force in a skirmish that follows one already resolved.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, runner1 }, new PhysicalCardImpl[] { boromir, runner2 });
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		// Gone at the first regroup decision.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
	}

	@Test
	public void CannotBePlayedWhenTheOnlyGondorCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(banner);
		assertAttachedTo(narsil, merchant);
		assertInHand(banner);

		scn.StartGame();
		// Boromir, vitality 3, exhausted; Frodo and the [gondor] ally beside him could exert.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetVitality(boromir));
		assertEquals(2, scn.GetVitality(merchant));
		assertEquals(4, scn.GetVitality(frodo));
		assertFalse(scn.FreepsPlayAvailable(banner));
		assertInHand(banner);
	}
}
