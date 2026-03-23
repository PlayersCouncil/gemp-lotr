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

public class Card_92_029_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Ulaire Enquea: unique Nazgul minion, twilight 6 — needed to reach shadow phase
		put("enquea", "1_231");
		// Ulaire Cantea: unique Nazgul minion, twilight 5 — in test booster, for uniqueness test
		put("cantea", "1_230");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_29"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_29", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_29
		 * Type: MetaSite
		 * Game Text: Shadow: Open and reveal a sealed booster pack (limit once per turn).
		 *   You may play one shadow card revealed, ignoring all costs.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_29", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void BasicFlowPlaysAShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction(); // fellowship phase pass → auto-move

		// Now in shadow phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);

		// Step 1: Choose a booster pack
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		// Step 2: Opponent dismisses the revealed cards
		scn.FreepsDismissRevealedCards();

		// Step 3: Shadow player picks a shadow card to play — pick Goblin Backstabber (1_174)
		scn.ChooseCardBPFromSelection(scn.P2, "1_174");

		// Goblin Backstabber should be in play (shadow characters zone)
		var backstabber = scn.GetLastCreatedCard();
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber);
		scn.StopCardCapture();

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipBoosterOpenTest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction(); // fellowship phase pass → auto-move

		// Now in shadow phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Step 1: Choose a booster pack
		scn.ShadowChooseCardBPFromSelection("FotR - Booster");

		// Step 2: Opponent dismisses the revealed cards
		scn.FreepsDismissRevealedCards();

		assertEquals(11, scn.ShadowGetBPChoiceCount());
	}

	@Test
	public void ReflectionsBoosterOpenTest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction(); // fellowship phase pass → auto-move

		// Now in shadow phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Step 1: Choose a booster pack
		scn.ShadowChooseCardBPFromSelection("REF - Booster");

		// Step 2: Opponent dismisses the revealed cards
		scn.FreepsDismissRevealedCards();

		assertEquals(18, scn.ShadowGetBPChoiceCount());
	}

	@Test
	public void LimitOncePerTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		// First use
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		// Decline to play any card
		scn.ShadowPassCurrentPhaseAction();

		// Second use in same turn — should NOT be available
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OwnerGatingFreepsCantUse() throws DecisionResultInvalidException, CardNotFoundException {
		// When Freeps owns the mod, Shadow player cannot use it
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		// Shadow phase — the mod is owned by Freeps, so Shadow can't use it
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OptionalPlayCanDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Decline to play (empty response = select 0 cards)
		scn.ShadowPassCurrentPhaseAction();

		// Should return to normal shadow phase actions
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IgnoresToPlayRequirements() throws DecisionResultInvalidException, CardNotFoundException {
		// Cave Troll of Moria (1_165) requires spotting a Moria Orc to play.
		// With no Moria Orcs on the table, it should still be playable via booster ignoring all costs.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Cave Troll requires a Moria Orc — none on the table, but should still be selectable
		scn.ChooseCardBPFromSelection(scn.P2, "1_165");

		var caveTroll = scn.GetLastCreatedCard();
		assertNotNull(caveTroll);
		assertEquals("Cave Troll of Moria", caveTroll.getBlueprint().getTitle());
		assertInZone(Zone.SHADOW_CHARACTERS, caveTroll);
		scn.StopCardCapture();
	}

	@Test
	public void IgnoresExtraCosts() throws DecisionResultInvalidException, CardNotFoundException {
		// Alive and Unspoiled (1_120) has ExtraCost: exert an Uruk-hai.
		// With no Uruk-hai on the table, it should still be playable via booster ignoring all costs.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Alive and Unspoiled requires exerting an Uruk-hai — none present, but should still work
		scn.ChooseCardBPFromSelection(scn.P2, "1_120");

		var aliveAndUnspoiled = scn.GetLastCreatedCard();
		assertNotNull(aliveAndUnspoiled);
		assertEquals("Alive and Unspoiled", aliveAndUnspoiled.getBlueprint().getTitle());
		assertInZone(Zone.SUPPORT, aliveAndUnspoiled);
		scn.StopCardCapture();
	}

	@Test
	public void IgnoresTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		// Cave Troll of Moria (1_165) costs 10 twilight. With 0 in the pool,
		// it should still be playable ignoring all costs.
		// (This test also validates ignoring ToPlay, but the focus is twilight.)
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		// Verify twilight pool is low (only Frodo moved = 2 twilight from site)
		assertTrue(scn.GetTwilight() < 10);

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		scn.ChooseCardBPFromSelection(scn.P2, "1_165");

		var caveTroll = scn.GetLastCreatedCard();
		assertNotNull(caveTroll);
		assertInZone(Zone.SHADOW_CHARACTERS, caveTroll);
		scn.StopCardCapture();
	}

	@Test
	public void RespectsUniqueness() throws DecisionResultInvalidException, CardNotFoundException {
		// Ulaire Cantea (1_230) is unique and already on the table.
		// The booster also contains 1_230, but it should NOT be selectable.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var cantea = scn.GetShadowCard("cantea");

		scn.MoveMinionsToTable(cantea);

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Cantea (1_230) should be visible but NOT selectable since it's already in play.
		// The selectable shadow cards should be: 1_174 (Backstabber), 1_165 (Cave Troll), 1_120 (Alive and Unspoiled)
		// but NOT 1_230 (Cantea, unique duplicate) and NOT 1_286 (Bounder, FP card).
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_230"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_174"));
	}

	@Test
	public void FPCardsNotSelectable() throws DecisionResultInvalidException, CardNotFoundException {
		// Bounder (1_286) is a Free Peoples card in the test booster.
		// It should be visible but NOT selectable since only shadow cards can be played.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Bounder (1_286) is FP — should not be selectable
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_286"));
		// But shadow cards should be
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_174"));
	}
}
