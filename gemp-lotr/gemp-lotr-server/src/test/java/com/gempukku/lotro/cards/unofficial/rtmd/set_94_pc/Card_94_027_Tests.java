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

public class Card_94_027_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("foes1", "1_105"); // Foes of Mordor: non-unique Free Peoples support-area condition, no play requirement
		put("foes2", "1_105"); // second copy
		put("gimli", "1_13"); // Gimli, Dwarf of Erebor: Dwarf on the table so Dwarf Guard can be played
		put("guard1", "1_7"); // Dwarf Guard: non-unique Free Peoples companion
		put("guard2", "1_7"); // second copy
		put("runner1", "1_178"); // Goblin Runner: non-unique Shadow minion, twilight 1
		put("runner2", "1_178"); // second copy
		put("commander1", "1_186"); // Guard Commander: unique Shadow minion, the card 94_16 loosens
		put("commander2", "1_186"); // second copy, for the 94_16 interaction
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_27", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_27"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// 94_16 ("Minions are not unique", global) against the Shadow player's 94_27
	protected VirtualTableScenario GetMinionsNonUniqueVersusShadowsCopyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_16", "94_27"
		);
	}

	// The same pair with the owners swapped: 94_27 belongs to the Freeps player, so it misses the Shadow player's minions
	protected VirtualTableScenario GetMinionsNonUniqueVersusFreepsCopyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_27", "94_16"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_27
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Each of your cards is unique.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_27", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(4, mod.getBlueprint().getIntensity());
	}

	@Test
	public void OwnerCannotPlayASecondCopyOfACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var foes1 = scn.GetFreepsCard("foes1");
		var foes2 = scn.GetFreepsCard("foes2");

		scn.MoveCardsToSupportArea(foes1);
		scn.MoveCardsToHand(foes2);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(foes2));
	}

	@Test
	public void OwnerCannotPlayASecondCopyOfACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard2));
	}

	@Test
	public void SecondCopiesArePlayableWithoutTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(guard2));
		scn.FreepsPlayCard(guard2);

		assertInZone(Zone.FREE_CHARACTERS, guard2);
	}

	@Test
	public void OwnerCannotPlayASecondCopyOfAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveMinionsToTable(runner1);
		scn.MoveCardsToHand(runner2);
		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(runner2));
	}

	@Test
	public void NonOwnersCardsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// "your" scopes the modifier to the runner; the Shadow player's copy leaves the Freeps player alone
		var scn = GetShadowScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(guard2));
		scn.FreepsPlayCard(guard2);

		assertInZone(Zone.FREE_CHARACTERS, guard2);
	}

	@Test
	public void TheAdventurePathStillAdvances() throws DecisionResultInvalidException, CardNotFoundException {
		// "your" matches the runner's sites too; sites are not played through PlayUtils' uniqueness check
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(4);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
	}

	@Test
	public void OwnersMinionsStayUniqueAgainstMinionsAreNotUnique() throws DecisionResultInvalidException, CardNotFoundException {
		// 94_16 loosens every minion, 94_27 restricts the owner's cards; the restriction wins
		var scn = GetMinionsNonUniqueVersusShadowsCopyScenario();
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
	public void NonOwnersMinionsStayNonUniqueAgainstMinionsAreNotUnique() throws DecisionResultInvalidException, CardNotFoundException {
		// Same pair, owners swapped: 94_27 is the Freeps player's, so only 94_16 reaches the Shadow player's minions
		var scn = GetMinionsNonUniqueVersusFreepsCopyScenario();
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
}
