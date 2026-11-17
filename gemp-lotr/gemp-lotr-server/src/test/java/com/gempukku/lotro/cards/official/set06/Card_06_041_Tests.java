package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("promise", "6_41");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, vitality 4; a regroup ability used only on purpose
					put("gollum2", "103_66");   // Gollum, Half a Wraith Himself: the [wraith] Gollum, vitality 4; his trigger answers a Nazgûl played, his ability needs threats
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, vitality 8
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit, vitality 4; no weapon here, so no text
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Durin's Secret to spot; a strength bonus only
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf for Durin's Secret, vitality 3; a skirmish ability used only on purpose
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: the Elf for Durin's Secret, no archer; +2 only at sites 6 to 8
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MasterBrokeHisPromiseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Master Broke His Promise
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert Gollum 3 times to wound each companion. The move limit for this turn is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("promise");

		assertEquals("Master Broke His Promise", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGollumThreeTimesToWoundEachCompanionAndNoAllyOrMinionAndLowersTheMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(bounder);

		var promise = scn.GetShadowCard("promise");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		assertInZone(Zone.HAND, promise);
		scn.MoveMinionsToTable(gollum);
		// Merry, vitality 4, is exhausted: a wound kills him where an exertion could not be made.
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowPlayAvailable(promise));

		scn.ShadowPlayCard(promise);
		// Gollum exerts three times and is not wounded besides: a fourth wound would kill him.
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DEAD, merry);
		// The ally is no companion.
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.DISCARD, promise);
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenGollumCanExertOnlyTwiceThoughAnotherGollumCultureMinionCould() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var promise = scn.GetShadowCard("promise");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		assertInZone(Zone.HAND, promise);
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 1 wound: he can exert twice, not three times.
		scn.AddWoundsToChar(gollum, 1);
		assertEquals(1, scn.GetWoundsOn(gollum));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertFalse(scn.ShadowPlayAvailable(promise));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void TheWraithGollumPaysTooAndTheLimitStaysLoweredByOneForTheRestOfTheTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var haldir = scn.GetFreepsCard("haldir");
		var secret = scn.GetFreepsCard("secret");
		scn.MoveCompanionsToTable(gandalf, gimli, haldir);
		scn.MoveCardsToHand(secret);

		var promise = scn.GetShadowCard("promise");
		var gollum2 = scn.GetShadowCard("gollum2");
		scn.MoveCardsToHand(promise);

		scn.StartGame();
		assertInZone(Zone.HAND, promise);
		// Durin's Secret raises the limit first, since the engine's move limit never reads below 1.
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit());
		scn.MoveMinionsToTable(gollum2);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(gollum2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.ShadowPlayAvailable(promise));

		scn.ShadowPlayCard(promise);
		assertEquals(3, scn.GetWoundsOn(gollum2));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(haldir));
		// 2 + 1 - 1
		assertEquals(2, scn.GetMoveLimit());

		// One move made of two: the fellowship moves again, to site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveLimit());

		// Two moves of two: the turn ends with no question of a third.
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P2());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		scn.PassReconcile(scn.P1());
		// The next turn has begun: the Shadow player moves his fellowship.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// The next turn's limit is the usual 2, not 1.
		assertEquals(2, scn.GetMoveLimit());
	}
}
