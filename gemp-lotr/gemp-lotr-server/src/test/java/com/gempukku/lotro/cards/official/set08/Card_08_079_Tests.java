package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "8_79");
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, vitality 2; its text needs 2 wounds on the Ring-bearer
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl; her text reads [wraith] conditions on companions
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; a skirmish ability never used
					put("answers", "7_185");    // Morgul Answers: a [wraith] condition in the support area; a skirmish ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Úlairë Attëa, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>.<br><b>Regroup:</b> Exert Úlairë Attëa and spot your [wraith] minion to return that minion to your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsHerToReturnYourWraithMinionToYourHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");
		var soldier = scn.GetShadowCard("soldier");
		var answers = scn.GetShadowCard("answers");
		scn.MoveCardsToSupportArea(answers);

		scn.StartGame();
		// No minions on the way, so no skirmish: the minions are laid in the regroup phase, as if they had lived
		// through their skirmishes.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(attea, hound, lemenya, soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, attea, hound, lemenya, soldier);
		assertInZone(Zone.SUPPORT, answers);
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);
		assertEquals(1, scn.GetWoundsOn(attea));

		// Your [wraith] minions of either race, Attëa herself among them ("your [wraith] minion", not "another");
		// not the [sauron] Orc, nor the [wraith] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, hound, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, answers));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInZone(Zone.HAND, hound);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, attea, lemenya, soldier);
		assertInZone(Zone.SUPPORT, answers);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AsTheOnlyWraithMinionSheReturnsHerselfToYourHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var soldier = scn.GetShadowCard("soldier");
		var answers = scn.GetShadowCard("answers");
		scn.MoveCardsToSupportArea(answers);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(attea, soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, attea, soldier);
		assertInZone(Zone.SUPPORT, answers);
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsPass();

		// She is the one [wraith] minion to spot: the ability is hers to use on herself.
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);

		// The one valid candidate is chosen unasked.
		assertInZone(Zone.HAND, attea);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertInZone(Zone.SUPPORT, answers);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, hound);
		assertEquals(0, scn.GetWoundsOn(attea));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(attea));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(attea, hound);
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(attea, 2);
		assertEquals(2, scn.GetWoundsOn(attea));
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(attea));
	}
}
