package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_296_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stored", "4_296");

					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally and villager, vitality 2; a regroup ability used only on purpose
					put("deor", "7_222");       // Déor: [rohan] Man ally and villager, vitality 2; a skirmish ability used only on purpose
					put("coif", "12_111");      // Coif: armor a [rohan] Man bears, vitality +1; its triggers answer Shadow twilight and a forest move
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally who is no villager; an archery ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; valiant and nothing else

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WellStoredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Well Stored
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	At the start of each fellowship phase, you may heal a villager.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stored");

		assertEquals("Well Stored", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseHealsOneWoundFromAVillager() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stored = scn.GetFreepsCard("stored");
		var herdsman = scn.GetFreepsCard("herdsman");
		var deor = scn.GetFreepsCard("deor");
		var coif = scn.GetFreepsCard("coif");
		var aldor = scn.GetFreepsCard("aldor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(stored);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman, deor, aldor);
		// The Coif makes the Herdsman vitality 3, so that he can carry 2 wounds and a heal of one shows.
		scn.AttachCardsTo(herdsman, coif);
		scn.AddWoundsToChar(herdsman, 2);
		scn.AddWoundsToChar(deor, 1);
		scn.AddWoundsToChar(aldor, 1);
		scn.AddWoundsToChar(guard, 1);
		assertAttachedTo(coif, herdsman);
		assertEquals(2, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(deor));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stored));
		scn.FreepsAcceptOptionalTrigger();
		// Villagers of two titles; not a [rohan] Man ally who is no villager, or a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(herdsman, deor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aldor, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(deor));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheOfferHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stored = scn.GetFreepsCard("stored");
		var deor = scn.GetFreepsCard("deor");
		scn.MoveCardsToSupportArea(stored);
		scn.MoveAlliesToTable(deor);
		scn.AddWoundsToChar(deor, 1);
		assertEquals(1, scn.GetWoundsOn(deor));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stored));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(deor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoVillagerWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stored = scn.GetFreepsCard("stored");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aldor = scn.GetFreepsCard("aldor");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(stored);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman, aldor);
		scn.AddWoundsToChar(aldor, 1);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.StartGame();

		// The villager is unwounded; the wounded are no villagers.  The heal would do nothing, and is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stored));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(1, scn.GetWoundsOn(guard));
	}

	@Test
	public void IsNotOfferedAtTheStartOfTheShadowOrManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stored = scn.GetFreepsCard("stored");
		var deor = scn.GetFreepsCard("deor");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Laid after the start of the fellowship phase, with a wounded villager.
		scn.MoveCardsToSupportArea(stored);
		scn.MoveAlliesToTable(deor);
		scn.AddWoundsToChar(deor, 1);
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(deor));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(stored));
		assertEquals(1, scn.GetWoundsOn(deor));
	}

	@Test
	public void IsOfferedAgainAtTheStartOfHisNextFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stored = scn.GetFreepsCard("stored");
		var herdsman = scn.GetFreepsCard("herdsman");
		var deor = scn.GetFreepsCard("deor");
		scn.MoveCardsToSupportArea(stored);
		scn.MoveAlliesToTable(herdsman, deor);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(deor, 1);
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(deor));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stored));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(deor);
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(1, scn.GetWoundsOn(herdsman));

		// Turn 3, his next fellowship phase.
		scn.SkipToSite(2);
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stored));
		scn.FreepsAcceptOptionalTrigger();
		// The Herdsman is the one wounded villager, and is healed without a choice.
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
