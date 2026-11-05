package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_059_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Player 2 holds Dragon-Sickness; player 1's fellowship moves on turn 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sickness", "33_59");
					// Thorin, Oakenshield: [dwarven] companion, strength 7; +1 for each [dwarven] follower he bears
					// (none here).
					put("thorin", "30_19");
					// [dwarven] Dwarf companions: one who is no Thorin, read beside him; others for the dead pile.
					put("balin", "30_2");        // a Fellowship ability, not used
					put("dwalin", "30_9");       // a Maneuver ability, not used
					put("fili", "30_10");        // a Response to a [dwarven] companion's wound, not used
					// Dain Ironfoot, Lord of the Iron Hills: a [dwarven] Dwarf ALLY, for the dead pile.
					put("dain", "32_2");
					// Gandalf, Leader of Dwarves: a [gandalf] companion, for the dead pile.
					put("gandalf", "30_25");
					// Goblin Patrol Troop: [moria] Orc, strength 13, no text.  It beats Balin in a skirmish.
					put("troop", "1_177");
					// Minions for the discard pile: an Orc and a Troll, of two cultures.
					put("veteran", "32_43");     // Orkish Veteran: [sauron] Orc, cost 2, site 4
					put("demolition", "32_31");  // Demolition Troll: [moria] Troll, cost 5, site 4
					// Hatred Rekindled: a [gundabad] condition, cost 3, nothing needed to play it.
					put("hatred", "30_34");
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DragonSicknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Dragon-Sickness
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug (no culture icon is printed on the full-art picture)
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Thorin is strength -1 for each [dwarven] character in the dead pile.
		 * 	<b>Shadow:</b> Discard this condition to play a minion from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sickness");

		assertEquals("Dragon-Sickness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ThorinIsStrengthMinus1ForEachDwarvenCharacterInTheFreePeoplesDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sickness = scn.GetShadowCard("sickness");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowFili = scn.GetShadowCard("fili");
		scn.MoveCompanionsToTable(thorin, balin);
		// The Free Peoples player's dead pile: a [dwarven] companion, a [dwarven] ally and a [gandalf] companion.
		scn.MoveCardsToDeadPile(dwalin, dain, gandalf);
		// The Shadow player's own dead pile: a [dwarven] companion.
		scn.MoveCardsToDeadPile(shadowFili);

		scn.StartGame();
		assertInZone(Zone.DEAD, dwalin, dain, gandalf, shadowFili);
		assertEquals(3, scn.GetFreepsDeadCount());
		// Without Dragon-Sickness.
		assertEquals(7, scn.GetStrength(thorin));
		assertEquals(7, scn.GetStrength(balin));

		scn.MoveCardsToSupportArea(sickness);
		assertInZone(Zone.SUPPORT, sickness);

		// 7 - 2: Dwalin and Dain are counted; Gandalf, of another culture, is not, nor is the Shadow player's Fili.
		assertEquals(5, scn.GetStrength(thorin));
		// Balin is no Thorin.
		assertEquals(7, scn.GetStrength(balin));
	}

	@Test
	public void ADwarvenCompanionKilledInASkirmishMakesThorinWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sickness = scn.GetShadowCard("sickness");
		var troop = scn.GetShadowCard("troop");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveCardsToSupportArea(sickness);
		// Balin has 1 vitality left.
		scn.AddWoundsToChar(balin, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sickness);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetFreepsDeadCount());
		// Nothing in the dead pile: 7.
		assertEquals(7, scn.GetStrength(thorin));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, troop);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// Balin loses to the Troop and is killed.
		assertInZone(Zone.DEAD, balin);
		assertEquals(1, scn.GetFreepsDeadCount());
		// 7 - 1
		assertEquals(6, scn.GetStrength(thorin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowActionDiscardsThisConditionToPlayAMinionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sickness = scn.GetShadowCard("sickness");
		var veteran = scn.GetShadowCard("veteran");
		var demolition = scn.GetShadowCard("demolition");
		var hatred = scn.GetShadowCard("hatred");
		// The Free Peoples player's Goblin Patrol Troop, a minion in HIS discard pile, whose title the Shadow
		// player's discard pile does not hold (the Shadow player's copy is in his draw deck).
		var freepsTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToSupportArea(sickness);
		scn.MoveCardsToDiscard(veteran, demolition, hatred, freepsTroop);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sickness);
		assertInZone(Zone.DISCARD, veteran, demolition, hatred, freepsTroop);

		scn.FreepsPass();
		// Enough for any of them.
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(sickness));
		scn.ShadowUseCardAction(sickness);

		assertInZone(Zone.DISCARD, sickness);
		// Minions of two cultures and races; not a Shadow condition, nor the other player's minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, demolition));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hatred, freepsTroop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(demolition);

		assertInZone(Zone.SHADOW_CHARACTERS, demolition);
		assertInZone(Zone.DISCARD, veteran, hatred, freepsTroop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithNoMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sickness = scn.GetShadowCard("sickness");
		var veteran = scn.GetShadowCard("veteran");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCardsToSupportArea(sickness);
		scn.MoveCardsToDiscard(hatred);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sickness);
		assertInZone(Zone.DISCARD, hatred);

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// The condition could be discarded and the pool could pay for Hatred Rekindled.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sickness));

		// A minion in the discard pile, and it is offered.
		scn.MoveCardsToDiscard(veteran);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, veteran);
		assertTrue(scn.ShadowActionAvailable(sickness));
	}

	@Test
	public void ShadowActionIsNotOfferedUntilThePoolCanPayForTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sickness = scn.GetShadowCard("sickness");
		var demolition = scn.GetShadowCard("demolition");
		scn.MoveCardsToSupportArea(sickness);
		scn.MoveCardsToDiscard(demolition);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sickness);
		assertInZone(Zone.DISCARD, demolition);

		scn.FreepsPass();
		// One short of 5 + 2 roaming at site 2.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sickness));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(sickness));
		scn.ShadowUseCardAction(sickness);

		// The one minion is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, demolition);
		assertInZone(Zone.DISCARD, sickness);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
