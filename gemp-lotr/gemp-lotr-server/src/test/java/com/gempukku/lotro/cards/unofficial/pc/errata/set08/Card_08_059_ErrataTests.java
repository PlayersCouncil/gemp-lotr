package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_08_059_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("galley", "58_59");
					put("raider", "4_249");

					put("chaff1", "4_250");
					put("chaff2", "4_251");
					put("chaff3", "4_252");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void CorsairWarGalleyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 8
		* Title: Corsair War Galley
		* Unique: True
		* Side: SHADOW
		* Culture: Raider
		* Twilight Cost: 1
		* Type: possession
		* Subtype: Support Area
		* Game Text: When you play this possession, you may add a [raider] token here.
		* 	While you can spot 6 [raider] tokens and a [raider] Man, the Shadow has initiative, regardless of the Free Peoples player’s hand. <br><b>Regroup:</b> Add (1) for each [raider] token you can spot. Discard this possession.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var galley = scn.GetFreepsCard("galley");

		assertTrue(galley.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, galley.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, galley.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, galley.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(galley, Keyword.SUPPORT_AREA));
		assertEquals(1, galley.getBlueprint().getTwilightCost());
	}

	@Test
	public void GalleyAddsTokenOnPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		scn.ShadowMoveCardToHand(galley);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(galley));
		scn.ShadowPlayCard(galley);

		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(galley));
	}

	@Test
	public void ShadowHasInitiativeIf6TokensAndARaider() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var raider = scn.GetShadowCard("raider");
		scn.ShadowMoveCardToHand(raider);
		scn.ShadowMoveCardToSupportArea(galley);

		scn.StartGame();

		scn.FreepsDrawCards(4);
		scn.SetTwilight(20);

		scn.FreepsPassCurrentPhaseAction();

		// Neither 6 tokens nor a raider man: Freeps has initiative
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(Zone.HAND, raider.getZone());
		assertTrue(scn.FreepsHasInitiative());

		// 6 tokens, but no raider man: Freeps has initiative
		scn.AddTokensToCard(galley, 6);
		assertEquals(6, scn.GetCultureTokensOn(galley));
		assertEquals(Zone.HAND, raider.getZone());
		assertTrue(scn.FreepsHasInitiative());

		// 6 tokens on galley and a raider man: Shadow has initiative
		scn.ShadowPlayCard(raider);
		assertEquals(6, scn.GetCultureTokensOn(galley));
		assertEquals(Zone.SHADOW_CHARACTERS, raider.getZone());
		assertTrue(scn.ShadowHasInitiative());

		// 5 tokens on galley and a raider man: Freeps has initiative
		scn.RemoveTokensFromCard(galley, 1);
		assertEquals(5, scn.GetCultureTokensOn(galley));
		assertEquals(Zone.SHADOW_CHARACTERS, raider.getZone());
		assertTrue(scn.FreepsHasInitiative());

		// 6 tokens between all raider cards and a raider man: Shadow has initiative
		scn.AddTokensToCard(raider, 1);
		assertEquals(5, scn.GetCultureTokensOn(galley));
		assertEquals(1, scn.GetCultureTokensOn(raider));
		assertEquals(Zone.SHADOW_CHARACTERS, raider.getZone());
		assertTrue(scn.ShadowHasInitiative());
	}

	@Test
	public void RegroupActionAdds1TwilightPerRaiderTokenAndSelfDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var galley = scn.GetShadowCard("galley");
		var raider = scn.GetShadowCard("raider");
		scn.ShadowMoveCardToHand(raider);
		scn.ShadowMoveCardToSupportArea(galley);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.REGROUP);

		scn.ShadowMoveCharToTable(raider);
		scn.AddTokensToCard(raider, 3);
		scn.AddTokensToCard(galley, 3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(Zone.SUPPORT, galley.getZone());

		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(galley));
		scn.ShadowUseCardAction(galley);
		assertEquals(9, scn.GetTwilight());
		assertEquals(Zone.DISCARD, galley.getZone());
	}
}
