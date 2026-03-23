package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_92_034_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Aragorn, Ranger of the North (1_89): Gondor Man companion
		put("aragorn", "1_89");
		// Boromir, Lord of Gondor (1_96): Gondor Man companion
		put("boromir", "1_96");
		// Athelas (1_94): non-unique classless Gondor possession, targets Gondor Man
		put("athelas1", "1_94");
		put("athelas2", "1_94");
		// Ranger's Sword (1_112): unique Hand weapon, targets Aragorn
		put("sword", "1_112");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_34", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_34"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_34
		 * Type: MetaSite
		 * Game Text: Each companion may bear no more than one possession.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_34", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void DifferentCompanionsCanEachBearOnePossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(athelas1, athelas2, sword);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		assertTrue(scn.FreepsPlayAvailable(athelas1));
		scn.FreepsPlayCard(athelas1);
		assertAttachedTo(athelas1, boromir);

		//Can no longer play the second Athelas as both Aragorn and Boromir already have
		// the maximum number of attached possessions
		assertFalse(scn.FreepsPlayAvailable(athelas2));
	}

	@Test
	public void AffectsOpponent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas1);
		scn.MoveCardsToHand(athelas2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(athelas2));
	}
}
