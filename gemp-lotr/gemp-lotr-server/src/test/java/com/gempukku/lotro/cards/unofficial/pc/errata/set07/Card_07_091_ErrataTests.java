package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_091_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "57_91");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; text only with 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose

					put("horseman", "7_226");   // Enraged Horseman: [rohan] Man companion, cost 2; text only in a skirmish
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, cost 1; an archery ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion (a Man only with 3 [rohan] companions or Eowyn);
					// an optional trigger when another [rohan] Man exerts, and a skirmish ability used only on purpose
					put("merry", "103_85");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit companion, strength 3; text about tales
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a Hobbit ally (not unbound); a skirmish ability used only on purpose

					put("runner1", "1_178");    // Goblin Runner: strength 5; its text is on being played
					put("runner2", "1_178");
					put("soldier", "1_271");    // Orc Soldier: strength 7; Skirmish: exert to wound a character he is skirmishing
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert to wound a companion (except the Ring-bearer)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Faramir, Wizard's Pupil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Ranger.
		 * Fellowship: Play a [rohan] Man to heal Faramir.
		 * Skirmish: Exert Faramir to make an unbound Hobbit strength +2.
		 * Skirmish: Exert Gandalf to make Faramir unable to take wounds.
		 */

		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", faramir.getBlueprint().getTitle());
		assertEquals("Wizard's Pupil", faramir.getBlueprint().getSubtitle());
		assertTrue(faramir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, faramir.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, faramir.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, faramir.getBlueprint().getCardType());
		assertEquals(Race.MAN, faramir.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(faramir, Keyword.RANGER));
		assertEquals(3, faramir.getBlueprint().getTwilightCost());
		assertEquals(7, faramir.getBlueprint().getStrength());
		assertEquals(3, faramir.getBlueprint().getVitality());
		assertEquals(6, faramir.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, faramir.getBlueprint().getSignet());
	}

	@Test
	public void FaramirIsPlayedFromHandForThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCardsToHand(faramir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(faramir));

		scn.FreepsPlayCard(faramir);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipPlaysARohanManFromHandToHealFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var horseman = scn.GetFreepsCard("horseman");
		var aldor = scn.GetFreepsCard("aldor");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(faramir, gandalf);
		scn.AddWoundsToChar(faramir, 2);
		scn.AddWoundsToChar(gandalf, 1);

		scn.StartGame();

		// A [rohan] Hobbit and a [gondor] Man in hand, and no [rohan] Man: nothing to pay with.
		scn.MoveCardsToHand(merry, aragorn);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(faramir));

		scn.MoveCardsToHand(horseman, aldor);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsActionAvailable(faramir));

		scn.FreepsUseCardAction(faramir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(horseman, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(horseman);
		assertInZone(Zone.FREE_CHARACTERS, horseman);
		assertEquals(2, scn.GetTwilight());     // Enraged Horseman's cost
		assertInHand(aldor, merry, aragorn);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishExertsFaramirToMakeAnUnboundHobbitStrongerForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(faramir, pippin, merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(faramir, 2);

		scn.StartGame();

		// A [shire] Man: of the Hobbits' culture, and no Hobbit.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.SHIRE));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, runner1}, new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(pippin);

		// Faramir exhausted: this ability cannot be paid for.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(faramir, "unbound Hobbit"));

		scn.RemoveWoundsFromChar(faramir, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(faramir, "unbound Hobbit"));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(merry));

		scn.FreepsUseCardAction(faramir, "unbound Hobbit");
		assertEquals(2, scn.GetWoundsOn(faramir));
		// Pippin in this skirmish and Merry in none; not the Ring-bearer, not Ring-bound Sam, not the Hobbit ally,
		// not the [shire] Man, not Faramir.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, aragorn, faramir));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(merry);
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));

		// Still in force to the end of this skirmish; gone in the next one, which is another skirmish phase.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(merry));
		scn.FreepsPass();
		assertEquals(3, scn.GetStrength(merry));
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
	}

	@Test
	public void SkirmishExertsGandalfToMakeFaramirUnableToTakeWoundsForThatSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var soldier = scn.GetShadowCard("soldier");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(faramir, gandalf);

		scn.StartGame();

		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, soldier);
		scn.FreepsResolveSkirmish(faramir);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsActionAvailable(faramir, "Gandalf"));

		scn.FreepsUseCardAction(faramir, "Gandalf");
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(faramir));

		// Wounds cannot be assigned to Faramir, so Orc Soldier has no one to wound and the game flags his ability as
		// doing nothing: it would not, were the wounds only prevented and Faramir still a character he could wound.
		// The wound for losing (strength 7 against 7, a tie) is not placed either.
		assertEquals("effect", scn.ShadowActionNoEffect(soldier));
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(faramir));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(faramir));

		// The regroup phase: the skirmish phase is over, and Faramir can be wounded again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.MoveMinionsToTable(slayer);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(faramir, gandalf));
		scn.ShadowChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityNeedsGandalfAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(faramir, avenger);

		scn.StartGame();

		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, soldier);
		scn.FreepsResolveSkirmish(faramir);

		// A [gandalf] companion who is not Gandalf.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(faramir, "unbound Hobbit"));
		assertFalse(scn.FreepsActionAvailable(faramir, "Gandalf"));

		// Gandalf exhausted (3 wounds, vitality 4).
		scn.MoveCompanionsToTable(gandalf);
		scn.AddWoundsToChar(gandalf, 3);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(faramir, "Gandalf"));

		scn.RemoveWoundsFromChar(gandalf, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(faramir, "Gandalf"));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
