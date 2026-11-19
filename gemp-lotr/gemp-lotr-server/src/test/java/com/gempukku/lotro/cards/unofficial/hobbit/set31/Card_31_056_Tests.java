package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_056_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer, and
	// Bert's site is 2, so he is not roaming there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bert", "31_56");

					// Minions to discard for the play, and not
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, cost 1; a maneuver ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc; an end-of-Shadow-phase offer with nothing to take
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; texts on assigning him, a regroup ability
					// Cards in hand whose cost is read
					put("william", "31_61");    // William, Troll of Ettenmoors: [troll] Troll, cost 5; his texts work only in play
					put("demolition", "32_31"); // Demolition Troll: [moria] Troll, cost 5; a skirmish ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an assignment ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, the Dwarf who pays Ancestral Knowledge
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal with nothing to heal, a regroup ability not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a regroup ability not used

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
	public void BertStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Bert, Troll of Ettenmoors
		 * Unique: True
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>. To play, discard an Orc.
		 * The twilight cost of each Troll is -2.
		 * Each time the Free Peoples player discards a condition, you may discard an ally.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("bert");

		assertEquals("Bert", card.getBlueprint().getTitle());
		assertEquals("Troll of Ettenmoors", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingBertDiscardsAnOrcAndCostsHisFullFiveThenEachTrollInHandCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var yazneg = scn.GetShadowCard("yazneg");
		var malevolent = scn.GetShadowCard("malevolent");
		var ferny = scn.GetShadowCard("ferny");
		var william = scn.GetShadowCard("william");
		var demolition = scn.GetShadowCard("demolition");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(bert, william, demolition, aggressor);

		scn.StartGame();
		assertInZone(Zone.HAND, bert);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(yazneg, malevolent, ferny);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilightCost(william));
		assertEquals(5, scn.GetTwilightCost(demolition));
		assertEquals(2, scn.GetTwilightCost(aggressor));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(bert));
		scn.ShadowPlayCard(bert);
		assertTrue(scn.ShadowHasCardChoiceAvailable(yazneg, malevolent));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);
		assertInZone(Zone.DISCARD, yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, malevolent);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, bert);
		// "Each Troll" does not name Bert, so it does not lower his own cost as he is played: 5, not 3.
		assertEquals(pool - 5, scn.GetTwilight());
		// With Bert in play: 5 - 2 for each Troll, of either culture; the Orc is not lowered.
		assertEquals(3, scn.GetTwilightCost(william));
		assertEquals(3, scn.GetTwilightCost(demolition));
		assertEquals(2, scn.GetTwilightCost(aggressor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BertCannotBePlayedUntilAnOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToHand(bert, yazneg);

		scn.StartGame();
		assertInZone(Zone.HAND, bert);
		assertInZone(Zone.HAND, yazneg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(ferny);
		scn.FreepsPass();

		// Bill Ferny Sr. is no Orc, and the Orc in hand is not in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bert));
		scn.ShadowPlayCard(yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertTrue(scn.ShadowPlayAvailable(bert));
		scn.ShadowPlayCard(bert);
		// The one Orc is discarded with nothing asked.
		assertInZone(Zone.DISCARD, yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, bert);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenAFreePeoplesEventDiscardsAShadowConditionBertMayDiscardAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, bard);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(bert);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, elrond);
		assertInZone(Zone.SUPPORT, bard);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Thorin is the one Dwarf to exert and Hatred Rekindled the one condition: nothing is asked.
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bert));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, bard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thorin, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(elrond);
		assertInZone(Zone.DISCARD, elrond);
		assertInZone(Zone.SUPPORT, bard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesTheAlliesAndTheNextConditionDiscardedOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var hatred = scn.GetShadowCard("hatred");
		var knowledge = scn.GetFreepsCard("knowledge");
		var tale = scn.GetFreepsCard("tale");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(knowledge);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, tale);
		scn.MoveAlliesToTable(elrond, bard);
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveMinionsToTable(bert);
		assertAttachedTo(tale, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		assertInZone(Zone.SUPPORT, elrond);
		assertInZone(Zone.SUPPORT, bard);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Free Peoples player discards his own [shire] condition, as the cost of its own ability.
		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bert));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, elrond);
		assertInZone(Zone.SUPPORT, bard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(knowledge);
		assertInZone(Zone.DISCARD, hatred);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bert));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(bard);
		assertInZone(Zone.DISCARD, bard);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerDiscardingHisOwnConditionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var ambition = scn.GetShadowCard("ambition");
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveMinionsToTable(bert);
		assertInZone(Zone.SUPPORT, ambition);
		assertInZone(Zone.SUPPORT, elrond);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, bert);
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		// No Uruk-hai is in play: the condition is discarded for nothing.
		scn.ShadowUseCardAction(ambition);
		assertInZone(Zone.DISCARD, ambition);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bert));
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardingAPossessionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var acorn = scn.GetFreepsCard("acorn");
		var elrond = scn.GetFreepsCard("elrond");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveAlliesToTable(elrond);
		scn.SkipToPhase(Phase.REGROUP);
		// Bert lived through the skirmishes; no Shadow condition is in play for the Acorn to discard.
		scn.MoveMinionsToTable(bert);
		scn.AddBurdens(1);
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, elrond);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(acorn);
		assertInZone(Zone.DISCARD, acorn);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bert));
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoAllyInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bert = scn.GetShadowCard("bert");
		var tale = scn.GetFreepsCard("tale");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.AttachCardsTo(bilbo, tale);
		scn.MoveMinionsToTable(bert);
		assertAttachedTo(tale, bilbo);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bert));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
