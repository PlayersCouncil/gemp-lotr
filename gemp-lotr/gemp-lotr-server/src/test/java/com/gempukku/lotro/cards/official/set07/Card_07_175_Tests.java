package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("corrupt", "7_175");

					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul, strength 9; her text reaches only bearers of [wraith] conditions
					// Ulaire Attea, Khamul of the East: a Nazgul of [raider], strength 12; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc), strength 5; its text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion, a Shadow card of a culture Free Peoples
					// cards also have; a regroup ability only
					put("gollum", "5_24");

					// Free Peoples cards of five cultures beside Frodo's [shire]
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8; a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a second [gondor] card; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf]; his own strength only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability only
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession; the minion archery total -1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorruptStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Corrupt
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Nazgûl strength +1 for each Free Peoples culture you spot.
		*/

		var scn = GetScenario();

		var corrupt = scn.GetFreepsCard("corrupt");

		assertEquals("Corrupt", corrupt.getBlueprint().getTitle());
		assertNull(corrupt.getBlueprint().getSubtitle());
		assertFalse(corrupt.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, corrupt.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, corrupt.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, corrupt.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(corrupt, Timeword.SKIRMISH));
		assertEquals(1, corrupt.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenNazgulStrengthPlusOneForEachFreePeoplesCultureSpottedUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var corrupt = scn.GetShadowCard("corrupt");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCardsToHand(corrupt);
		assertInHand(corrupt);
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound, gollum);
		scn.SetTwilight(5);
		assertEquals(aragorn, cloak.getAttachedTo());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, hound});
		// Khamul and Gollum are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.ShadowPlayAvailable(corrupt));

		scn.ShadowPlayCard(corrupt);

		// Five Free Peoples cultures: [shire] (Frodo), [gondor] (Aragorn and Boromir, counted once), [gandalf]
		// (Gandalf), [dwarven] (an ally) and [elven] (a possession only).  The [gollum] minion is no Free Peoples
		// card.  The number is the player's, filled in with all of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(5), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("5");

		// Either Nazgul, the one in this skirmish or the one in none; not a [wraith] minion who is no Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertEquals(14, scn.GetStrength(lemenya)); // 9 + 5
		assertEquals(12, scn.GetStrength(khamul));
		assertInDiscard(corrupt);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish phase it was played in: in the next skirmish it is gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(lemenya));
	}

	@Test
	public void TheNazgulMayBeOneInNoSkirmishAndThePlayerMaySpotFewerCulturesThanThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var corrupt = scn.GetShadowCard("corrupt");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(corrupt);
		assertInHand(corrupt);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.SetTwilight(5);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(khamul));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(9, scn.GetStrength(lemenya));

		scn.ShadowPlayCard(corrupt);

		// [shire] and [gondor]: up to 2, and he spots only one.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");
		scn.ShadowChooseCard(khamul);

		assertEquals(13, scn.GetStrength(khamul)); // 12 + 1
		assertEquals(9, scn.GetStrength(lemenya));
		assertInDiscard(corrupt);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoNazgulInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var corrupt = scn.GetShadowCard("corrupt");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(corrupt);
		assertInHand(corrupt);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(5);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowPlayAvailable(corrupt));

		scn.ShadowPlayCard(corrupt);
		// The number is asked all the same ([shire] and [gondor]), and nothing is then made stronger.
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(corrupt);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var corrupt = scn.GetShadowCard("corrupt");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(corrupt);
		assertInHand(corrupt);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(5);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(corrupt));
		assertInHand(corrupt);
	}
}
