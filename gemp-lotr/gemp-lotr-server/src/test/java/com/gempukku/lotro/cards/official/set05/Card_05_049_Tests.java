package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_05_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("devilry", "5_49");
					// Scaling Ladder: another [isengard] machine that places tokens on itself; its abilities are used
					// only on purpose
					put("ladder", "5_57");

					put("troop", "1_143");      // Troop of Uruk-hai: [isengard] Uruk-hai, cost 5, site 5, vitality 4; damage +1, weapons give no bonus against it
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("legion", "17_129");    // White Hand Legion: [uruk-hai] Uruk-hai, strength 17, vitality 3; damage +1, to play remove 2 threats (cheated in)
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; "Shadow: Exert this minion to add (1)", used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; his maneuver ability is never used
					put("knight", "5_35");      // Gondorian Knight: vitality 3, no text but knight
					// Elrond, Lord of Rivendell: ally, vitality 4; his fellowship action is never used, and his
					// start-of-turn heal comes before any wound is laid
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DevilryofOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Devilry of Orthanc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Machine</b>. Plays to your support area.
		 * <b>Shadow:</b> Exert an Uruk-hai to place an [isengard] token on this card.
		 * <b>Maneuver:</b> Spot 8 [isengard] tokens here to exert every character. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("devilry");

		assertEquals("Devilry of Orthanc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukHaiToPlaceATokenUntilNoUrukHaiCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var troop = scn.GetShadowCard("troop");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(devilry);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, devilry);
		assertInHand(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(forger);
		scn.ShadowRefreshDecision();

		// No Uruk-hai to exert, only an [isengard] Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(devilry));
		scn.ShadowPlayCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(devilry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(devilry));

		scn.ShadowUseCardAction(devilry);
		assertEquals(1, scn.GetCultureTokensOn(devilry));
		assertEquals(1, scn.GetWoundsOn(troop));
		// Placing a token is not what discards it.
		assertInZone(Zone.SUPPORT, devilry);
		assertTrue(scn.ShadowActionAvailable(devilry));

		scn.ShadowUseCardAction(devilry);
		assertEquals(2, scn.GetCultureTokensOn(devilry));
		assertEquals(2, scn.GetWoundsOn(troop));

		// No limit is printed: a third token in the same phase.
		assertTrue(scn.ShadowActionAvailable(devilry));
		scn.ShadowUseCardAction(devilry);
		assertEquals(3, scn.GetCultureTokensOn(devilry));
		assertEquals(3, scn.GetWoundsOn(troop));

		// Vitality 4 with 3 wounds: the Uruk-hai cannot pay again, and the Orc, who could exert, may not.
		assertEquals(0, scn.GetWoundsOn(forger));
		assertFalse(scn.IsExhausted(forger));
		assertFalse(scn.ShadowActionAvailable(devilry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnyCultureMayExertAndTheTokenGoesOnThisCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var ladder = scn.GetShadowCard("ladder");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(devilry, ladder);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, devilry, ladder);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, savage, legion, forger);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(troop, 3);
		assertEquals(3, scn.GetWoundsOn(troop));
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(devilry);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);
		assertEquals(1, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(forger));
		// On this card, not on the other machine.
		assertEquals(1, scn.GetCultureTokensOn(devilry));
		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEightTokensHereExertsEveryCharacterButTheExhaustedAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(devilry, ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// It places its tokens on itself, one at a time.
		scn.AddTokensToCard(devilry, 8);
		scn.AddTokensToCard(ladder, 1);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(knight, 2);
		assertInZone(Zone.SUPPORT, devilry, ladder);
		assertEquals(8, scn.GetCultureTokensOn(devilry));
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(devilry));
		scn.ShadowUseCardAction(devilry);

		// Companions, the ally and the Shadow player's own minion each exert once.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(savage));
		// The exhausted Knight cannot exert, and is not wounded either.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertInZone(Zone.FREE_CHARACTERS, knight);
		// It discards itself; the other machine stays.
		assertInZone(Zone.DISCARD, devilry);
		assertInZone(Zone.SUPPORT, ladder);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithSevenTokensHereAndAnotherOnAMachineBeside() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var devilry = scn.GetShadowCard("devilry");
		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(devilry, ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Eight [isengard] tokens on machines, seven of them here.
		scn.AddTokensToCard(devilry, 7);
		scn.AddTokensToCard(ladder, 1);
		assertInZone(Zone.SUPPORT, devilry);
		assertEquals(7, scn.GetCultureTokensOn(devilry));
		assertEquals(1, scn.GetCultureTokensOn(ladder));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(devilry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, devilry);
	}
}
