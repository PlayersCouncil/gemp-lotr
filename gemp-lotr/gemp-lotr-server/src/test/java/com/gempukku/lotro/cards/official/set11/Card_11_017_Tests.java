package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksmanship", "11_17");
					put("marksmanship2", "11_17");
					put("legolas", "11_21");    // Legolas, Companion of the Ring: Elf companion, no requirement to play; +2 only at a battleground or forest
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, home 3F, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion; a skirmish ability used only on purpose
					put("archer", "1_51");      // Legolas, Prince of Mirkwood: an Elf archer companion; +3 only against a Nazgul
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenMarksmanshipStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Elven Marksmanship
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 6
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an Elf.<br><b>Toil 2</b>. <helper>(For each [elven] character you exert when playing this, its twilight cost is -2.)</helper><br><b>Archery:</b> Discard this condition to make the fellowship archery total +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marksmanship");

		assertEquals("Elven Marksmanship", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(6, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmanship = scn.GetFreepsCard("marksmanship");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(marksmanship, legolas);

		scn.StartGame();
		assertInHand(marksmanship);

		// Gimli and Frodo: no Elf to spot.
		assertFalse(scn.FreepsPlayAvailable(marksmanship));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);

		assertTrue(scn.FreepsPlayAvailable(marksmanship));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyAloneIsSpottedAndExertingHimForToilTakesTwoOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var marksmanship = scn.GetFreepsCard("marksmanship");
		var golradir = scn.GetFreepsCard("golradir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(marksmanship);

		scn.StartGame();
		assertInHand(marksmanship);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(golradir));

		// The Elf ally is the only Elf in play.
		assertTrue(scn.FreepsPlayAvailable(marksmanship));
		scn.FreepsPlayCard(marksmanship);

		// Toil: only an [elven] character may be exerted, and none need be.
		assertTrue(scn.FreepsHasCardChoiceAvailable(golradir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(golradir);

		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, marksmanship);
		// 6 - 2 for the one exertion
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingTwoElvenCharactersForToilTakesFourOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmanship = scn.GetFreepsCard("marksmanship");
		var legolas = scn.GetFreepsCard("legolas");
		var golradir = scn.GetFreepsCard("golradir");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(marksmanship);

		scn.StartGame();
		assertInHand(marksmanship);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(golradir));

		scn.FreepsPlayCard(marksmanship);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, golradir);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertInZone(Zone.SUPPORT, marksmanship);
		// 6 - 2 - 2 for the two exertions
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryActionDiscardsThisConditionToMakeTheFellowshipArcheryTotalPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmanship = scn.GetFreepsCard("marksmanship");
		var marksmanship2 = scn.GetFreepsCard("marksmanship2");
		var archer = scn.GetFreepsCard("archer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(archer);
		scn.MoveCardsToSupportArea(marksmanship, marksmanship2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, marksmanship);
		assertInZone(Zone.SUPPORT, marksmanship2);

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// The archer Legolas adds 1.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(marksmanship));

		scn.FreepsUseCardAction(marksmanship);

		// This condition is discarded, not the other copy.
		assertInDiscard(marksmanship);
		assertInZone(Zone.SUPPORT, marksmanship2);
		// 1 + 2
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		scn.ShadowPass();

		// The other copy is still there to be used.
		assertTrue(scn.FreepsActionAvailable(marksmanship2));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmanship = scn.GetFreepsCard("marksmanship");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(marksmanship);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, marksmanship);

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(marksmanship));

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(marksmanship));
	}

	@Test
	public void TheArcheryTotalBonusLastsThroughTheArcheryPhaseAndNoLonger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmanship = scn.GetFreepsCard("marksmanship");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(marksmanship);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, marksmanship);

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(marksmanship);
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPass();
		// The last decision of the archery phase: still in force.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.FreepsPass();

		// The first decision after it, in the assignment phase: gone.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
