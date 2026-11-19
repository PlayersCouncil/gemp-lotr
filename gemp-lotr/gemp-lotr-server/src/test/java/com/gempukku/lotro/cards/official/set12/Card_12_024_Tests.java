package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("highground", "12_24");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: an Elf companion, for the discard pile
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] character
					put("uruviel", "15_26");    // Uruviel, Woodland Maid: an [elven] follower, no character
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TakingtheHighGroundStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Taking the High Ground
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Toil 1</b>. <helper>(For each [elven] character you exert when playing this, its twilight cost is -1.)</helper><br><b>Archery:</b> Discard an [elven] character from hand to make the fellowship archery total +1.<br>Each time the fellowship moves from an underground site, return your Elf to your hand.
		*/

		var scn = GetScenario();

		var highground = scn.GetFreepsCard("highground");

		assertEquals("Taking the High Ground", highground.getBlueprint().getTitle());
		assertNull(highground.getBlueprint().getSubtitle());
		assertFalse(highground.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, highground.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, highground.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, highground.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(highground, Keyword.TOIL));
		assertEquals(1, scn.GetKeywordCount(highground, Keyword.TOIL));
		assertTrue(scn.HasKeyword(highground, Keyword.SUPPORT_AREA));
		assertEquals(2, highground.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedByToilExertingOneElvenCharacterItAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var highground = scn.GetFreepsCard("highground");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(highground);

		scn.StartGame();
		assertInHand(highground);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		scn.FreepsPlayCard(highground);
		// Toil: any number of [elven] characters, the ally among them; nothing is owed, the cost being added.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		scn.FreepsChooseCards(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		// 2 - 1 for the one exertion
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, highground);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityDiscardsAnElvenCharacterFromHandToMakeTheFellowshipArcheryTotalOneMoreForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var highground = scn.GetFreepsCard("highground");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var uruviel = scn.GetFreepsCard("uruviel");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(highground);

		scn.StartGame();
		scn.SetFreepsHand(arwen, celeborn, gimli, uruviel);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertInHand(arwen, celeborn, gimli, uruviel);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(highground));

		scn.FreepsUseCardAction(highground);
		// An [elven] companion and an [elven] ally; not an [elven] card that is no character, nor a [dwarven] character.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, uruviel));
		scn.FreepsChooseCard(arwen);

		assertInDiscard(arwen);
		assertInHand(celeborn, gimli, uruviel);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// No limit is printed: the last [elven] character in hand pays for a second use.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(highground));
		scn.FreepsUseCardAction(highground);
		assertInDiscard(arwen, celeborn);
		assertInHand(gimli, uruviel);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		// The two arrows land on the minion, and the bonus is gone with the archery phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithNoElvenCharacterInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var highground = scn.GetFreepsCard("highground");
		var gimli = scn.GetFreepsCard("gimli");
		var uruviel = scn.GetFreepsCard("uruviel");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(highground);

		scn.StartGame();
		scn.SetFreepsHand(gimli, uruviel);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// An [elven] follower and a [dwarven] character in hand: nothing to pay with.
		assertInHand(gimli, uruviel);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(highground));
	}

	@Test
	public void EachMoveFromAnUndergroundSiteReturnsAnElfInPlayToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var highground = scn.GetFreepsCard("highground");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(4);
		// Balin's Tomb is underground, and so is The Bridge of Khazad-dûm at site 5; Galadriel's Glade at 6 is not.
		scn.MoveCardsToSupportArea(highground);
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToDiscard(lorien);
		assertInZone(Zone.SUPPORT, highground, celeborn);
		assertInPlay(arwen, aragorn);
		assertInDiscard(lorien);
		scn.FreepsRefreshDecision();

		// The move from Balin's Tomb: one Elf in play, the companion or the ally; none from the discard pile.
		scn.FreepsPass();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, lorien));
		scn.FreepsChooseCard(celeborn);

		assertInHand(celeborn);
		assertInPlay(arwen, aragorn);
		assertInDiscard(lorien);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move from The Bridge of Khazad-dûm: the other Elf goes, the only one left.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInHand(arwen, celeborn);
		assertInPlay(aragorn);
		assertInDiscard(lorien);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheMoveToAnUndergroundSiteReturnsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var highground = scn.GetFreepsCard("highground");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");

		scn.StartGame();
		scn.SkipToSite(3);
		// Rivendell Valley is not underground; Balin's Tomb at site 4 is.
		scn.MoveCardsToSupportArea(highground);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(celeborn);
		assertInZone(Zone.SUPPORT, highground, celeborn);
		assertInPlay(arwen);
		scn.FreepsRefreshDecision();

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInPlay(arwen, celeborn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
