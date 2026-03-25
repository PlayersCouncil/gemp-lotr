package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * 93_2: Your Ring-bearer (except Frodo or Sam) is resistance +3.
 *
 * Owner-gated to Freeps.
 * Uses GimliRB as a non-Frodo/non-Sam ring-bearer to test the positive case.
 * Also tests Frodo to confirm the exclusion works.
 */
public class Card_93_002_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
	}};

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing,
				"93_2", null
		);
	}

	protected VirtualTableScenario GetFrodoScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_2", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing,
				null, "93_2"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_2", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void GimliRBGetsResistanceBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();
		var gimli = scn.GetRingBearer();

		scn.StartGame();

		// Gimli RB: 4 base resistance + 1 (damage +1 → resistance +1) = 5
		// With mod: 5 + 3 = 8
		assertEquals(8, scn.GetResistance(gimli));
	}

	@Test
	public void FrodoDoesNotGetResistanceBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrodoScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();

		// Frodo is excluded — base resistance 10, no bonus
		assertEquals(10, scn.GetResistance(frodo));
	}

	@Test
	public void DoesNotApplyWhenShadowOwns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var gimli = scn.GetRingBearer();

		scn.StartGame();

		// Shadow owns the mod — Gimli should not get the bonus
		// Base resistance: 4+1
		assertEquals(5, scn.GetResistance(gimli));
	}
}
