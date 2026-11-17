package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("run", "18_109");
					put("run2", "18_109");
					put("run3", "18_109");

					put("song", "8_112");       // Song of the Shire: [shire] support-area condition that holds [shire] tokens; abilities used only on purpose
					put("song2", "8_112");
					put("path", "8_42");        // A Path Appointed: [gondor] support-area condition that holds [gondor] tokens; abilities used only on purpose

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] unbound Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] unbound Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit companion; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is no Hobbit, strength 8, vitality 4
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally takes part in skirmishes

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MakeaRunForItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Make a Run For It
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Remove 2 [shire] tokens to cancel a skirmish involving an unbound Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("run");

		assertEquals("Make a Run For It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesTwoShireTokensToCancelTheSkirmishOfAShireUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var song = scn.GetFreepsCard("song");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(song);
		scn.AddTokensToCard(song, 2);
		assertEquals(2, scn.GetCultureTokensOn(song));

		scn.StartGame();
		scn.MoveCardsToHand(run);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsPlayAvailable(run));

		scn.FreepsPlayCard(run);
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertInDiscard(run);
		// Nobody fought: 13 against 3 would have killed Merry.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokensMayComeFromTwoCardsAndAGondorUnboundHobbitsSkirmishIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var pippin = scn.GetFreepsCard("pippin");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(song, song2);
		scn.AddTokensToCard(song, 1);
		scn.AddTokensToCard(song2, 1);
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(1, scn.GetCultureTokensOn(song2));

		scn.StartGame();
		scn.MoveCardsToHand(run);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, troop1);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop1));
		assertTrue(scn.FreepsPlayAvailable(run));

		// One token on each card: both are needed, and both go, one card chosen at a time.
		scn.FreepsPlayCard(run);
		assertTrue(scn.FreepsHasCardChoiceAvailable(song, song2));
		scn.FreepsChooseCard(song2);
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(0, scn.GetCultureTokensOn(song2));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertInDiscard(run);
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneShireTokenBesideGondorTokensItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var song = scn.GetFreepsCard("song");
		var path = scn.GetFreepsCard("path");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(song, path);
		scn.AddTokensToCard(song, 1);
		scn.AddTokensToCard(path, 2);
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(2, scn.GetCultureTokensOn(path));

		scn.StartGame();
		scn.MoveCardsToHand(run);
		assertInHand(run);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		// Three tokens in all, but only one of them [shire].
		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(run));
		assertInHand(run);
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(2, scn.GetCultureTokensOn(path));
	}

	@Test
	public void ASkirmishOfARingBoundHobbitAnUnboundManOrAHobbitAllyIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var run2 = scn.GetFreepsCard("run2");
		var run3 = scn.GetFreepsCard("run3");
		var song = scn.GetFreepsCard("song");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(song);
		scn.AddTokensToCard(song, 6);
		assertEquals(6, scn.GetCultureTokensOn(song));

		scn.StartGame();
		scn.MoveCardsToHand(run, run2, run3, follow);
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);
		// Bounder is made to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop1}, new PhysicalCardImpl[]{aragorn, troop2},
				new PhysicalCardImpl[]{bounder, troop3});

		// Sam is a Hobbit and Ring-bound.  The event is played for nothing: its tokens are paid, and the skirmish
		// goes on.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.FreepsPlayAvailable(run));
		scn.FreepsPlayCard(run);
		assertEquals(4, scn.GetCultureTokensOn(song));
		assertInDiscard(run);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Aragorn is unbound and no Hobbit.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertTrue(scn.FreepsPlayAvailable(run2));
		scn.FreepsPlayCard(run2);
		assertEquals(2, scn.GetCultureTokensOn(song));
		assertInDiscard(run2);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Bounder is a Hobbit, and an ally: only companions are unbound.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		assertTrue(scn.FreepsPlayAvailable(run3));
		scn.FreepsPlayCard(run3);
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertInDiscard(run3);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
