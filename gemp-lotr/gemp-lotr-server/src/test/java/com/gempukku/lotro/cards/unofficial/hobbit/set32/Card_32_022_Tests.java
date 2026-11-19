package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_022_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer. The characters with a
	// start-of-turn text are cheated in after StartGame(), so none of those offers is made.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gathering", "32_22");
					// Gandalf, Friend of Thorin: the Hobbit sets' Gandalf, a companion; a Fellowship ability only, and
					// a discard bar that wants two [dwarven] followers
					put("gandalf", "30_26");
					// Elrond, Lord of Rivendell: an Elrond ally; his start-of-turn heal of allies of site 3 finds none
					put("elrondally", "1_40");
					// Elrond, Elven Lord 7_21: an Elrond who is a companion; a start-of-turn exertion offer
					put("elrondcomp", "7_21");
					// Elrond, Elven Lord 30_21: the Hobbit sets' Elrond, an ally; a start-of-turn heal offer and a
					// Regroup ability used only on purpose
					put("elrondhdg", "30_21");
					// Galadriel, Lady of Light: a Galadriel ally; her start-of-turn heal of allies of site 6 finds none
					put("galadrielally", "1_45");
					// Galadriel, Bearer of Wisdom: a Galadriel who is a companion; a resistance bonus only
					put("galadrielcomp", "9_14");
					// Galadriel, Elven Lady: the Hobbit sets' Galadriel, an ally; a start-of-turn heal of Gandalf and a
					// Skirmish ability used only on purpose
					put("galadrielhdg", "32_13");
					// Radagast, The Brown: a [gandalf] Wizard ally who is not Gandalf; a Fellowship ability only
					put("radagast", "31_17");
					// Lindir, Elven Minstrel: an [elven] Elf ally who is neither Elrond nor Galadriel; a move trigger
					// that heals an [elven] ally, none wounded
					put("lindir", "33_14");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, strength 7, damage +1; a Maneuver ability used only on purpose

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger only
					put("ferny", "33_22");      // Bill Ferny Sr.: [gundabad] Man, strength 4; a Regroup ability used only on purpose
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability used only on purpose
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7; archer only at site 9, a Regroup ability used only on purpose
					// Dark Sorcery: a Shadow condition that is no minion; its Shadow ability is used only on purpose
					put("sorcery", "33_61");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GatheringoftheThreeRingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Gathering of the Three Rings
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spell.
		 * 		Make X minions strength -X until the regroup phase, where X is the number of the following characters you can spot: Gandalf, Elrond or Galadriel.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gathering");

		assertEquals("Gathering of the Three Rings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGandalfAloneItMakesOneOfTwoMinionsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gathering = scn.GetFreepsCard("gathering");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast, lindir);
		scn.MoveCardsToHand(gathering);

		var runner = scn.GetShadowCard("runner");
		var aggressor = scn.GetShadowCard("aggressor");
		var sorcery = scn.GetShadowCard("sorcery");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, aggressor);
		scn.MoveCardsToSupportArea(sorcery);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(aggressor));
		assertInHand(gathering);
		assertTrue(scn.FreepsPlayAvailable(gathering));
		scn.FreepsPlayCard(gathering);

		// Radagast is a [gandalf] Wizard and Lindir an [elven] ally, but only Gandalf is one of the three: X is 1.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, aggressor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, gandalf, sorcery));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aggressor);

		assertEquals(5, scn.GetStrength(aggressor));
		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(gathering);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithElrondTheCompanionAndGaladrielTheAllyItMakesTwoOfThreeMinionsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gathering = scn.GetFreepsCard("gathering");
		var elrond = scn.GetFreepsCard("elrondcomp");
		var galadriel = scn.GetFreepsCard("galadrielally");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveAlliesToTable(galadriel, lindir);
		scn.MoveCardsToHand(gathering);

		var runner = scn.GetShadowCard("runner");
		var ferny = scn.GetShadowCard("ferny");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveCompanionsToTable(elrond);
		scn.MoveMinionsToTable(runner, ferny, aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(6, scn.GetStrength(aggressor));
		scn.FreepsPlayCard(gathering);

		// No Gandalf: Elrond and Galadriel make X 2.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, ferny, aggressor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, elrond));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(runner, ferny);

		assertEquals(3, scn.GetStrength(runner));
		assertEquals(2, scn.GetStrength(ferny));
		assertEquals(6, scn.GetStrength(aggressor));
		assertInDiscard(gathering);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAllThreeSpottedItMakesThreeOfFourMinionsStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetFreepsCard("gathering");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrondally");
		var galadriel = scn.GetFreepsCard("galadrielcomp");
		scn.MoveCompanionsToTable(gandalf, galadriel);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(gathering);

		var runner = scn.GetShadowCard("runner");
		var ferny = scn.GetShadowCard("ferny");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, ferny, aggressor, veteran);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(6, scn.GetStrength(aggressor));
		assertEquals(7, scn.GetStrength(veteran));
		scn.FreepsPlayCard(gathering);

		// Each of the three titles is unique, so 3 is as high as X goes.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, ferny, aggressor, veteran));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(runner, ferny, aggressor);

		assertEquals(2, scn.GetStrength(runner));
		assertEquals(1, scn.GetStrength(ferny));
		assertEquals(3, scn.GetStrength(aggressor));
		assertEquals(7, scn.GetStrength(veteran));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PlayableInTheManeuverPhaseAndNotInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetFreepsCard("gathering");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(gathering);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(gathering));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInHand(gathering);
		assertFalse(scn.FreepsPlayAvailable(gathering));
	}

	@Test
	public void ThePenaltyLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gathering = scn.GetFreepsCard("gathering");
		var elrond = scn.GetFreepsCard("elrondhdg");
		var galadriel = scn.GetFreepsCard("galadrielhdg");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToHand(gathering);

		var runner = scn.GetShadowCard("runner");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, galadriel);
		scn.MoveMinionsToTable(runner, aggressor);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(6, scn.GetStrength(aggressor));
		scn.FreepsPlayCard(gathering);
		// The Hobbit sets' Elrond and Galadriel: X is 2, and the two minions there are take it unasked.
		assertEquals(3, scn.GetStrength(runner));
		assertEquals(4, scn.GetStrength(aggressor));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dwalin, runner}, new PhysicalCardImpl[]{balin, aggressor});

		// Dwalin's skirmish first; Balin's follows it.
		scn.FreepsResolveSkirmish(dwalin);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(balin);
		assertTrue(scn.IsCharSkirmishingAgainst(balin, aggressor));
		assertEquals(4, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Balin (7) beats the Aggressor (4), who lives through one wound.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(6, scn.GetStrength(aggressor));
	}
}
