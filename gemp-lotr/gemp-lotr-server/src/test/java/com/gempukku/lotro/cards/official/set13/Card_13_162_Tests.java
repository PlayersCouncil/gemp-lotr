package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_162_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enemy", "13_162");
					put("enemy2", "13_162");
					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai; damage +1 and toil only
					put("saruman", "17_116");    // Saruman, Master of the White Hand: [uruk-hai] Wizard minion; each companion resistance -1; a skirmish ability never used
					put("guard", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1 only
					put("thunder", "4_99");      // Roll of Thunder: a Free Peoples event, only held in the Shadow hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnemyWithoutNumberStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Enemy Without Number
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [uruk-hai] minions.<br><b>Shadow:</b> Discard 2 cards from hand to add (1).<br><b>Regroup:</b> Spot an [uruk-hai] minion and discard this condition from play to add (3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enemy");

		assertEquals("Enemy Without Number", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnceTwoUrukHaiCultureMinionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var enemy2 = scn.GetShadowCard("enemy2");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(enemy);
		scn.MoveCardsToHand(enemy2, saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(tyrannical, guard);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, enemy);
		assertInZone(Zone.HAND, enemy2, saruman);

		scn.FreepsPass();
		// One [uruk-hai] minion, beside a [sauron] Uruk-hai and an [uruk-hai] card that is no minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(enemy2));

		// Saruman, an [uruk-hai] minion who is no Uruk-hai, makes two.
		scn.ShadowPlayCard(saruman);
		assertTrue(scn.ShadowPlayAvailable(enemy2));
		scn.ShadowPlayCard(enemy2);
		assertInZone(Zone.SUPPORT, enemy2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsTwoCardsFromHandToAdd1AndIsNotOfferedWithOneCardLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var guard = scn.GetShadowCard("guard");
		var thunder = scn.GetShadowCard("thunder");
		scn.MoveCardsToSupportArea(enemy);
		scn.MoveCardsToHand(tyrannical, guard, thunder);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, enemy);
		assertInZone(Zone.HAND, tyrannical, guard, thunder);
		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		assertEquals(3, scn.GetShadowHandCount());

		assertTrue(scn.ShadowActionAvailable(enemy));
		scn.ShadowUseCardAction(enemy);
		// Cards of either side and any culture may be discarded; exactly two.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tyrannical, guard, thunder));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(guard, thunder);
		assertInDiscard(guard, thunder);
		assertInZone(Zone.HAND, tyrannical);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, enemy);

		// One card left in hand: the cost cannot be paid.
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enemy));
	}

	@Test
	public void RegroupAbilitySpotsAnUrukHaiAndDiscardsThisConditionToAdd3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var enemy2 = scn.GetShadowCard("enemy2");
		var tyrannical = scn.GetShadowCard("tyrannical");
		scn.MoveCardsToSupportArea(enemy, enemy2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, enemy, enemy2);
		scn.MoveMinionsToTable(tyrannical);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(enemy));
		scn.ShadowUseCardAction(enemy);
		assertInDiscard(enemy);
		assertInZone(Zone.SUPPORT, enemy2);
		assertEquals(twilight + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsOfferedWithSarumanAsTheOnlyUrukHaiCultureMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(enemy);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, enemy);
		scn.MoveMinionsToTable(saruman, guard);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(enemy));
		scn.ShadowUseCardAction(enemy);
		assertInDiscard(enemy);
		assertEquals(twilight + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithoutAnUrukHaiCultureMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var enemy2 = scn.GetShadowCard("enemy2");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(enemy, enemy2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, enemy, enemy2);
		scn.MoveMinionsToTable(guard);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		// A [sauron] Uruk-hai, and another [uruk-hai] card that is no minion.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enemy));
		assertFalse(scn.ShadowActionAvailable(enemy2));
		assertInZone(Zone.SUPPORT, enemy, enemy2);
	}
}
