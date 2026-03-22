package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_021_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Gandalf, Friend of the Shirefolk (1_72): Gandalf companion, twilight 4
		put("gandalf", "1_72");
		// Aragorn, Elessar Telcontar (10_25): Gondor companion, twilight 5
		put("aragorn", "10_25");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_21", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_21"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_21
		 * Type: MetaSite
		 * Game Text: Fellowship: Play a companion; their twilight cost is -4.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_21", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlayCompanionWithDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		// Gandalf costs 4, discount of 4 means he should cost 0.
		// Aragorn costs 5, discount of 4 means he should cost 1.
		// With 1 twilight in pool, Gandalf should be playable and Aragorn should be playable.
		// With 0 twilight, only Gandalf (cost 0 after discount) should be playable via the ability.

		var scn = GetFreepsScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(gandalf, aragorn);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Both should be offered
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(mod);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void NotOwnerGated() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow owns the modifier, but Freeps should still be able to use it
		var scn = GetShadowScenario();

		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
	}
}
