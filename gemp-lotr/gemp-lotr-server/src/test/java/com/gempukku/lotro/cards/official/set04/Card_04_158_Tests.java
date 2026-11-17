package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lieutenant", "4_158");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, Damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2, Damage +1 and nothing else
					put("chaser", "4_181");     // Uruk Chaser: [isengard] Uruk-hai printed fierce; stronger in a fierce skirmish only
					put("raiders", "4_207");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1
					put("forger", "3_56");      // Isengard Forger: an [isengard] Orc, no Uruk-hai; a Shadow ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LieutenantofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Lieutenant of Orthanc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Exert Lieutenant of Orthanc to make another Uruk-hai <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lieutenant");

		assertEquals("Lieutenant of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsHimToMakeAnotherUrukHaiFierceUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var chaser = scn.GetShadowCard("chaser");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant, savage, sentry, chaser, forger);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lieutenant));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(lieutenant));
		scn.ShadowUseCardAction(lieutenant);

		// Uruk-hai of two cultures, and one fierce already; not the Lieutenant himself or an [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, sentry, chaser));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lieutenant, forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(forger, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lieutenant, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(lieutenant);
		scn.ShadowChooseCard(sentry);
		assertEquals(2, scn.GetWoundsOn(lieutenant));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lieutenant));
	}

	@Test
	public void FierceLastsThroughTheFierceSkirmishAndEndsAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var raiders = scn.GetShadowCard("raiders");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant, raiders);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		// The Raiding Party is the one other Uruk-hai: chosen with nothing asked.
		scn.ShadowUseCardAction(lieutenant);
		assertEquals(1, scn.GetWoundsOn(lieutenant));
		assertTrue(scn.HasKeyword(raiders, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		// The Lieutenant fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Strength 9 beats Aragorn's 8: no wound for the Uruk-hai, 1 + damage 1 for Aragorn.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(raiders, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, raiders);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, raiders));
		assertTrue(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lieutenant, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
	}
}
