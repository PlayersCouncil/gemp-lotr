package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_029_Tests
{

	// The Fellowship path, with Anduin Banks 7_341 laid as site 4 in place of Balin's Tomb for the rules' loop.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "5_29");
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are only about unbound
					// Hobbits, and none is in play.
					put("pursuer", "4_190");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					// Anduin Banks: "Skirmish: Spot your minion and remove a burden to make that minion strength +2."
					put("site4", "7_341");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Sméagol, Slinker
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype:
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Ring-bound</b>. To play, add a burden.
		 * 		<b>Skirmish:</b> Add a burden to make Sméagol strength +2 and take no wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Sméagol", card.getBlueprint().getTitle());
		assertEquals("Slinker", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		// CR 4.2 "Gollum/Sméagol": he has no race.
		assertNull(card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayingSmeagolAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(smeagol));

		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsABurdenForStrengthPlusTwoAndNoWoundsForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		// The phase before his skirmish: no skirmish ability is offered there.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(smeagol));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);

		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 5 against 5: a tie, which the minion wins.  The losing Sméagol takes no wound.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetBurdens());

		// The fierce minion fights him again in a skirmish phase of its own: the bonus and the bar are gone.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(smeagol));

		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedInAnotherCompanionsSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);

		// The text names no skirmish of his own: Sméagol, who is not skirmishing, may still use it.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);

		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtAnduinBanksTheBurdenLoopRunsAsManyMoreTimesAsTheShadowPlayerAgrees() throws DecisionResultInvalidException, CardNotFoundException {
		// CR 4.2 "loops": Sméagol skirmishing a minion at Anduin Banks.  Each use of his ability adds the burden the
		// next use of the site's ability removes.  After two rounds, the board as it was, the loop is found: the Free
		// Peoples player names how many more times it runs and the Shadow player agrees, "with the Shadow player
		// taking the final action" (Q-0329).  The loop is run, and each ability is barred for the rest of the
		// skirmish.
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(5, scn.GetStrength(pursuer));

		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(smeagol));

		assertTrue(scn.ShadowActionAvailable(banks));
		scn.ShadowUseCardAction(banks);
		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(pursuer));

		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(smeagol));

		assertTrue(scn.ShadowActionAvailable(banks));
		scn.ShadowUseCardAction(banks);
		assertEquals(0, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(pursuer));

		// He would be offered his ability a third time: the loop is found, and put to the players.
		assertFalse(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsDecisionAvailable("<div class='cardHint' value='5_29'>·Sméagol, Slinker</div> and <div class='cardHint' value='7_341'>Anduin Banks</div> can be used forever."));
		scn.FreepsNameLoopIterations(3);
		assertTrue(scn.ShadowDecisionAvailable("the loop between <div class='cardHint' value='5_29'>·Sméagol, Slinker</div> and <div class='cardHint' value='7_341'>Anduin Banks</div> will be performed <b>3</b> more times."));
		scn.ShadowAgreeToLoop();

		// Three rounds, the site's action last: Sméagol 7 + 6, the Pursuer 9 + 6, no burden.  The Free Peoples
		// player acts next, with Sméagol's ability barred.
		assertEquals(0, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(smeagol));
		assertEquals(15, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionLoopBanned(smeagol));
		scn.PassSkirmishActions();

		// 13 against 15: the minion wins, and Sméagol takes no wound.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtAnduinBanksALowerNumberGivesSmeagolTheLastActionAndLeavesHisBurden() throws DecisionResultInvalidException, CardNotFoundException {
		// CR 4.2 "loops": the Shadow player "may name a lower number of iterations ... with the Free Peoples player
		// taking the final action", so the loop ends on Sméagol's ability, adding one action where the rounds end
		// on the site's (Q-0329).
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(pursuer);
		scn.FreepsPass();

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, pursuer);
		scn.FreepsResolveSkirmish(smeagol);

		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		scn.FreepsUseCardAction(smeagol);
		scn.ShadowUseCardAction(banks);
		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(smeagol));
		assertEquals(9, scn.GetStrength(pursuer));

		scn.FreepsNameLoopIterations(3);
		scn.ShadowLowerLoop(2);

		// Two rounds and Sméagol's ability once more: he is 7 + 6, the Pursuer 9 + 4, and his last burden stays.
		// The Shadow player acts next, with the site's ability barred.
		assertEquals(1, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(smeagol));
		assertEquals(13, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionLoopBanned(banks));
		scn.PassSkirmishActions();

		// 13 against 13: a tie the minion wins, and Sméagol takes no wound.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
