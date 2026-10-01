package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_94_016_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("commander1", "1_186"); // Guard Commander: unique Moria minion, twilight 3
		put("commander2", "1_186"); // Guard Commander: second copy, to test uniqueness override
		put("aragorn1", "1_89");    // Aragorn, Ranger of the North: unique FP companion
		put("aragorn2", "1_89");    // second copy, proves the filter is minion-only
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_16"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_16", null
		);
	}

	// No meta-site at all - the true "without the card" control, since the filter is global
	// (no "your"), so even a Freeps-owned copy would still loosen every minion's uniqueness.
	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
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
		 * Name: Race Text 94_16
		 * Type: MetaSite
		 * Game Text: Minions are not unique.
		 */

		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		assertEquals("Race Text 94_16", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(2, mod.getBlueprint().getIntensity());
	}

	@Test
	public void CanPlayDuplicateMinionWhenModifierActive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");

		scn.MoveMinionsToTable(commander1);
		scn.MoveCardsToHand(commander2);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(commander2));
		scn.ShadowPlayCard(commander2);

		assertInZone(Zone.SHADOW_CHARACTERS, commander1);
		assertInZone(Zone.SHADOW_CHARACTERS, commander2);
	}

	@Test
	public void CannotPlayDuplicateMinionWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");

		scn.MoveMinionsToTable(commander1);
		scn.MoveCardsToHand(commander2);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(commander2));
	}

	@Test
	public void UniqueCompanionsAreStillBlockedFilterIsMinionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn1 = scn.GetFreepsCard("aragorn1");
		var aragorn2 = scn.GetFreepsCard("aragorn2");

		scn.MoveCompanionsToTable(aragorn1);
		scn.MoveCardsToHand(aragorn2);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(aragorn2));
	}

	@Test
	public void FreepsOwnedCopyStillLoosensUniquenessForShadowsMinionsSinceThereIsNoYour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");

		scn.MoveMinionsToTable(commander1);
		scn.MoveCardsToHand(commander2);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// No "your" in the text - a Freeps-owned copy still loosens uniqueness for the Shadow
		// player's minions.
		assertTrue(scn.ShadowPlayAvailable(commander2));
		scn.ShadowPlayCard(commander2);

		assertInZone(Zone.SHADOW_CHARACTERS, commander1);
		assertInZone(Zone.SHADOW_CHARACTERS, commander2);
	}
}
