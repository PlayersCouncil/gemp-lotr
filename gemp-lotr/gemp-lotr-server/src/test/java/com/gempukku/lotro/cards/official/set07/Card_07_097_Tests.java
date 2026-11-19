package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merchant", "7_97");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6, Knight and no other text
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man companion, strength 6; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, here only a card in hand
					put("bow", "1_41");         // Elven Bow: an [elven] possession, here only a card in hand
					put("defiance", "1_37");    // Defiance: an [elven] Skirmish event, here only a card in hand
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, site 4
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5, vitality 1, site 4; its play trigger never fires when cheated in
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3, site 5, Damage +1 and no other text
					put("fighter1", "7_286");   // Mordor Fighter: while a threat and another [sauron] Orc can be spotted,
					put("fighter2", "7_286");   //   the Shadow has initiative whatever the Free Peoples hand holds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianMerchantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondorian Merchant
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 3T
		 * Game Text: <b>Maneuver:</b> If you have initiative, discard 2 cards from hand to make the site number of a minion +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");

		assertEquals("Gondorian Merchant", merchant.getBlueprint().getTitle());
		assertNull(merchant.getBlueprint().getSubtitle());
		assertTrue(merchant.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, merchant.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, merchant.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, merchant.getBlueprint().getCardType());
		assertEquals(Race.MAN, merchant.getBlueprint().getRace());
		assertEquals(1, merchant.getBlueprint().getTwilightCost());
		assertEquals(6, merchant.getBlueprint().getStrength());
		assertEquals(1, merchant.getBlueprint().getVitality());
		assertTrue(merchant.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
	}

	@Test
	public void ManeuverAbilityDiscardsTwoCardsFromHandToMakeAMinionsSiteNumberPlusTwoUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveAlliesToTable(merchant);
		// A companion, a possession, an event and two Shadow minions in hand.
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		var handRunner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(aragorn, bow, defiance, handTroop, handRunner);
		assertInHand(aragorn, bow, defiance, handTroop, handRunner);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merchant));

		scn.FreepsUseCardAction(merchant);
		// Any two cards of the hand, of either side and any type.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, bow, defiance, handTroop, handRunner));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, handRunner);

		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(bow, defiance, handTroop);
		assertInDiscard(aragorn, handRunner);

		// Any minion, of two cultures and races; no companion and no ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, ranger, frodo, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		// 4 + 2; the Savage is left as printed.
		assertEquals(6, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still in force through the skirmishes: the second skirmish, after one already resolved.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{ranger, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(ranger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetMinionSiteNumber(troop));

		// Gone as the regroup phase starts.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
	}

	@Test
	public void NotOfferedFromThreeCardsInHandAndOfferedFromFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(aragorn, bow, defiance);
		assertInHand(aragorn, bow, defiance);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Three cards, enough to pay the two: but the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merchant));

		// A fourth card: the Free Peoples player has initiative, and only just.
		scn.MoveCardsToHand(handTroop);
		assertInHand(handTroop);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.FreepsActionAvailable(merchant));

		// The initiative is checked before the cost: the hand falls to two, and the effect is still carried out.
		scn.FreepsUseCardAction(merchant);
		scn.FreepsChooseCards(aragorn, bow);
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(6, scn.GetMinionSiteNumber(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheShadowHasInitiativeWhateverTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("bow"), scn.GetFreepsCard("defiance"),
				scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"));

		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter1, fighter2);
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Five cards in hand, enough to pay; but each Mordor Fighter spots a threat and the other [sauron] Orc.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merchant));

		// With the threat gone the Fighters no longer take the initiative, and the ability is offered.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merchant));
	}
}
