package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mystic1", "18_128");
					put("mystic2", "18_128");

					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, strength 5, vitality 2; +3 only while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard, strength 8, vitality 4;
					                             // companions resistance -1, and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, vitality 2, damage +1, no other text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("defend", "4_115");      // Defend It and Hope: [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion
					                             // once (or twice if that minion is an Uruk-hai)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandMysticStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Mystic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Response:</b> If an [uruk-hai] minion is about to take a wound, exert this minion to prevent that wound.
		*/

		var scn = GetScenario();

		var mystic1 = scn.GetFreepsCard("mystic1");

		assertEquals("White Hand Mystic", mystic1.getBlueprint().getTitle());
		assertNull(mystic1.getBlueprint().getSubtitle());
		assertFalse(mystic1.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mystic1.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, mystic1.getBlueprint().getCulture());
		assertEquals(CardType.MINION, mystic1.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, mystic1.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(mystic1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(mystic1, Keyword.DAMAGE));
		assertEquals(4, mystic1.getBlueprint().getTwilightCost());
		assertEquals(8, mystic1.getBlueprint().getStrength());
		assertEquals(3, mystic1.getBlueprint().getVitality());
		assertEquals(5, mystic1.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachUsePreventsOneWoundAndExertsOnlyTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mystic1 = scn.GetShadowCard("mystic1");
		var mystic2 = scn.GetShadowCard("mystic2");
		var hounding = scn.GetShadowCard("hounding");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(mystic1, mystic2, hounding, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(mystic2));
		assertEquals(0, scn.GetWoundsOn(hounding));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsPlayCard(defend);
		// Aragorn, the one [gondor] Man, exerts; Hounding Uruk, an Uruk-hai, is to be wounded twice.
		scn.FreepsChooseCard(hounding);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The first wound: each copy offers its own response.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowChooseOptionalTrigger(mystic1);
		assertEquals(1, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(mystic2));
		assertEquals(0, scn.GetWoundsOn(hounding));
		// Saruman, an [uruk-hai] minion who was not about to be wounded, is not touched.
		assertEquals(0, scn.GetWoundsOn(saruman));

		// "That wound" is one wound: the second is answered again, and taken when declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(hounding));
		assertEquals(1, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(mystic2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AmongTheLosersOfOneSkirmishOnlyUrukHaiCultureMinionsCanBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mystic1 = scn.GetShadowCard("mystic1");
		var hounding = scn.GetShadowCard("hounding");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(mystic1, hounding, saruman, guard);
		// Only so that Aragorn beats all three: 8 + 20 against 5 + 8 + 9.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 20));
		scn.SkipToAssignments();
		// One minion is the Free Peoples player's to assign to a companion; the Shadow player adds the others.
		scn.FreepsAssignToMinions(aragorn, hounding);
		scn.ShadowAssignToMinions(aragorn, saruman, guard);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(hounding));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(guard));

		// The three losers are about to take a wound each, together.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowChooseOptionalTrigger(mystic1);
		assertEquals(1, scn.GetWoundsOn(mystic1));
		// An [uruk-hai] Uruk-hai and an [uruk-hai] Wizard may be saved; a [sauron] Uruk-hai may not, nor an
		// [uruk-hai] minion who is not about to take a wound (the Mystic, in no skirmish).
		assertTrue(scn.ShadowHasCardChoiceAvailable(hounding, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, mystic1));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hounding);

		// A wound is still to be prevented, and the Mystic can exert once more.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowChooseOptionalTrigger(mystic1);
		assertEquals(2, scn.GetWoundsOn(mystic1));
		assertTrue(scn.IsExhausted(mystic1));

		assertEquals(0, scn.GetWoundsOn(hounding));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsAWoundOnTheMysticHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mystic1 = scn.GetShadowCard("mystic1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(mystic1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(mystic1));

		// The only minion, chosen unasked.
		scn.FreepsPlayCard(defend);

		// The Mystic is an Uruk-hai, wounded twice. The first wound: he exerts, and it is prevented.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(mystic1));

		// The second: declined, and taken.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(mystic1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWoundOnAnUrukHaiCultureMinionWhoIsNoUrukHaiIsAnsweredAndPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mystic1 = scn.GetShadowCard("mystic1");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(mystic1, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsPlayCard(defend);
		// Saruman is a Wizard: wounded once, and that one wound alone sets the response off.
		scn.FreepsChooseCard(saruman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mystic1));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWoundOnAMinionOfAnotherCultureIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mystic1 = scn.GetShadowCard("mystic1");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(mystic1, guard);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(guard);

		// The [sauron] Uruk-hai takes both wounds unanswered, though the Mystic could exert: vitality 2, killed.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(guard);
		assertEquals(0, scn.GetWoundsOn(mystic1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheMysticIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mystic1 = scn.GetShadowCard("mystic1");
		var hounding = scn.GetShadowCard("hounding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(mystic1, hounding);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(mystic1, 2);
		assertEquals(2, scn.GetWoundsOn(mystic1));
		assertEquals(0, scn.GetWoundsOn(hounding));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(hounding);

		// Hounding Uruk takes both wounds unanswered: vitality 2, killed.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(hounding);
		assertEquals(2, scn.GetWoundsOn(mystic1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
