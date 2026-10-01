package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_93_001_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("backstabber", "1_174"); // Goblin Backstabber: non-unique Moria Orc minion
		put("runner", "1_178"); // Goblin Runner: non-unique Moria Orc minion
		put("scimitar", "1_180"); // Goblin Scimitar: Hand weapon, bears on Moria Orc
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: Gondor companion
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_1"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_1", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_1
		 * Type: MetaSite
		 * Game Text: Skirmish: Play a possession on your minion.
		 */
		var scn = GetShadowScenario();
		var card = scn.GetShadowCard("mod");
		assertEquals("Race Text 93_1", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlayPossessionOnMinionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var scimitar = scn.GetShadowCard("scimitar");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		scn.FreepsPass();

		// Skirmish phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Play the scimitar on the backstabber
		assertAttachedTo(scimitar, backstabber);
	}

	@Test
	public void OwnerGatingFreepsCantUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var backstabber = scn.GetShadowCard("backstabber");
		var scimitar = scn.GetShadowCard("scimitar");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		scn.FreepsPass();

		// Freeps owns mod, so Shadow can't use it
		assertFalse(scn.ShadowActionAvailable("mod"));
	}
}
