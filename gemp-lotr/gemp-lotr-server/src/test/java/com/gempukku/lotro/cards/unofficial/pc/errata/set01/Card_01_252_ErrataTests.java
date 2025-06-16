package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_252_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "51_252");
					put("orc", "1_266");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheIrresistibleShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: The Irresistible Shadow
		* Unique: True
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 0
		* Type: condition
		* Subtype: Support Area
		* Game Text: To play, exert a [sauron] Orc.
		* 	The Ring-bearer is strength +1 for each burden you can spot.
		* 	If you cannot spot another companion, the Ring-bearer is resistance -2 for each burden you can spot.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var shadow = scn.GetFreepsCard("shadow");

		assertTrue(shadow.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shadow.getBlueprint().getSide());
		assertEquals(Culture.SAURON, shadow.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, shadow.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(shadow, Keyword.SUPPORT_AREA));
		assertEquals(0, shadow.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowRequiresOrcExertion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(shadow);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.AddWoundsToChar(orc, 1);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertFalse(scn.ShadowPlayAvailable(shadow));
	}

	@Test
	public void ShadowExertsOrcToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(shadow);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(orc));
		assertTrue(scn.ShadowPlayAvailable(shadow));
		scn.ShadowPlayCard(shadow);
		assertEquals(1, scn.GetWoundsOn(orc));
	}

	@Test
	public void ShadowMakesRBStrengthPlusOnePerBurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var shadow = scn.GetShadowCard("shadow");
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();

		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(frodo)); // 3 base + 1 ring + 1 from Irresistable Shadow
		scn.AddBurdens(1);
		assertEquals(6, scn.GetStrength(frodo)); // 3 base + 1 ring + 2 from Irresistable Shadow
	}

	@Test
	public void ShadowMakesRBResMinus2PerBurdenIfNoOtherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		var shadow = scn.GetShadowCard("shadow");
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();

		assertEquals(1, scn.GetBurdens());
		assertEquals(7, scn.GetResistance(frodo)); // 10 base - 1 burden - 2 from Irresistable Shadow
		scn.AddBurdens(1);
		assertEquals(4, scn.GetResistance(frodo)); // 10 base - 2 burdens - 4 from Irresistable Shadow
		scn.MoveCompanionToTable(aragorn);
		assertEquals(8, scn.GetResistance(frodo)); // 10 base - 2 burdens - 0 from Irresistable Shadow
	}
}
