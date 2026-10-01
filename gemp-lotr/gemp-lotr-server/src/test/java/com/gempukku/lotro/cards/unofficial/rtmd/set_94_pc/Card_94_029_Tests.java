package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_029_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("backstabber", "1_174"); // Goblin Backstabber: clean, non-fierce Moria minion
		put("gateTroll", "6_103"); // Gate Troll: already prints Fierce, to check whether the keyword stacks
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_29", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_29"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_29
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: Your opponent's minions are fierce.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_29", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(5, mod.getBlueprint().getIntensity());
	}

	// Because modifiers run as the card's owner, a Freeps-owned copy makes the Shadow player's
	// minions fierce, and a Shadow-owned copy has no effect on that turn.

	@Test
	public void OpponentsMinionsBecomeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();

		assertTrue(scn.HasKeyword(backstabber, Keyword.FIERCE));
	}

	@Test
	public void RunnersOwnMinionsAreNotAffectedWhenShadowOwnsMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();

		// not(your) never matches the runner's own minion
		assertFalse(scn.HasKeyword(backstabber, Keyword.FIERCE));
	}

	@Test
	public void AlreadyFierceMinionKeywordCountIsRecorded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gateTroll = scn.GetShadowCard("gateTroll");

		scn.MoveMinionsToTable(gateTroll);
		scn.StartGame();

		assertTrue(scn.HasKeyword(gateTroll, Keyword.FIERCE));
		// Pinned: the mod's Fierce stacks with the printed one rather than being deduplicated.
		assertEquals(2, scn.GetKeywordCount(gateTroll, Keyword.FIERCE));
	}
}
