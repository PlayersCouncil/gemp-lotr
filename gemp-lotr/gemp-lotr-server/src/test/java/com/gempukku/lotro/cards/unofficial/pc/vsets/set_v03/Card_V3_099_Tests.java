package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prisoners", "103_99");   // Release the Prisoners!
					put("slayer", "3_93");        // Morgul Slayer - [Sauron] Orc
					put("runner", "1_178");       // Goblin Runner - [Moria], not [Sauron]

					put("sam", "1_311");          // Sam - [Shire] companion
					put("merry", "1_302");        // Merry - [Shire] companion (the prisoner)
					put("bilbo", "1_284");        // Bilbo - [Shire] ally
					put("aragorn", "1_89");       // Aragorn - [Gondor] companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReleasethePrisonersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Release the Prisoners!
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Maneuver: Exert a [sauron] minion and remove a companion in the dead pile from the game to exert every character sharing a culture with that companion.  Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("prisoners");

		assertEquals("Release the Prisoners!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ReleasethePrisonersRequiresSauronMinionToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners");
		var runner = scn.GetShadowCard("runner");  // [Moria], not [Sauron]
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToSupportArea(prisoners);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToDeadPile(merry);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// No [Sauron] minion to exert
		assertFalse(scn.ShadowActionAvailable(prisoners));
	}

	@Test
	public void ReleasethePrisonersRequiresCompanionInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners");
		var slayer = scn.GetShadowCard("slayer");
		// No companions in dead pile

		scn.MoveCardsToSupportArea(prisoners);
		scn.MoveMinionsToTable(slayer);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(prisoners));
	}

	@Test
	public void ReleasethePrisonersExertsAllCharactersOfMatchingCultureAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners");
		var slayer = scn.GetShadowCard("slayer");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(prisoners, bilbo);  // Bilbo is an ally in support area
		scn.MoveMinionsToTable(slayer);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToDeadPile(merry);  // Merry is [Shire], our prisoner

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(slayer));

		assertTrue(scn.ShadowActionAvailable(prisoners));
		scn.ShadowUseCardAction(prisoners);
		// Slayer auto-selected as only [Sauron] minion
		// Merry auto-selected as only companion in dead pile

		// All [Shire] characters exerted
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(bilbo));

		// [Gondor] Aragorn NOT exerted
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// [Sauron] minion exerted as cost
		assertEquals(1, scn.GetWoundsOn(slayer));

		// Merry removed from game
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInZone(Zone.REMOVED, merry);

		// Condition discarded
		assertInDiscard(prisoners);
	}
}
