package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_126_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hunt", "51_126");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("troop", "12_157");     // Uruk-hai Troop: [uruk-hai] Uruk-hai, strength 9, vitality 4, damage +1; the rest of its text is about weapons, and no one here bears one
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
		 * Twilight Cost: 2
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
		assertEquals(2, card.getBlueprint().getTwilightCost());
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
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hunt));

		scn.ShadowPlayCard(hunt);
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(servant, Keyword.FIERCE));
		assertEquals(3, scn.GetTwilight());     // 5 - 2 to play
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
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hunt));
		scn.ShadowPlayCard(hunt);

		assertFalse(scn.HasKeyword(servant, Keyword.FIERCE));
		assertEquals(3, scn.GetTwilight());     // 5 - 2 to play
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
		assertFalse(scn.ShadowPlayAvailable(hunt));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void FierceLastsThroughTheFierceSkirmishAndIsGoneInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunt = scn.GetShadowCard("hunt");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Strength 9 against 8: the Troop wins and survives, Aragorn takes 1 + 1 for damage.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.PassFierceSkirmishActions();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
