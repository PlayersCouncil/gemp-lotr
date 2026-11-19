package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("advancing", "12_134");
					put("advancing2", "12_134");
					put("sieger", "18_129");      // White Hand Sieger: an [uruk-hai] Uruk-hai of vitality 1; its only other text is a requirement to play, and it is cheated in
					put("saruman", "17_116");     // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard); each companion is resistance -1
					put("guard", "10_81");        // Cirith Ungol Guard: a [sauron] Uruk-hai; Damage +1 is its whole text
					put("alliance", "12_141");    // Dark Alliance: an [uruk-hai] condition, no minion; a response to a special ability, never used here
					put("defend", "4_115");       // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once (twice if an Uruk-hai)
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the [gondor] Man who exerts for it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AdvancingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Advancing Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br>When you cannot spot another [uruk-hai] minion, discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("advancing");

		assertEquals("Advancing Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.MUSTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithNoOtherUrukHaiMinionItIsDiscardedThoughAnUrukHaiConditionAndASauronUrukHaiAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advancing = scn.GetShadowCard("advancing");
		var alliance = scn.GetShadowCard("alliance");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(advancing);
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(10);
		assertInHand(advancing);
		assertInPlay(alliance, guard);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(advancing));
		scn.ShadowPlayCard(advancing);

		// "Another": the Uruk itself does not count, and neither the [uruk-hai] condition nor the [sauron] Uruk-hai does.
		assertInDiscard(advancing);
		assertInPlay(alliance, guard);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedBesideAnUrukHaiMinionWhoIsNoUrukHaiItStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advancing = scn.GetShadowCard("advancing");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(advancing);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(10);
		assertInHand(advancing);
		assertInPlay(saruman);
		scn.FreepsPass();

		scn.ShadowPlayCard(advancing);

		assertInZone(Zone.SHADOW_CHARACTERS, advancing, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDiscardedWhenTheOtherUrukHaiMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var advancing = scn.GetShadowCard("advancing");
		var sieger = scn.GetShadowCard("sieger");

		scn.StartGame();
		scn.MoveMinionsToTable(advancing, sieger);
		scn.SkipToPhase(Phase.MANEUVER);

		// While the Sieger is in play, the Advancing Uruk stays.
		assertInZone(Zone.SHADOW_CHARACTERS, advancing, sieger);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(advancing, sieger));
		scn.FreepsChooseCard(sieger);

		// The Sieger, vitality 1, is killed; with no other [uruk-hai] minion to spot, the Advancing Uruk is discarded.
		assertInDiscard(sieger, advancing);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoCopiesKeepEachOtherInPlayWhenTheOtherUrukHaiMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var advancing = scn.GetShadowCard("advancing");
		var advancing2 = scn.GetShadowCard("advancing2");
		var sieger = scn.GetShadowCard("sieger");

		scn.StartGame();
		scn.MoveMinionsToTable(advancing, advancing2, sieger);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SHADOW_CHARACTERS, advancing, advancing2, sieger);

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(advancing, advancing2, sieger));
		scn.FreepsChooseCard(sieger);

		// The Sieger is killed; each copy is another [uruk-hai] minion to the other, so both stay.
		assertInDiscard(sieger);
		assertInZone(Zone.SHADOW_CHARACTERS, advancing, advancing2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
