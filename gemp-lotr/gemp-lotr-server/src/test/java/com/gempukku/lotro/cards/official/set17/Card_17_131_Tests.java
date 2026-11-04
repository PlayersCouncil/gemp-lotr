package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slayer1", "17_131");
					put("slayer2", "17_131");   // not unique: "this minion" is read beside another copy
					put("tracker", "15_171");   // Tracking Uruk: a hunter minion, Hunter 1 is its whole text
					put("ferocity", "4_151");   // Ferocity: [isengard] Skirmish event, cost 0, an Uruk-hai strength +2 (+3 at a battleground)

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, no hunter; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, no hunter; a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion of vitality 3, laid exhausted; a skirmish ability only
					put("guardian", "17_7");    // Elven Guardian: a hunter companion (Hunter 1); a maneuver heal, used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability only
					put("halls", "1_26");       // Their Halls of Stone: [dwarven] Skirmish event, cost 0, a Dwarf strength +2 (+4 underground)

					// Gamling, Warrior of Rohan: "Skirmish: Play a [rohan] possession on Gamling to make him strength +2
					// and damage +1", a card played in a skirmish that is no event.  Rider's Spear: a [rohan] hand weapon, cost 1.
					put("gamling", "5_82");
					put("spear", "4_288");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandSlayerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Slayer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>Each time a player plays an event in a skirmish involving this minion, that player may exert a non-hunter character.
		*/

		var scn = GetScenario();

		var slayer = scn.GetFreepsCard("slayer1");

		assertEquals("White Hand Slayer", slayer.getBlueprint().getTitle());
		assertNull(slayer.getBlueprint().getSubtitle());
		assertFalse(slayer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, slayer.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, slayer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, slayer.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, slayer.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(slayer, Keyword.FIERCE));
		assertEquals(3, slayer.getBlueprint().getTwilightCost());
		assertEquals(6, slayer.getBlueprint().getStrength());
		assertEquals(2, slayer.getBlueprint().getVitality());
		assertEquals(5, slayer.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachEventPlayedInItsSkirmishLetsThePlayerOfThatEventExertANonHunterCharacterOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var guardian = scn.GetFreepsCard("guardian");
		var bounder = scn.GetFreepsCard("bounder");
		var halls = scn.GetFreepsCard("halls");
		scn.MoveCompanionsToTable(gimli, aragorn, boromir, guardian);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(halls);

		var slayer1 = scn.GetShadowCard("slayer1");
		var tracker = scn.GetShadowCard("tracker");
		var ferocity = scn.GetShadowCard("ferocity");
		scn.MoveCardsToHand(ferocity);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted, and so cannot exert.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(slayer1, tracker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, slayer1}, new PhysicalCardImpl[]{aragorn, tracker});
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(halls);

		// The Free Peoples player played the event, so it is his to choose, and he may choose nobody.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(0, scn.FreepsGetChoiceMin());
		// A non-hunter character of either side: the Slayer itself (a minion), companions in this skirmish and out of it,
		// and an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, gimli, aragorn, frodo, bounder));
		// A hunter companion and a hunter minion, and a character who cannot exert.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guardian, tracker, boromir));
		scn.FreepsChooseCard(slayer1);

		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(halls);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A second event in the same skirmish, played by the Shadow player: it is his to choose this time.  The
		// choice answers the play, and comes before the event's own choice of an Uruk-hai (CR 4.2 "playing a card").
		scn.ShadowPlayCard(ferocity);

		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, aragorn, frodo, bounder));
		// The Slayer is exhausted now (vitality 2, one wound), and is no more on offer than Boromir.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(slayer1, guardian, tracker, boromir));
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(slayer1);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ThePlayerOfTheEventMayChooseToExertNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var halls = scn.GetFreepsCard("halls");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(halls);

		var slayer1 = scn.GetShadowCard("slayer1");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, slayer1);
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsPlayCard(halls);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, gimli));
		scn.FreepsDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnEventPlayedInTheSkirmishOfAnotherMinionSetsOffOnlyTheCopyInThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var halls = scn.GetFreepsCard("halls");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(halls);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, slayer1}, new PhysicalCardImpl[]{aragorn, slayer2});
		// Aragorn's skirmish first: the first Slayer is assigned to Gimli and waits for his.
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		// Their Halls of Stone strengthens Gimli, though the skirmish it is played in is Aragorn's.
		scn.FreepsPlayCard(halls);

		// One choice, from the Slayer skirmishing Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer1, slayer2));
		scn.FreepsChooseCard(slayer2);

		assertEquals(1, scn.GetWoundsOn(slayer2));
		assertEquals(0, scn.GetWoundsOn(slayer1));
		// No second choice: the Slayer waiting for Gimli's skirmish is in no skirmish this event was played in.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACardThatIsNoEventPlayedInItsSkirmishSetsNothingOffWhereAnEventDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(gamling);
		scn.MoveCardsToHand(spear);

		var slayer1 = scn.GetShadowCard("slayer1");
		var ferocity = scn.GetShadowCard("ferocity");
		scn.MoveCardsToHand(ferocity);

		scn.StartGame();
		assertInZone(Zone.HAND, spear);
		scn.MoveMinionsToTable(slayer1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gamling, slayer1);
		scn.FreepsResolveSkirmish(gamling);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.HAND, spear);
		scn.FreepsUseCardAction(gamling);

		// A possession played in the Slayer's skirmish: nothing to choose, and the Shadow player has his skirmish actions.
		assertAttachedTo(spear, gamling);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gamling));
		assertEquals(0, scn.GetWoundsOn(slayer1));

		// The control: an event played there.
		scn.ShadowPlayCard(ferocity);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gamling, slayer1));
		scn.ShadowChooseCard(gamling);

		assertEquals(1, scn.GetWoundsOn(gamling));
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
