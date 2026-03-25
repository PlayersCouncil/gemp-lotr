package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class Card_92_026_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Hobbit Sword (1_299): Shire possession, bearer must be a hobbit
		put("sword", "1_299");
		// Thrór's Map (1_318): Shire support area possession
		put("map", "1_318");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_26", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_26"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_26
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, add (1) for each Free Peoples possession.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_26", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void AddsTwilightForEachPossessionOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var sword = scn.GetFreepsCard("sword");
		var map = scn.GetFreepsCard("map");

		scn.AttachCardsTo(scn.GetRingBearer(), sword);
		scn.MoveCardsToSupportArea(map);

		scn.StartGame();

		// Move to site 2
		scn.FreepsPassCurrentPhaseAction();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// Site shadow + 1 companion (Frodo) + 2 possessions
		assertEquals(site2Shadow + 1 + 2, twilightAfterMove);
	}

	@Test
	public void NoPossessionsAddsNoExtraTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// Site shadow + 1 companion (Frodo), no possessions
		assertEquals(site2Shadow + 1, twilightAfterMove);
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var sword = scn.GetFreepsCard("sword");
		var map = scn.GetFreepsCard("map");

		scn.AttachCardsTo(scn.GetRingBearer(), sword);
		scn.MoveCardsToSupportArea(map);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		int twilightAfterMove = scn.GetTwilight();
		int site2Shadow = scn.GetCurrentSite().getBlueprint().getTwilightCost();

		// Shadow owns the modifier, so no extra twilight from possessions
		assertEquals(site2Shadow + 1, twilightAfterMove);
	}
}
