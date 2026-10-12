package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunt", "1_126");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("troop", "12_157");     // Uruk-hai Troop: [uruk-hai] Uruk-hai, strength 9, vitality 4, damage +1; the rest of its text is about weapons, and no one here bears one
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, not an Uruk-hai; a regroup ability only ever used on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HuntThemDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hunt Them Down!
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Make an Uruk-hai <b>fierce</b> until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hunt");

		assertEquals("Hunt Them Down!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenUrukHaiOfEitherCultureFierceAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunt = scn.GetShadowCard("hunt");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, troop, servant);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(servant, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(hunt));

		scn.ShadowPlayCard(hunt);
		// An Uruk-hai of the event's culture and one of another; the [isengard] Orc is no Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(troop);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(servant, Keyword.FIERCE));
		assertInDiscard(hunt);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoUrukHaiItIsPlayedForNothingAndTheOrcIsNotMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunt = scn.GetShadowCard("hunt");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// An event is offered whatever its effect will find (CR 4.2 "effect"); the [isengard] Orc is still no Uruk-hai.
		assertTrue(scn.ShadowPlayAvailable(hunt));
		scn.ShadowPlayCard(hunt);

		assertFalse(scn.HasKeyword(servant, Keyword.FIERCE));
		assertInDiscard(hunt);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunt = scn.GetShadowCard("hunt");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.HAND, hunt);
		assertFalse(scn.ShadowPlayAvailable(hunt));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void FierceLastsThroughTheFierceSkirmishAndIsGoneInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunt = scn.GetShadowCard("hunt");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		// The only Uruk-hai: nothing is asked.
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.SkipToAssignments();
		assertFalse(scn.InFierceRound());
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Strength 8 against 5: Aragorn wins and the Savage, vitality 3, takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// A fierce minion is defended against a second time.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, savage);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
