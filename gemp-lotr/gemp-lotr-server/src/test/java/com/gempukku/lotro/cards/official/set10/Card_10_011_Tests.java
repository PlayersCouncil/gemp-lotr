package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "10_11");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, cost 4
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, cost 2
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, cost 2

					put("defiance", "1_37");    // Defiance: [elven] Skirmish event
					put("dismay", "2_17");      // Dismay Our Enemies: [elven] Response event
					put("bow", "1_41");         // Elven Bow: [elven] possession, bearer must be an Elf
					put("bow2", "1_41");
					put("sleep", "1_84");       // Sleep, Caradhras: [gandalf] Fellowship event
					put("songs", "1_61");       // Songs of the Blessed Realm: [elven] condition, support area

					put("wreathed", "1_238");   // Wreathed in Shadow: [wraith] condition, support area; an archery ability
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession, support area
					put("weary", "4_212");      // Weary: [isengard] condition borne by a companion
					put("palantir", "3_67");    // The Palantir of Orthanc: [isengard] artifact, support area
					put("axe", "4_26");         // Iron Axe: [dunland] possession, bearer must be a [dunland] Man
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Galadriel, Lady Redeemed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: When Galadriel is in your starting fellowship, her twilight cost is -3.
		 * <b>Fellowship</b> <i>or</i> <b>Regroup:</b> Discard an [elven] event from hand to discard a Shadow
		 * condition or Shadow possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", card.getBlueprint().getTitle());
		assertEquals("Lady Redeemed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysFromHandAsACompanionForThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCardsToHand(galadriel);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(galadriel));

		scn.FreepsPlayCard(galadriel);
		// Outside the starting fellowship there is no discount: the printed 3.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheStartingFellowshipGaladrielCostsNothingAndAnotherElfCostsHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");

		// The game has not been started: both players have yet to choose their starting fellowships.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, aragorn, legolas, gimli));

		scn.FreepsChooseCard(aragorn);
		// Aragorn's 4 is the whole of what a starting fellowship may cost. Galadriel at 3 - 3 = 0 still fits;
		// Legolas, another Elf, costs his own 2 and does not, and nor does Gimli at 2.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, gimli));

		scn.FreepsChooseCard(galadriel);
		assertInZone(Zone.FREE_CHARACTERS, aragorn, galadriel);
		assertInZone(Zone.DECK, legolas, gimli);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void FellowshipActionDiscardsAnElvenEventFromHandToDiscardAShadowConditionOrShadowPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var defiance = scn.GetFreepsCard("defiance");
		var dismay = scn.GetFreepsCard("dismay");
		var bow = scn.GetFreepsCard("bow");
		var sleep = scn.GetFreepsCard("sleep");
		var legolasBow = scn.GetFreepsCard("bow2");
		var songs = scn.GetFreepsCard("songs");
		var ownWreathed = scn.GetFreepsCard("wreathed");
		var wreathed = scn.GetShadowCard("wreathed");
		var ships = scn.GetShadowCard("ships");
		var weary = scn.GetShadowCard("weary");
		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCompanionsToTable(galadriel, legolas);
		// Free Peoples cards of both types named, and the Free Peoples player's own copy of a Shadow condition,
		// which is not active on his turn.
		scn.AttachCardsTo(legolas, legolasBow);
		scn.MoveCardsToSupportArea(songs, ownWreathed);
		// Two [elven] events, an [elven] card that is no event, and an event that is not [elven].
		scn.MoveCardsToHand(defiance, dismay, bow, sleep);
		// The opponent's: a condition and a possession in his support area, a condition on a companion, and an
		// artifact.
		scn.MoveCardsToSupportArea(wreathed, ships, palantir);
		scn.AttachCardsTo(frodo, weary);
		assertInZone(Zone.HAND, defiance, dismay, bow, sleep);
		assertInZone(Zone.SUPPORT, wreathed, ships, palantir, songs, ownWreathed);
		assertAttachedTo(weary, frodo);
		assertAttachedTo(legolasBow, legolas);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(galadriel);
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, dismay));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bow, sleep));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(defiance);
		assertInZone(Zone.DISCARD, defiance);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());

		assertTrue(scn.FreepsHasCardChoiceAvailable(wreathed, ships, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(palantir, songs, legolasBow, ownWreathed));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(wreathed);
		assertInDiscard(wreathed);
		assertInZone(Zone.SUPPORT, ships, palantir, songs, ownWreathed);
		assertAttachedTo(weary, frodo);
		assertAttachedTo(legolasBow, legolas);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// There is no limit: a second use in the same phase. Dismay Our Enemies is the one [elven] event left
		// in hand, so it is discarded without a choice.
		assertTrue(scn.FreepsActionAvailable(galadriel));
		scn.FreepsUseCardAction(galadriel);
		assertInZone(Zone.DISCARD, dismay);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ships, weary));

		scn.FreepsChooseCard(ships);
		assertInDiscard(ships);
		assertAttachedTo(weary, frodo);
		assertEquals(2, scn.GetShadowDiscardCount());

		// The Elven Bow and Sleep, Caradhras are still in hand, and Weary could still be discarded, but neither
		// card is an [elven] event, and the two in the discard pile are not in hand.
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, bow, sleep);
		assertFalse(scn.FreepsActionAvailable(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsAPossessionBorneByAMinionAndIsNoManeuverAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var defiance = scn.GetFreepsCard("defiance");
		var dismay = scn.GetFreepsCard("dismay");
		var wreathed = scn.GetShadowCard("wreathed");
		var palantir = scn.GetShadowCard("palantir");
		var brigand = scn.GetShadowCard("brigand");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCompanionsToTable(galadriel);
		scn.MoveCardsToHand(defiance, dismay);
		scn.MoveCardsToSupportArea(wreathed, palantir);
		assertInZone(Zone.HAND, defiance, dismay);
		assertInZone(Zone.SUPPORT, wreathed, palantir);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(brigand, axe);
		assertAttachedTo(axe, brigand);

		// Neither phase named: with an [elven] event in hand and cards to discard, nothing is offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galadriel));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(galadriel));
		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(galadriel);
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, dismay));
		scn.FreepsChooseCard(dismay);
		assertInZone(Zone.DISCARD, dismay);
		assertInZone(Zone.HAND, defiance);
		assertEquals(1, scn.GetFreepsHandCount());

		// The Iron Axe on the minion and the condition in the support area; not the minion, not the artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, wreathed));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, palantir));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(axe);
		assertInDiscard(axe);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInZone(Zone.SUPPORT, wreathed, palantir);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNothingToDiscardTheEventIsStillDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var defiance = scn.GetFreepsCard("defiance");
		var dismay = scn.GetFreepsCard("dismay");
		var songs = scn.GetFreepsCard("songs");
		var ownWreathed = scn.GetFreepsCard("wreathed");
		var palantir = scn.GetShadowCard("palantir");
		scn.MoveCompanionsToTable(galadriel);
		scn.MoveCardsToHand(defiance, dismay);
		// Nothing here is an active Shadow condition or Shadow possession: an artifact, a Free Peoples condition,
		// and the Free Peoples player's own Shadow condition.
		scn.MoveCardsToSupportArea(songs, ownWreathed, palantir);
		assertInZone(Zone.HAND, defiance, dismay);
		assertInZone(Zone.SUPPORT, songs, ownWreathed, palantir);

		scn.StartGame();

		// The ability has a cost, so it is offered all the same and the cost is paid for nothing.
		assertTrue(scn.FreepsActionAvailable(galadriel));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(defiance);
		assertInZone(Zone.DISCARD, defiance);
		assertInZone(Zone.HAND, dismay);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, songs, ownWreathed, palantir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
