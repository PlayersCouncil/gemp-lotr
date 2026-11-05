package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_326_Tests
{

	// The Towers path with Horse-country as its site 1.  Site 2 is Eastfold (a Shadow ability that needs 3
	// [isengard] trackers), site 3 Barrows of Edoras (a sanctuary; a Fellowship action used only on purpose) and
	// site 4 White Rocks (its Shadow number counts mounted companions, which reaches no reading here).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_266");      // Eomer, Sister-son of Theoden: a [rohan] Man companion; his start-of-turn heal of a [rohan] ally finds none wounded
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, home site 4T
					put("ridersmount", "4_287"); // Rider's Mount: a [rohan] mount, bearer a [rohan] Man; a trigger at the start of bearer's skirmish only
					put("horse", "4_283");      // Horse of Rohan: a [rohan] mount, bearer a Man, Elf or Wizard; a trigger at the start of bearer's skirmish only
					put("spear", "4_288");      // Rider's Spear: a [rohan] possession that is no mount (a hand weapon); damage +1 while bearer is mounted
					put("arod", "13_1");        // Arod, Rohirrim Steed: a [dwarven] mount, bearer a Dwarf; its play trigger and response not reached
				}},
				new HashMap<>()
				{{
					put("site1", "4_326");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorsecountryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Horse-country
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1T
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Plains</b>. While you can spot a [rohan] mount at Horse-country, the move limit is +1 for this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Horse-country", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void BeginningTheGameAtHorseCountryWithARohanMountRaisesTheMoveLimitForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, ridersmount);

		scn.StartGame();
		assertAttachedTo(ridersmount, eomer);
		assertEquals("Horse-country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();          // move 1 -> 2
		// The fellowship has left Horse-country, and the limit stays where it was for the rest of the turn.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Three moves of three: the turn ends.  On the turns that follow the limit is 2 again, the mount still borne.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertAttachedTo(ridersmount, eomer);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanMountAtHorseCountryTheMoveLimitStays2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");
		var spear = scn.GetFreepsCard("spear");
		var arod = scn.GetFreepsCard("arod");
		scn.MoveCompanionsToTable(eomer, gimli);
		scn.AttachCardsTo(eomer, spear);
		scn.AttachCardsTo(gimli, arod);

		scn.StartGame();
		// A [rohan] possession that is no mount, and a mount that is not [rohan].
		assertAttachedTo(spear, eomer);
		assertAttachedTo(arod, gimli);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();          // move 1 -> 2
		assertEquals(2, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());

		// Two moves of two: the regroup phase ends the turn, and no question of another move stops the skip.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ARohanMountPlayedAtHorseCountryRaisesTheMoveLimitForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(ridersmount);

		scn.StartGame();
		assertEquals("Horse-country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(ridersmount);
		assertAttachedTo(ridersmount, eomer);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 1 -> 2
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());

		// Two moves made in a turn begun at Horse-country, and a third is offered.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ARohanMountBorneByAGondorManCountsOnlyOnItsOwnersTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horse);

		scn.StartGame();
		// A [rohan] mount of another title, on a Man who is not [rohan] (the [rohan] Man spotted for its play may
		// have left play since).
		assertAttachedTo(horse, aragorn);
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		// The second player's turn 2, his fellowship at Horse-country: the first player's mount is not his to spot.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertAttachedTo(horse, aragorn);
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheSecondPlayersOwnRohanMountRaisesHisMoveLimitAtHorseCountry() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowEomer = scn.GetShadowCard("eomer");
		var shadowMount = scn.GetShadowCard("ridersmount");
		scn.MoveCompanionsToTable(shadowEomer);
		scn.AttachCardsTo(shadowEomer, shadowMount);

		scn.StartGame();
		// The first player's turn 1 at Horse-country: the second player's mount is not active on it.
		assertAttachedTo(shadowMount, shadowEomer);
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void ARohanMountBorneByAnAllyAwayFromHorseCountryDoesNotRaiseTheMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guma = scn.GetFreepsCard("guma");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, ridersmount);

		scn.StartGame();
		// Guma's home is site 4T, not yet on the path: the fellowship is at Horse-country and the mount he bears is not.
		assertAttachedTo(ridersmount, guma);
		assertEquals("Horse-country", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
