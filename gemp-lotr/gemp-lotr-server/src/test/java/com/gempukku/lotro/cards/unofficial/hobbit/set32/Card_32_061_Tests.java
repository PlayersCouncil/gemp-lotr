package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_061_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	// Enquëa's site is 5, so she roams there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "32_61");

					// Wise characters, and characters who are not Wise
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: Wise [gandalf] companion, cost 1; his other texts want [dwarven] followers or Thorin in hand
					put("elrond", "30_21");     // Elrond, Elven Lord: Wise [elven] ally, cost 4; a start-of-turn heal, a regroup ability not used
					put("galadriel", "32_13");  // Galadriel, Elven Lady: Wise [elven] ally, cost 3; a start-of-turn heal of Gandalf, a skirmish ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, not Wise, the Dwarf who pays Ancestral Knowledge
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a fellowship ability not used
					put("lindir", "33_14");     // Lindir, Elven Minstrel: [elven] Elf ally, not Wise; a heal as the fellowship moves
					// A minion in hand whose cost is read beside Enquëa's
					put("sentinel", "33_49");   // Orc Sentinel: [sauron] Orc, cost 1; its shadow ability wants another [sauron] Orc in play

					// Discards of a condition, and of a card that is none
					put("knowledge", "30_1");   // Ancestral Knowledge: [dwarven] Maneuver event, exert a Dwarf character to discard a condition
					put("tale", "1_317");       // There and Back Again: [shire] condition on a Hobbit companion, "Maneuver: Discard this condition to ..."
					put("acorn", "31_38");      // An Acorn from Beorn's house: [shire] possession on Bilbo, "Regroup: Discard this possession to remove a doubt and discard a Shadow condition"
					put("hatred", "30_34");     // Hatred Rekindled: [gundabad] support-area condition; a Shadow ability not used
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] support-area condition, "Skirmish: Discard this condition to make an Uruk-hai strength +2"
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Úlairë Enquëa, Revived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.
		 * This minion is twilight cost -1 for each Wise character you spot (limit -2).
		 * Each time the Free Peoples player discards a condition, you may exert a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Revived", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EnqueaCostsOneLessForEachWiseCharacterSpottedButNoMoreThanTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var sentinel = scn.GetShadowCard("sentinel");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var thorin = scn.GetFreepsCard("thorin");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(gandalf, elrond, galadriel, enquea, sentinel);

		scn.StartGame();
		scn.MoveAlliesToTable(lindir);
		assertInZone(Zone.SUPPORT, lindir);
		assertInHand(gandalf, elrond, galadriel, enquea, sentinel);
		scn.FreepsRefreshDecision();

		// Neither Thorin nor Lindir is Wise.
		assertEquals(6, scn.GetTwilightCost(enquea));
		assertEquals(1, scn.GetTwilightCost(sentinel));

		scn.FreepsPlayCard(gandalf);
		assertEquals(5, scn.GetTwilightCost(enquea));
		scn.FreepsPlayCard(elrond);
		assertEquals(4, scn.GetTwilightCost(enquea));
		// A third Wise character: the limit is -2.
		scn.FreepsPlayCard(galadriel);
		assertEquals(4, scn.GetTwilightCost(enquea));
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(enquea);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		// 4 + 2 roaming
		assertEquals(pool - 6, scn.GetTwilight());
		assertEquals(1, scn.GetTwilightCost(sentinel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAFreePeoplesEventDiscardsAShadowConditionEnqueaMayExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(enquea);
		// Vitality 3: Balin is exhausted.
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, elrond);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Thorin is the one Dwarf able to exert and Hatred Rekindled the one condition: nothing is asked.
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		// Companions of two cultures and races; not the exhausted one, nor an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, bilbo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, elrond));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndTheNextConditionDiscardedOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var tale = scn.GetFreepsCard("tale");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, tale);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(enquea);
		assertAttachedTo(tale, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Free Peoples player discards his own [shire] condition, as the cost of its own ability.
		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerDiscardingHisOwnConditionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var ambition = scn.GetShadowCard("ambition");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveMinionsToTable(enquea);
		assertInZone(Zone.SUPPORT, ambition);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, enquea);
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		// No Uruk-hai is in play: the condition is discarded for nothing.
		scn.ShadowUseCardAction(ambition);
		assertInZone(Zone.DISCARD, ambition);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardingAPossessionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var acorn = scn.GetFreepsCard("acorn");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.AttachCardsTo(bilbo, acorn);
		scn.SkipToPhase(Phase.REGROUP);
		// Enquëa lived through the skirmishes; no Shadow condition is in play for the Acorn to discard.
		scn.MoveMinionsToTable(enquea);
		scn.AddBurdens(1);
		assertAttachedTo(acorn, bilbo);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(acorn);
		assertInZone(Zone.DISCARD, acorn);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var tale = scn.GetFreepsCard("tale");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, tale);
		scn.MoveMinionsToTable(enquea);
		// Vitality 4 each: three wounds leave nothing to exert.
		scn.AddWoundsToChar(bilbo, 3);
		scn.AddWoundsToChar(thorin, 3);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertAttachedTo(tale, bilbo);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
