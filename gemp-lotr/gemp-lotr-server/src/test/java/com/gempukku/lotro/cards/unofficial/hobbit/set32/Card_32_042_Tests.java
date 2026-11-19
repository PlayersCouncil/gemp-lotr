package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_042_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes and
	// the regroup phase of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pursuer", "32_42");
					put("pursuer2", "32_42");

					// Vicious Warg: a mount borne by an Orc (strength +3, vitality +1, fierce); its play trigger
					// is never met (cheated on)
					put("vwarg", "32_32");
					// Goblin Scimitar: a hand weapon borne by an Orc, no mount; its play texts are never met (cheated on)
					put("scimitar", "31_31");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7; a maneuver ability not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion; a skirmish ability not used
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal and a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrkishPursuerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Orkish Pursuer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, you may exert him twice to add a doubt.
		 * <b>Regroup:</b> If this minion is not mounted, exert him to make the Free Peoples player wound a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pursuer");

		assertEquals("Orkish Pursuer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningPursuerMayExertTwiceToAddADoubtAndTheOtherCopyIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, pursuer);
		// The second copy is left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// 8 against 7: the Pursuer wins, and Balin takes a wound.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pursuer2));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningPursuerWhoCannotExertTwiceIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Vitality 3 with 1 wound: the Pursuer can exert once more, not twice.
		scn.AddWoundsToChar(pursuer, 1);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, pursuer);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PursuerLosingASkirmishIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Balin at 12, to beat the Pursuer's 8 without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, pursuer);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// The Pursuer takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedPursuerIsOfferedAgainForHisFierceWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var vwarg = scn.GetShadowCard("vwarg");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// The Warg makes the Pursuer fierce, strength 11 and vitality 4.
		scn.AttachCardsTo(pursuer, vwarg);
		assertAttachedTo(vwarg, pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, pursuer);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetBurdens());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(nori, pursuer);
		scn.FreepsResolveSkirmish(nori);
		assertEquals(0, scn.GetWoundsOn(nori));
		scn.PassFierceSkirmishActions();
		// 11 against 7: he wins again.
		assertEquals(1, scn.GetWoundsOn(nori));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionOfTheUnmountedPursuerMakesTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var scimitar = scn.GetShadowCard("scimitar");
		var vwarg = scn.GetShadowCard("vwarg");
		var balin = scn.GetFreepsCard("balin");
		var gloin = scn.GetFreepsCard("gloin");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, gloin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 3 with 2 wounds: Balin is exhausted, so a wound kills him where an exertion could not be made.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer, pursuer2);
		// A possession that is no mount on the first copy; a mount on the second.
		scn.AttachCardsTo(pursuer, scimitar);
		scn.AttachCardsTo(pursuer2, vwarg);
		assertAttachedTo(scimitar, pursuer);
		assertAttachedTo(vwarg, pursuer2);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		assertTrue(scn.ShadowActionAvailable(pursuer));
		assertFalse(scn.ShadowActionAvailable(pursuer2));
		scn.ShadowUseCardAction(pursuer);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		// The Free Peoples player chooses the companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, balin, gloin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);
		assertInZone(Zone.DEAD, balin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileThePursuerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(pursuer, 2);
		assertEquals(2, scn.GetWoundsOn(pursuer));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pursuer));
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(balin));
	}
}
