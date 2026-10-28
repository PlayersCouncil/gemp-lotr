package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunlending", "12_65");
					put("dunlending2", "12_65");
					put("axeman", "11_69");     // Axeman of Harad: another [men] Man minion; a cost reduction only
					put("dagger", "18_69");     // Henchman's Dagger: [men] hand weapon, cost 0, bearer must be a [men] minion
					put("pavise", "11_94");     // Pavise: [men] shield, bearer must be a [men] minion; cannot take wounds except in a skirmish
					put("lethim", "7_63");      // Let Her Deal With Them: [gollum] condition borne by a minion, bearer damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrenziedDunlendingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Frenzied Dunlending
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While this minion is bearing a possession, it is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dunlending");

		assertEquals("Frenzied Dunlending", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceOnceAPossessionIsPlayedOnItAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var axeman = scn.GetShadowCard("axeman");
		var dagger = scn.GetShadowCard("dagger");
		var pavise = scn.GetShadowCard("pavise");
		var lethim = scn.GetShadowCard("lethim");

		scn.StartGame();
		scn.SetShadowHand(dagger);
		scn.MoveMinionsToTable(dunlending, dunlending2, axeman);
		// The second copy bears a condition, which is no possession; the Axeman bears a possession.
		scn.AttachCardsTo(dunlending2, lethim);
		scn.AttachCardsTo(axeman, pavise);
		assertInHand(dagger);
		assertInZone(Zone.ATTACHED, lethim, pavise);
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(dunlending2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(dagger);
		assertTrue(scn.ShadowHasCardChoiceAvailable(dunlending, dunlending2, axeman));
		scn.ShadowChooseCard(dunlending);

		assertInZone(Zone.ATTACHED, dagger);
		assertTrue(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(dunlending2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShieldIsAPossessionAsAWeaponIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dunlending = scn.GetShadowCard("dunlending");
		var pavise = scn.GetShadowCard("pavise");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending);
		assertFalse(scn.HasKeyword(dunlending, Keyword.FIERCE));
		scn.AttachCardsTo(dunlending, pavise);
		assertInZone(Zone.ATTACHED, pavise);

		assertTrue(scn.HasKeyword(dunlending, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
