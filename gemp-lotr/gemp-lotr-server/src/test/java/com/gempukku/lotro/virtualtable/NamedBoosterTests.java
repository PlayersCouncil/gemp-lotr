package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: UseBoosterOf, which has the game's pack opener offer one pack, "Test - Booster", that
 * holds exactly the cards a test names, in the order it names them.  A pack is opened in a game by one card only,
 * Race Text 92_29 ("Shadow: Open and reveal a sealed booster pack (limit once per turn). You may play one shadow
 * card revealed, ignoring all costs."), so that is the card these tables have; nothing here turns on what it does
 * beyond showing the pack and playing a minion from it.  The cards named are plain ones: Goblin Patrol Troop 1_177
 * (a minion with no text), Goblin Runner 1_178, and Bounder 1_286 (a Free Peoples ally, which the card shows and
 * does not let be chosen).
 */
public class NamedBoosterTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a card for the decks, never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_29"
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void TheOnePackOfferedHoldsExactlyTheCardsNamedInTheirOrder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");

		// The same blueprint twice: a pack may hold two copies of a card.
		scn.UseBoosterOf("1_177", "1_286", "1_178", "1_177");
		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);

		// One pack is offered, by the name the fixed test pack has.
		assertEquals(List.of("Test - Booster"), scn.ShadowGetBPChoices());
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		// The other player is shown the four cards named, in the order named.
		assertEquals(List.of("1_177", "1_286", "1_178", "1_177"), scn.FreepsGetBPChoices());
		scn.FreepsDismissRevealedCards();

		// The owner has the same four before him; the game decides which of them may be chosen as for any pack.
		assertEquals(List.of("1_177", "1_286", "1_178", "1_177"), scn.ShadowGetBPChoices());
		assertEquals(3, scn.ShadowGetSelectableCount());
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_177"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_286", false));

		// A card of the pack is played as a card of any pack is.
		scn.StartCardCapture();
		scn.ShadowChooseCardBPFromSelection("1_177");

		assertEquals(1, scn.GetCreatedCards().size());
		assertEquals("1_177", scn.GetLastCreatedCard().getBlueprintId());
		assertInZone(Zone.SHADOW_CHARACTERS, scn.GetLastCreatedCard());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ALaterCallTakesTheEarlierPacksPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseBoosterOf("1_177");
		scn.UseBoosterOf("1_178", "1_286");
		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);
		assertEquals(List.of("Test - Booster"), scn.ShadowGetBPChoices());
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		assertEquals(List.of("1_178", "1_286"), scn.FreepsGetBPChoices());
	}

	@Test
	public void TheFixedTestPackTakesANamedPacksPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseBoosterOf("1_177");
		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		// product/Test-Booster.hjson holds eight cards.
		assertEquals(8, scn.FreepsGetBPChoiceCount());
	}

	@Test
	public void APackOfNoCardsOrOfSomethingThatIsNoCardIsRefusedAndThePackBeforeItStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseBoosterOf("1_178");

		assertRefused(() -> scn.UseBoosterOf(), "UseBoosterOf was given no cards");
		assertRefused(() -> scn.UseBoosterOf("1_177", "99_999"), "'99_999'", "not the blueprint id of any card");
		assertRefused(() -> scn.UseBoosterOf("troop"), "'troop'", "not the blueprint id of any card");

		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		assertEquals(List.of("1_178"), scn.FreepsGetBPChoices());
	}
}
