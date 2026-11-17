package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_026_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1 moves from Bag
	// End (site 1) to The Troll Hoard (site 2, Shadow number 3), and a regroup move goes on to Rivendell (site 3,
	// Shadow number 1). None of the three is in region 2 or 3.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("war", "32_26");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					put("fili", "30_10");       // Fili, Brother of Kili: [dwarven] Dwarf companion; a response to a [dwarven] companion's wound

					// Allies: [dwarven] ones, and others
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, vitality 3; a fellowship ability only
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] Dwarf ally, vitality 3; his regroup trigger needs a [dwarven] follower
					put("bain", "32_15");       // Bain, Son of Bard: [esgaroth] Man ally; a start-of-turn heal of a wounded [esgaroth] Man only
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal and a regroup ability

					// Items
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon, borne by a [dwarven] character
					put("barricade", "31_4");   // Great Barricade: [dwarven] possession in the support area
					put("arkenstone", "31_7");  // The Arkenstone: a [dwarven] artifact in the support area
					put("weapon", "32_18");     // Iron-Forged Weapon: an [esgaroth] possession, borne by a [dwarven] companion

					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WarforGoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: War for Gold
		 * Unique: True
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time the fellowship moves, add (1) for each ally you can spot (limit (3)).
		 * 	<b>Maneuver:</b> Discard this condition to wound a [dwarven] ally (or discard a [dwarven] possession instead if you cannot spot a [dwarven] ally).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("war");

		assertEquals("War for Gold", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveAddsOneForEachAllyOfTheFreePeoplesPlayerCountedAtThatMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var balin = scn.GetFreepsCard("balin");
		var grimir = scn.GetFreepsCard("grimir");
		var bain = scn.GetFreepsCard("bain");
		var shadowDain = scn.GetShadowCard("dain");
		scn.MoveCardsToSupportArea(war);
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(grimir);
		// The Shadow player's own ally is not active on the Free Peoples player's turn.
		scn.MoveAlliesToTable(shadowDain);
		assertInZone(Zone.SUPPORT, war, grimir, shadowDain);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// To The Troll Hoard: 3 for its Shadow number + 2 for Bilbo and Balin + 1 for Grimir.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.MoveAlliesToTable(bain);
		assertInZone(Zone.SUPPORT, bain);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// To Rivendell: 1 for its Shadow number + 2 for the companions + 2 for Grimir and Bain.
		assertEquals(pool + 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AddsNoMoreThanThreeAtAMoveAndThreeAgainAtTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCardsToSupportArea(war);
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable("grimir", "dain", "bain", "elrond");
		assertInZone(Zone.SUPPORT, war);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// Four allies, limit (3): 3 for The Troll Hoard + 2 for the companions + 3.
		assertEquals(8, scn.GetTwilight());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// 1 for Rivendell + 2 for the companions + 3 again, in a phase of its own.
		assertEquals(pool + 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveAlliesToTable("grimir", "bain");

		scn.StartGame();
		scn.FreepsPass();
		// Put down after the turn's first move, which it would otherwise answer.
		scn.MoveCardsToSupportArea(war);
		assertInZone(Zone.SUPPORT, war);

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		// A card in hand, so that the Free Peoples player is asked to reconcile and the turn does not end at once.
		scn.SetFreepsHand(axe);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToStay();
		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}

	@Test
	public void ManeuverDiscardsItToWoundADwarvenAllyAndNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var bilbo = scn.GetRingBearer();
		var fili = scn.GetFreepsCard("fili");
		var balin = scn.GetFreepsCard("balin");
		var grimir = scn.GetFreepsCard("grimir");
		var dain = scn.GetFreepsCard("dain");
		var bain = scn.GetFreepsCard("bain");
		var axe = scn.GetFreepsCard("axe");
		var barricade = scn.GetFreepsCard("barricade");
		scn.MoveCardsToSupportArea(war);
		scn.MoveCompanionsToTable(fili, balin);
		scn.MoveAlliesToTable(grimir, dain, bain);
		scn.AttachCardsTo(fili, axe);
		scn.MoveCardsToSupportArea(barricade);
		// Vitality 3 with 2 wounds: a wound kills him.
		scn.AddWoundsToChar(grimir, 2);
		assertEquals(2, scn.GetWoundsOn(grimir));

		assertInZone(Zone.SUPPORT, war, barricade);
		assertAttachedTo(axe, fili);
		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertAttachedTo(axe, fili);
		assertInZone(Zone.SUPPORT, barricade);
		assertTrue(scn.ShadowActionAvailable(war));

		scn.ShadowUseCardAction(war);
		assertInDiscard(war);
		// Not an ally of another culture, nor a [dwarven] companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grimir, dain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bain, fili, balin, bilbo));
		scn.ShadowChooseCard(grimir);

		// A wound, not an exertion: the exhausted ally is killed.
		assertInZone(Zone.DEAD, grimir);
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(fili));
		// A [dwarven] ally was spotted, so no possession goes.
		assertAttachedTo(axe, fili);
		assertInZone(Zone.SUPPORT, barricade);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarvenAllyItDiscardsADwarvenPossessionInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var fili = scn.GetFreepsCard("fili");
		var balin = scn.GetFreepsCard("balin");
		var bain = scn.GetFreepsCard("bain");
		var axe = scn.GetFreepsCard("axe");
		var barricade = scn.GetFreepsCard("barricade");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var weapon = scn.GetFreepsCard("weapon");
		scn.MoveCardsToSupportArea(war);
		scn.MoveCompanionsToTable(fili, balin);
		// An ally of another culture, and [dwarven] companions, are no [dwarven] ally.
		scn.MoveAlliesToTable(bain);
		scn.AttachCardsTo(fili, axe);
		scn.AttachCardsTo(balin, weapon);
		scn.MoveCardsToSupportArea(barricade, arkenstone);

		assertInZone(Zone.SUPPORT, war, barricade, arkenstone);
		assertAttachedTo(axe, fili);
		assertAttachedTo(weapon, balin);
		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(axe, fili);
		assertAttachedTo(weapon, balin);
		assertInZone(Zone.SUPPORT, barricade, arkenstone);
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));

		scn.ShadowUseCardAction(war);
		assertInDiscard(war);
		// A borne one and one in the support area; not a [dwarven] artifact nor a possession of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, barricade));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(arkenstone, weapon));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);

		assertInDiscard(axe);
		assertAttachedTo(weapon, balin);
		assertInZone(Zone.SUPPORT, barricade, arkenstone);
		// And nobody is wounded.
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNothingOfEitherKindItIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var balin = scn.GetFreepsCard("balin");
		var bain = scn.GetFreepsCard("bain");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var weapon = scn.GetFreepsCard("weapon");
		scn.MoveCardsToSupportArea(war);
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(bain);
		scn.AttachCardsTo(balin, weapon);
		scn.MoveCardsToSupportArea(arkenstone);

		assertInZone(Zone.SUPPORT, war, arkenstone);
		assertAttachedTo(weapon, balin);
		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(weapon, balin);
		assertInZone(Zone.SUPPORT, arkenstone);
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowActionAvailable(war));

		scn.ShadowUseCardAction(war);
		assertInDiscard(war);
		assertAttachedTo(weapon, balin);
		assertInZone(Zone.SUPPORT, arkenstone);
		assertEquals(0, scn.GetWoundsOn(bain));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToSupportArea(war);
		scn.MoveAlliesToTable(grimir);

		assertInZone(Zone.SUPPORT, war);
		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(war));
		assertInZone(Zone.SUPPORT, war);
		assertEquals(0, scn.GetWoundsOn(grimir));
	}
}
