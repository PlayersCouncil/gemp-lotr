package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("daddy", "13_144");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] companion; strength by the races he spots
					// Pallando, Far-travelling One: another follower, Aid (2); transferred to a [gandalf] companion
					// his own text asks nothing
					put("pallando", "13_37");
					// [shire] events of three phases, which do nothing in a discard pile
					put("appetite", "1_294");     // Hobbit Appetite: [shire] Fellowship event
					put("deftness", "1_293");     // Halfling Deftness: [shire] Skirmish event
					put("intentions", "1_304");   // Noble Intentions: [shire] Skirmish event
					put("stealth", "1_298");      // Hobbit Stealth: [shire] Skirmish event, laid only in the Shadow player's discard pile
					put("trolls", "1_314");       // Stone Trolls: a [shire] condition, no event
					put("wizard", "1_78");        // Mysterious Wizard: a [gandalf] event
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DaddyTwofootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Daddy Twofoot, Next-door Neighbor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Follower
		 * Resistance: +1
		 * Aid: Add a burden
		 * Game Text: <b>Aid</b> – Add a burden. <helper>(At the start of the maneuver phase, you may add a burden to transfer this to a companion.)</helper><br>Each time you transfer this to a companion, you may take a [shire] event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("daddy");

		assertEquals("Daddy Twofoot", card.getBlueprint().getTitle());
		assertEquals("Next-door Neighbor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void EachTimeHisAidTransfersHimToACompanionAShireEventMayBeTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var daddy = scn.GetFreepsCard("daddy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var appetite = scn.GetFreepsCard("appetite");
		var deftness = scn.GetFreepsCard("deftness");
		var intentions = scn.GetFreepsCard("intentions");
		var trolls = scn.GetFreepsCard("trolls");
		var wizard = scn.GetFreepsCard("wizard");
		var stealth = scn.GetShadowCard("stealth");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(daddy);
		scn.MoveCardsToDiscard(appetite, deftness, intentions, trolls, wizard, stealth);

		scn.StartGame();
		// Only there so that there is a maneuver phase; nobody is assigned to it.
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		// Aid - Add a burden.
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(daddy, aragorn);
		assertInDiscard(appetite, deftness, intentions, trolls, wizard, stealth);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy));

		scn.FreepsAcceptOptionalTrigger();

		// The [shire] events in his own discard pile; not a [shire] condition, not a [gandalf] event, and not a
		// [shire] event in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(appetite, deftness, intentions));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trolls, wizard, stealth));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(appetite);

		assertInHand(appetite);
		assertInDiscard(deftness, intentions, trolls, wizard, stealth);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second maneuver phase in the same turn: the Aid taken again gives him to Gimli, and that transfer
		// offers the take again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);

		assertEquals(2, scn.GetBurdens());
		assertAttachedTo(daddy, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(deftness, intentions));
		scn.FreepsChooseCard(deftness);

		assertInHand(appetite, deftness);
		assertInDiscard(intentions, trolls, wizard, stealth);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningTheTakeLeavesTheDiscardPileAndAnotherFollowersTransferOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var daddy = scn.GetFreepsCard("daddy");
		var pallando = scn.GetFreepsCard("pallando");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var appetite = scn.GetFreepsCard("appetite");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(daddy, pallando);
		scn.MoveCardsToDiscard(appetite);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInDiscard(appetite);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsChooseOptionalTrigger(daddy);
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(daddy, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(daddy));
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(appetite);
		assertEquals(0, scn.GetFreepsHandCount());
		assertAttachedTo(daddy, aragorn);

		// Pallando's Aid is still to be answered. His transfer is no transfer of Daddy Twofoot.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsChooseOptionalTrigger(pallando);
		scn.FreepsChooseCard(gandalf);

		assertAttachedTo(pallando, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(daddy));
		assertInDiscard(appetite);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoShireEventInHisDiscardPileTheTransferOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var daddy = scn.GetFreepsCard("daddy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var trolls = scn.GetFreepsCard("trolls");
		var wizard = scn.GetFreepsCard("wizard");
		var stealth = scn.GetShadowCard("stealth");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(daddy);
		// A [shire] card that is no event, an event of another culture, and a [shire] event in the other
		// player's discard pile.
		scn.MoveCardsToDiscard(trolls, wizard, stealth);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInDiscard(trolls, wizard, stealth);
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(daddy, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(daddy));
		assertInDiscard(trolls, wizard, stealth);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
