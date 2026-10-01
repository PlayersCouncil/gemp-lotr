package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_002_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: companion, killed by wounds
		put("frodo", "1_74"); // Frodo: the Ring-bearer, exempted from the mod
		put("gandalf", "1_72"); // Gandalf: Wizard companion, target of Sent Back
		put("sentback", "9_27"); // Sent Back: places a companion in the dead pile without killing it
		put("backstabber", "1_174"); // Goblin Backstabber: minion, assigned against Gandalf
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_2", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_2"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_2
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Each time your character is placed in the dead pile (except the Ring-bearer), discard it.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_2", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void KilledCompanionGoesToDiscardInsteadOfDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		int deadCount = scn.GetFreepsDeadCount();
		int discardCount = scn.GetFreepsDiscardCount();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.DISCARD, aragorn);
		assertEquals(deadCount, scn.GetFreepsDeadCount());
		assertEquals(discardCount + 1, scn.GetFreepsDiscardCount());
	}

	@Test
	public void RingBearerIsExemptAndStillGoesToDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();

		int deadCount = scn.GetFreepsDeadCount();

		scn.AddWoundsToChar(frodo, frodo.getBlueprint().getVitality());
		// A single pump is enough to run the death check; a full SkipToPhase can't complete once
		// the Ring-bearer's death has ended the game.
		scn.FreepsPassCurrentPhaseAction();

		assertInZone(Zone.DEAD, frodo);
		assertEquals(deadCount + 1, scn.GetFreepsDeadCount());
	}

	@Test
	public void SentBackAlsoRedirectsToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gandalf = scn.GetFreepsCard("gandalf");
		var sentback = scn.GetFreepsCard("sentback");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToSupportArea(sentback);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, backstabber);

		int deadCount = scn.GetFreepsDeadCount();
		int discardCount = scn.GetFreepsDiscardCount();

		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);

		// Sent Back places the companion in the dead pile without killing it; the mod redirects
		// that placement to the discard pile too. Sent Back's own cost (discard itself) adds a
		// second card to the discard pile alongside the companion.
		assertInZone(Zone.DISCARD, gandalf);
		assertEquals(deadCount, scn.GetFreepsDeadCount());
		assertEquals(discardCount + 2, scn.GetFreepsDiscardCount());
	}

	@Test
	public void WithoutTheModKilledCompanionGoesToDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.DEAD, aragorn);
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsCompanionStillDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();

		scn.AddWoundsToChar(aragorn, aragorn.getBlueprint().getVitality());
		scn.SkipToPhase(Phase.REGROUP);

		// The mod's filter is "your,character" - Shadow ownership means it doesn't affect
		// Free Peoples companions.
		assertInZone(Zone.DEAD, aragorn);
	}
}
