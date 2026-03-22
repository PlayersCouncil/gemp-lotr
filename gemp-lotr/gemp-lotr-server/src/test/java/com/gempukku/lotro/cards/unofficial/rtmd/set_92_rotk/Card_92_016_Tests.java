package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_016_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Sméagol, Old Noser (5_28): FP companion, strength 3, vitality 4, ring-bound
		put("smeagol", "5_28");
		// Gollum, Nasty Treacherous Creature (5_24): Shadow minion, strength 5, vitality 4
		put("gollum", "5_24");
		// Legolas, Greenleaf (1_50): Elven companion, strength 6, vitality 3, archer
		put("legolas", "1_50");
		// Lurtz, Servant of Isengard (1_127): Isengard Uruk-hai minion, strength 13, vitality 3, archer, damage +1
		put("lurtz", "1_127");
	}};

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_16"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_16", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void GollumAndSmeagolImmuneToArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		// Legolas (FP archer) and Lurtz (Shadow archer) each deal 1 archery wound.
		// Smeagol should be immune to the Shadow archery wound.
		// Gollum should be immune to the FP archery wound.
		// Legolas's arrow must go to Lurtz (only valid shadow target).
		// Lurtz's arrow can go to Frodo or Legolas but NOT Smeagol.
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var legolas = scn.GetFreepsCard("legolas");
		var gollum = scn.GetShadowCard("gollum");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(smeagol, legolas);
		scn.MoveMinionsToTable(gollum, lurtz);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();

		// FP archery total: 1 (Legolas). Shadow archery total: 1 (Lurtz).
		// Gollum can't take wounds, so Legolas's arrow auto-goes to Lurtz.
		// Lurtz's arrow: Smeagol can't take wounds, so only Frodo/Legolas are valid.
		// We should be prompted to assign Lurtz's arrow to Frodo or Legolas.
		assertTrue(scn.FreepsHasCardChoicesAvailable(legolas));
		assertFalse(scn.FreepsHasCardChoicesAvailable(smeagol));

		scn.FreepsChooseCard(legolas);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(lurtz));
	}
}
