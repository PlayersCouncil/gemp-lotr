package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "101_49");

					put("warden", "1_259");     // Morgul Warden: a unique [sauron] Orc, vitality 3; stronger for each other [sauron] Orc
					put("swordsman", "3_98");   // Orc Swordsman: a [sauron] Orc that is not unique, vitality 3; stronger while bearing a weapon
					put("trooper", "3_99");     // Orc Trooper: a [sauron] Orc that is not unique, vitality 3; stronger for each card in hand
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("mouth", "12_118");     // The Mouth of Sauron: a [sauron] Man, vitality 3; an assignment ability that is never used
					put("summons", "1_242");    // The Dark Lord's Summons: another [sauron] condition for the support area; a Shadow-phase ability that is never used

					// Light Shining Faintly: "Fellowship: Add a burden to heal a Ring-bound companion twice."
					// The burden that is added by legal play; nobody is wounded, so the healing does nothing.
					put("light", "4_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AShadowofthePastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: A Shadow of the Past
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 4 burdens, each [sauron] Orc is <b>damage +1</b>.
		 * 	While you can spot 6 burdens, each [Sauron] Orc is <b>fierce</b>.
		 * 	At the end of each Shadow phase, exert 2 [sauron] Orcs or hinder this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shadow");

		assertEquals("A Shadow of the Past", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachSauronOrcIsDamagePlusOneWhileFourBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var warden = scn.GetShadowCard("warden");
		var swordsman = scn.GetShadowCard("swordsman");
		var troop = scn.GetShadowCard("troop");
		var mouth = scn.GetShadowCard("mouth");
		var light = scn.GetFreepsCard("light");
		scn.MoveCardsToSupportArea(shadow);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, swordsman, troop, mouth);
		scn.FreepsRefreshDecision();
		scn.AddBurdens(3);
		// Tokens that are not burdens.
		scn.AddThreats(2);

		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.HasKeyword(warden, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.DAMAGE));

		// The fourth burden.
		scn.FreepsPlayCard(light);

		assertEquals(4, scn.GetBurdens());
		// A unique [sauron] Orc and one that is not.
		assertEquals(1, scn.GetKeywordCount(warden, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(swordsman, Keyword.DAMAGE));
		// Not the [moria] Orc, nor the [sauron] Man.
		assertFalse(scn.HasKeyword(troop, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(mouth, Keyword.DAMAGE));
		// Four burdens are not six.
		assertFalse(scn.HasKeyword(warden, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.FIERCE));

		scn.RemoveBurdens(1);

		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.HasKeyword(warden, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSauronOrcIsFierceWhileSixBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var warden = scn.GetShadowCard("warden");
		var swordsman = scn.GetShadowCard("swordsman");
		var troop = scn.GetShadowCard("troop");
		var mouth = scn.GetShadowCard("mouth");
		var light = scn.GetFreepsCard("light");
		scn.MoveCardsToSupportArea(shadow);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, swordsman, troop, mouth);
		scn.FreepsRefreshDecision();
		scn.AddBurdens(5);
		// Tokens that are not burdens.
		scn.AddThreats(2);

		assertEquals(5, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.HasKeyword(warden, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.FIERCE));
		// Five burdens are more than four.
		assertEquals(1, scn.GetKeywordCount(warden, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(swordsman, Keyword.DAMAGE));

		// The sixth burden.
		scn.FreepsPlayCard(light);

		assertEquals(6, scn.GetBurdens());
		// A unique [sauron] Orc and one that is not.
		assertTrue(scn.HasKeyword(warden, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(swordsman, Keyword.FIERCE));
		// Not the [moria] Orc, nor the [sauron] Man.
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(mouth, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(mouth, Keyword.DAMAGE));
		// Six burdens are four burdens once, not twice: the damage bonus is still +1.
		assertEquals(1, scn.GetKeywordCount(warden, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(swordsman, Keyword.DAMAGE));

		scn.RemoveBurdens(1);

		assertEquals(5, scn.GetBurdens());
		assertFalse(scn.HasKeyword(warden, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(warden, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheEndOfTheShadowPhaseAndOfNoOtherTwoSauronOrcsMayBeExertedToKeepTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var warden = scn.GetShadowCard("warden");
		var swordsman = scn.GetShadowCard("swordsman");
		var trooper = scn.GetShadowCard("trooper");
		var troop = scn.GetShadowCard("troop");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();
		// Every minion has vitality 3 and is unwounded, so each of them could exert.
		scn.MoveMinionsToTable(warden, swordsman, trooper, troop, mouth);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsPass();

		// The fellowship phase has ended and the Shadow phase begun: nothing asked, nothing done.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertFalse(scn.IsHindered(shadow));

		scn.ShadowPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Exert");

		// Three [sauron] Orcs, one unique; not the [moria] Orc, nor the [sauron] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warden, swordsman, trooper));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, mouth));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(warden, trooper);

		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(1, scn.GetWoundsOn(trooper));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertFalse(scn.IsHindered(shadow));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The maneuver phase ends with nothing more asked or done.
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(1, scn.GetWoundsOn(trooper));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertFalse(scn.IsHindered(shadow));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtTheEndOfTheShadowPhaseTheConditionMayBeHinderedInsteadAndNoOrcExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var warden = scn.GetShadowCard("warden");
		var swordsman = scn.GetShadowCard("swordsman");
		var trooper = scn.GetShadowCard("trooper");
		var summons = scn.GetShadowCard("summons");
		// Another [sauron] condition beside it.
		scn.MoveCardsToSupportArea(shadow, summons);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, swordsman, trooper);
		scn.AddBurdens(4);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.IsHindered(shadow));
		assertFalse(scn.IsHindered(summons));
		assertEquals(1, scn.GetKeywordCount(warden, Keyword.DAMAGE));

		scn.ShadowPass();

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Hinder");

		assertTrue(scn.IsHindered(shadow));
		assertFalse(scn.IsHindered(summons));
		assertInZone(Zone.SUPPORT, shadow, summons);
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(trooper));
		// A hindered card's game text does not apply: the four burdens no longer make an Orc damage +1.
		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.HasKeyword(warden, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(swordsman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFewerThanTwoSauronOrcsAbleToExertTheConditionIsHinderedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var warden = scn.GetShadowCard("warden");
		var swordsman = scn.GetShadowCard("swordsman");
		var troop = scn.GetShadowCard("troop");
		var mouth = scn.GetShadowCard("mouth");
		var summons = scn.GetShadowCard("summons");
		// Another [sauron] condition beside it.
		scn.MoveCardsToSupportArea(shadow, summons);

		scn.StartGame();
		// One [sauron] Orc that can exert, one that is exhausted, and an Orc and a [sauron] minion that could
		// exert and are not [sauron] Orcs.
		scn.MoveMinionsToTable(warden, swordsman, troop, mouth);
		scn.AddWoundsToChar(swordsman, 2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.IsExhausted(swordsman));
		assertFalse(scn.IsHindered(shadow));
		assertFalse(scn.IsHindered(summons));

		scn.ShadowPass();

		// A player may only choose what can be performed in full, so there is nothing to choose.
		assertTrue(scn.IsHindered(shadow));
		assertFalse(scn.IsHindered(summons));
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(2, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheChoiceIsPutAgainAtTheEndOfASecondShadowPhaseInTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var warden = scn.GetShadowCard("warden");
		var swordsman = scn.GetShadowCard("swordsman");
		var trooper = scn.GetShadowCard("trooper");
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, swordsman, trooper);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Exert");
		scn.ShadowChooseCards(warden, swordsman);

		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertFalse(scn.IsHindered(shadow));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Nobody is assigned, the minions stay, and the fellowship moves on to site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertFalse(scn.IsHindered(shadow));

		scn.ShadowPass();

		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Exert");
		assertTrue(scn.ShadowHasCardChoiceAvailable(warden, swordsman, trooper));
		scn.ShadowChooseCards(warden, swordsman);

		// Vitality 3 with 2 wounds.
		assertEquals(2, scn.GetWoundsOn(warden));
		assertEquals(2, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(trooper));
		assertFalse(scn.IsHindered(shadow));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
