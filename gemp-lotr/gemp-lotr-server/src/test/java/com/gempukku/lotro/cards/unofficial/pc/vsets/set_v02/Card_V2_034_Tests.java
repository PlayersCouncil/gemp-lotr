package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("occupier", "102_34");
					put("filler", "1_3");
					put("trail", "102_31");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukOccupierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uruk Occupier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Damage +1.
		* 	This minion is strength +1 for each battleground you control.
		* 	Shadow: If you control 3 battlegrounds, discard a card from hand to play this minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("occupier");

		assertEquals("Uruk Occupier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukOccupierIsStrengthPlus1PerControlledBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var trail = scn.GetShadowCard("trail");
		scn.MoveCardsToDiscard(occupier);
		scn.MoveCardsToSupportArea(trail); //makes all controlled sites battleground

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPassCurrentPhaseAction();

		scn.MoveMinionsToTable(occupier);
		assertEquals(8, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(9, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(10, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(11, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(12, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(13, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(14, scn.GetStrength(occupier));
		scn.ShadowTakeControlOfSite();
		assertEquals(15, scn.GetStrength(occupier));
	}

	@Test
	public void UrukOccupierDiscardsCardFromHandToPlayFromDiscardIf3BattlegroundsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var occupier = scn.GetShadowCard("occupier");
		var trail = scn.GetShadowCard("trail");
		var filler = scn.GetShadowCard("filler");
		scn.MoveCardsToDiscard(occupier);
		scn.MoveCardsToSupportArea(trail); //makes all controlled sites battleground

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(occupier));
		scn.ShadowTakeControlOfSite();
		assertFalse(scn.ShadowActionAvailable(occupier));
		scn.ShadowTakeControlOfSite();
		assertFalse(scn.ShadowActionAvailable(occupier));
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowActionAvailable(occupier));

		assertEquals(Zone.HAND, filler.getZone());
		assertEquals(Zone.DISCARD, occupier.getZone());

		scn.ShadowUseCardAction(occupier);
		assertEquals(Zone.DISCARD, filler.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, occupier.getZone());
	}
}
