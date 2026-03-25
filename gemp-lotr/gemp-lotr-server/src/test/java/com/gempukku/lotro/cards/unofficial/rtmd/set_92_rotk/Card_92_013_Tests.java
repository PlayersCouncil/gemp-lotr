package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_013_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Hides (4_19): support area possession, twilight 1
		put("hides", "4_19");
		// Aragorn, Ranger of the North (1_89): Gondor companion, twilight 4, vitality 4
		put("aragorn", "1_89");
		// Gimli, Dwarf of Erebor (1_13): Dwarven companion, twilight 2, vitality 3
		put("gimli", "1_13");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_13", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_13"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_13
		 * Type: MetaSite
		 * Game Text: Shadow: Play a possession to make the Free Peoples player exert a companion.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_13", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ShadowPlaysAPossessionToExertCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var hides = scn.GetShadowCard("hides");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(hides);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Shadow plays hides as cost
		scn.ShadowChooseCardBPFromSelection(hides);

		// FP player chooses which companion to exert
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WorksWhenOwnedByFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// Not owner-gated, so shadow should be able to use it even when freeps owns the meta-site.
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var hides = scn.GetShadowCard("hides");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(hides);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		scn.ShadowChooseCardBPFromSelection(hides);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void NotAvailableWithoutPossessionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		// No possession in hand, action shouldn't be available
		assertFalse(scn.ShadowActionAvailable(mod));
	}
}
