package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_024_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("icall", "103_24");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor], vitality 4; a maneuver ability only
					put("rohanAragorn", "17_93");  // Aragorn, Defender of Rohan: the Aragorn who is not [gondor]; his text needs the Ring-bearer assigned
					put("anduril", "7_80");        // Andúril, King's Blade: artifact, hand weapon, cost 3; Aragorn is defender +1 while there is no threat
					put("sword", "1_112");         // Ranger's Sword: hand weapon, cost 1, bearer must be Aragorn; damage +1
					put("bow", "1_90");            // Aragorn's Bow: ranged weapon, cost 1, bearer must be Aragorn; an archery ability
					put("oath", "8_41");           // Oathbreaker: [gondor] Wraith companion, cost 2; to play, add a threat
					put("king", "103_27");         // King of the Dead, Dead Keeper: [gondor] Wraith companion, cost 4; to play, add 2 threats
					put("deadman", "10_27");       // Dead Man of Dunharrow: [gondor] Wraith companion
					put("knight1", "5_35");        // Gondorian Knight: [gondor] Man with no text, cost 2
					put("knight2", "5_35");
					put("knight3", "5_35");
					put("legolas", "1_50");        // Legolas, Greenleaf: [elven] Elf, cost 2, nothing to play him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ICallonYouStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: I Call on You
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert Aragorn (or spot Anduril) and add X threats to play a Wraith from your dead pile, where X is the region number.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("icall");

		assertEquals("I Call on You", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAragornAddsOneThreatInRegionOneAndPlaysAChosenWraithFromTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var oath = scn.GetFreepsCard("oath");
		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, knight1);
		// A hand weapon of Aragorn's that is not Andúril.
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(oath, king, knight2, legolas);
		scn.MoveCardsToDiscard(deadman);

		scn.StartGame();
		// Every Free Peoples Wraith there is belongs to the [gondor] culture, so an [elven] companion in the dead pile
		// is made a Wraith to tell "a Wraith" from "a [gondor] companion".
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, legolas, null, Race.WRAITH));

		// Site 1 is in region 1.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(icall);

		// The costs are paid before the Wraith is chosen.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(oath, king, legolas));
		// A [gondor] companion in the dead pile who is no Wraith, and a Wraith in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight2, deadman));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(king);

		assertInZone(Zone.FREE_CHARACTERS, king);
		assertInZone(Zone.DEAD, oath, knight2, legolas);
		assertInZone(Zone.DISCARD, deadman);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// 1 for region 1 + 2 that King of the Dead adds to be played
		assertEquals(3, scn.GetThreats());
		// 3 for the event + 4 for King of the Dead
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.DISCARD, icall);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWraithOfAnotherCultureAloneInTheDeadPileIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, knight1);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(legolas, knight2);
		// The text names a race and no culture: an [elven] companion made a Wraith is the only Wraith in the dead pile.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, legolas, null, Race.WRAITH));

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(icall);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.DEAD, knight2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// 1 for region 1, and Legolas adds none to be played
		assertEquals(1, scn.GetThreats());
		// 3 for the event + 2 for Legolas
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NeedsAnAragornOfAnyCultureToExertAndNotAnotherGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var rohanAragorn = scn.GetFreepsCard("rohanAragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToHand(icall, rohanAragorn);
		scn.MoveCardsToDeadPile(oath);

		scn.StartGame();

		// A [gondor] Man who could exert, and no Aragorn.
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertFalse(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(rohanAragorn);
		assertTrue(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(icall);
		assertEquals(1, scn.GetWoundsOn(rohanAragorn));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertInZone(Zone.FREE_CHARACTERS, oath);
		// 1 for region 1 + 1 that Oathbreaker adds to be played
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAragornExhaustedUntilAndurilCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var knight1 = scn.GetFreepsCard("knight1");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn, knight1);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(icall, anduril);
		scn.MoveCardsToDeadPile(oath);

		scn.StartGame();

		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		assertFalse(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(anduril);
		assertAttachedTo(anduril, aragorn);
		assertTrue(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(icall);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, oath);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAndurilInPlayAragornIsNotExertedThoughHeCould() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var knight1 = scn.GetFreepsCard("knight1");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn, knight1);
		scn.AttachCardsTo(aragorn, anduril);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(oath);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(icall);

		// The part in parentheses is not a choice: with Andúril to spot, nobody exerts.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertInZone(Zone.FREE_CHARACTERS, oath);
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, icall);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InRegionTwoItAddsTwoThreatsAndCannotBePlayedWithRoomForOnlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var knight1 = scn.GetFreepsCard("knight1");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn, knight1);
		scn.MoveCardsToHand(icall, sword, bow);
		scn.MoveCardsToDeadPile(oath);

		scn.StartGame();
		// Site 4 is in region 2.
		scn.SkipToSite(4);

		// Frodo, Aragorn and the Knight: a limit of 3 threats, and room for one more.
		scn.AddThreats(2);
		// What may be played is worked out when the player is asked, so a weapon is played to be asked again.
		scn.FreepsPlayCard(sword);
		assertEquals(3, scn.GetThreatLimit());
		assertFalse(scn.FreepsPlayAvailable(icall));

		// Room for three: two for the event and the one Oathbreaker adds to be played.
		scn.RemoveThreats(2);
		scn.FreepsPlayCard(bow);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(icall);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, oath);
		// 2 for region 2 + 1 that Oathbreaker adds to be played
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InRegionThreeItAddsThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn, knight1, knight2);
		scn.MoveCardsToHand(icall);
		scn.MoveCardsToDeadPile(oath);

		scn.StartGame();
		// Site 7 is in region 3.
		scn.SkipToSite(7);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(icall));

		scn.FreepsPlayCard(icall);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, oath);
		// 3 for region 3 + 1 that Oathbreaker adds to be played
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoWraithInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn, knight1);
		scn.MoveCardsToHand(icall);
		// A [gondor] companion who is no Wraith in the dead pile, and a Wraith in the discard pile.
		scn.MoveCardsToDeadPile(knight2);
		scn.MoveCardsToDiscard(oath);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(icall));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("F-0107: offered when its own threat leaves no room for the threat the Wraith must add to be played; Aragorn exerts, the threat is added and no Wraith is played")
	public void CannotBePlayedWhenItsOwnThreatsLeaveNoRoomForTheThreatTheWraithAddsToBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var icall = scn.GetFreepsCard("icall");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(icall, sword);
		scn.MoveCardsToDeadPile(oath);

		scn.StartGame();

		// Frodo and Aragorn: a limit of 2 threats. With 1 there already, the event's own threat for region 1 is the
		// last there is room for, and Oathbreaker could not then add the threat it needs to be played.
		scn.AddThreats(1);
		scn.FreepsPlayCard(sword);
		assertEquals(2, scn.GetThreatLimit());

		assertFalse(scn.FreepsPlayAvailable(icall));
		assertInZone(Zone.DEAD, oath);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
