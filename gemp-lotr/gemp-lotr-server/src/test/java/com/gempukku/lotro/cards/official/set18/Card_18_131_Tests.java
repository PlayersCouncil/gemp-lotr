package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "18_131");

					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, cost 2, strength 5; +3 while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard; companions resistance -1,
					                             // and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1, no other text
					put("endless", "13_161");    // Endless Assault: an [uruk-hai] condition; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>To play, spot an [uruk-hai] minion.
		*/

		var scn = GetScenario();

		var uruk = scn.GetFreepsCard("uruk");

		assertEquals("White Hand Uruk", uruk.getBlueprint().getTitle());
		assertNull(uruk.getBlueprint().getSubtitle());
		assertFalse(uruk.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, uruk.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, uruk.getBlueprint().getCulture());
		assertEquals(CardType.MINION, uruk.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, uruk.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(uruk, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(uruk, Keyword.DAMAGE));
		assertEquals(3, uruk.getBlueprint().getTwilightCost());
		assertEquals(10, uruk.getBlueprint().getStrength());
		assertEquals(2, uruk.getBlueprint().getVitality());
		assertEquals(5, uruk.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiCultureMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var hounding = scn.GetShadowCard("hounding");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");

		scn.StartGame();
		scn.MoveCardsToHand(uruk, hounding);
		// A [sauron] Uruk-hai on the table, and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(guard);
		scn.MoveCardsToSupportArea(endless);
		scn.SetTwilight(7);
		assertInHand(uruk, hounding);
		assertInZone(Zone.SUPPORT, endless);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(uruk));

		scn.ShadowPlayCard(hounding);
		assertTrue(scn.ShadowPlayAvailable(uruk));

		scn.ShadowPlayCard(uruk);
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyUrukHaiCultureMinionIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToHand(uruk);
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(7);
		assertInHand(uruk);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(uruk));
		scn.ShadowPlayCard(uruk);
		assertInZone(Zone.SHADOW_CHARACTERS, uruk);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
