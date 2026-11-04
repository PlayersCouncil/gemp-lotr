package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forges", "18_120");

					put("uruk", "18_131");      // White Hand Uruk: [uruk-hai] Uruk-hai, vitality 2; a requirement to play, cheated in
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai, vitality 4; a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2, damage +1 and nothing else

					// Cards that take [uruk-hai] tokens by their own text; their triggers answer plays this file never makes
					put("roar1", "17_113");     // Deathly Roar: a token for each Free Peoples possession or artifact played
					put("roar2", "17_113");
					put("control", "13_177");   // Weapons of Control: a token for each wounded companion as it is played
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession holding [raider] tokens; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewForgesBuiltStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: New Forges Built
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert an [uruk-hai] minion to reinforce an [uruk-hai] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forges");

		assertEquals("New Forges Built", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukHaiMinionToReinforceAnUrukHaiTokenOnTheCardChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetShadowCard("forges");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var roar1 = scn.GetShadowCard("roar1");
		var roar2 = scn.GetShadowCard("roar2");
		var control = scn.GetShadowCard("control");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(forges);
		assertInHand(forges);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, saruman, guard);
		scn.MoveCardsToSupportArea(roar1, roar2, control, ships);
		scn.AddTokensToCard(roar1, 1);
		scn.AddTokensToCard(control, 2);
		scn.AddTokensToCard(ships, 2);
		assertEquals(1, scn.GetCultureTokensOn(roar1));
		assertEquals(0, scn.GetCultureTokensOn(roar2));
		assertEquals(2, scn.GetCultureTokensOn(control));
		assertEquals(2, scn.GetCultureTokensOn(ships));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(guard));

		assertTrue(scn.ShadowPlayAvailable(forges));
		scn.ShadowPlayCard(forges);

		// The cost: an [uruk-hai] minion of either race, not the [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(guard));

		// The token: either card that holds an [uruk-hai] one; not the Deathly Roar that holds none, nor the
		// [raider] possession holding its own tokens.
		assertTrue(scn.ShadowHasCardChoiceAvailable(roar1, control));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(roar2, ships));
		scn.ShadowChooseCard(roar1);

		assertInDiscard(forges);
		assertEquals(2, scn.GetCultureTokensOn(roar1));
		assertEquals(2, scn.GetCultureTokensOn(control));
		assertEquals(0, scn.GetCultureTokensOn(roar2));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyUrukHaiMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetShadowCard("forges");
		var uruk = scn.GetShadowCard("uruk");
		var guard = scn.GetShadowCard("guard");
		var roar1 = scn.GetShadowCard("roar1");
		scn.MoveCardsToHand(forges);
		assertInHand(forges);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, guard);
		scn.MoveCardsToSupportArea(roar1);
		scn.AddTokensToCard(roar1, 1);
		assertEquals(1, scn.GetCultureTokensOn(roar1));
		// Vitality 2 with 1 wound: exhausted.  The [sauron] Guard can still exert.
		scn.AddWoundsToChar(uruk, 1);
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(forges));
		assertInHand(forges);
		assertEquals(1, scn.GetCultureTokensOn(roar1));
	}

	@Test
	public void WithNoUrukHaiTokenInPlayTheMinionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forges = scn.GetShadowCard("forges");
		var uruk = scn.GetShadowCard("uruk");
		var roar1 = scn.GetShadowCard("roar1");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(forges);
		assertInHand(forges);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(roar1, ships);
		scn.AddTokensToCard(ships, 2);
		assertEquals(0, scn.GetCultureTokensOn(roar1));
		assertEquals(2, scn.GetCultureTokensOn(ships));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(uruk));

		assertTrue(scn.ShadowPlayAvailable(forges));
		scn.ShadowPlayCard(forges);

		// The one [uruk-hai] minion exerts unasked; there is no [uruk-hai] token to reinforce.
		assertInDiscard(forges);
		assertEquals(1, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetCultureTokensOn(roar1));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
