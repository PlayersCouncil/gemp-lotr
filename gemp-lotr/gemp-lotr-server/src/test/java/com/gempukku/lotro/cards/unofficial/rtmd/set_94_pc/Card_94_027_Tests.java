package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_94_027_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");      // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");      // second copy
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Dwarven Armor borne
					put("guard1", "1_7");       // Dwarf Guard: Free Peoples companion, not unique, vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");       // second copy
					put("guard3", "1_7");       // third copy
					put("armor1", "1_8");       // Dwarven Armor: possession, not unique, cost 0, borne by a Dwarf; changes no number
					put("armor2", "1_8");       // second copy
					put("runner1", "1_178");    // Goblin Runner: Shadow minion, not unique, cost 1
					put("runner2", "1_178");    // second copy
					put("commander1", "1_186"); // Guard Commander: unique Shadow minion, which Race Text 94_16 makes not unique
					put("commander2", "1_186"); // second copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_27", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");      // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");      // second copy
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Dwarven Armor borne
					put("guard1", "1_7");       // Dwarf Guard: Free Peoples companion, not unique, vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");       // second copy
					put("guard3", "1_7");       // third copy
					put("armor1", "1_8");       // Dwarven Armor: possession, not unique, cost 0, borne by a Dwarf; changes no number
					put("armor2", "1_8");       // second copy
					put("runner1", "1_178");    // Goblin Runner: Shadow minion, not unique, cost 1
					put("runner2", "1_178");    // second copy
					put("commander1", "1_186"); // Guard Commander: unique Shadow minion, which Race Text 94_16 makes not unique
					put("commander2", "1_186"); // second copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_27"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");      // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");      // second copy
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Dwarven Armor borne
					put("guard1", "1_7");       // Dwarf Guard: Free Peoples companion, not unique, vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");       // second copy
					put("guard3", "1_7");       // third copy
					put("armor1", "1_8");       // Dwarven Armor: possession, not unique, cost 0, borne by a Dwarf; changes no number
					put("armor2", "1_8");       // second copy
					put("runner1", "1_178");    // Goblin Runner: Shadow minion, not unique, cost 1
					put("runner2", "1_178");    // second copy
					put("commander1", "1_186"); // Guard Commander: unique Shadow minion, which Race Text 94_16 makes not unique
					put("commander2", "1_186"); // second copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// 94_16 ("Minions are not unique", global) against the Shadow player's 94_27
	protected VirtualTableScenario GetMinionsNonUniqueVersusShadowsCopyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");      // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");      // second copy
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Dwarven Armor borne
					put("guard1", "1_7");       // Dwarf Guard: Free Peoples companion, not unique, vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");       // second copy
					put("guard3", "1_7");       // third copy
					put("armor1", "1_8");       // Dwarven Armor: possession, not unique, cost 0, borne by a Dwarf; changes no number
					put("armor2", "1_8");       // second copy
					put("runner1", "1_178");    // Goblin Runner: Shadow minion, not unique, cost 1
					put("runner2", "1_178");    // second copy
					put("commander1", "1_186"); // Guard Commander: unique Shadow minion, which Race Text 94_16 makes not unique
					put("commander2", "1_186"); // second copy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_16", "94_27"
		);
	}

	// The same pair with the owners swapped: 94_27 belongs to the Freeps player, so it misses the Shadow player's minions
	protected VirtualTableScenario GetMinionsNonUniqueVersusFreepsCopyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes1", "1_105");      // Foes of Mordor: [gondor] condition, not unique, cost 1, plays to the support area; a maneuver ability used only on purpose
					put("foes2", "1_105");      // second copy
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, so that Dwarf Guard may be played and Dwarven Armor borne
					put("guard1", "1_7");       // Dwarf Guard: Free Peoples companion, not unique, vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");       // second copy
					put("guard3", "1_7");       // third copy
					put("armor1", "1_8");       // Dwarven Armor: possession, not unique, cost 0, borne by a Dwarf; changes no number
					put("armor2", "1_8");       // second copy
					put("runner1", "1_178");    // Goblin Runner: Shadow minion, not unique, cost 1
					put("runner2", "1_178");    // second copy
					put("commander1", "1_186"); // Guard Commander: unique Shadow minion, which Race Text 94_16 makes not unique
					put("commander2", "1_186"); // second copy
				}},
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		assertTrue(scn.AwaitingShadowPhaseActions());
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheAdventurePathStillAdvances() throws DecisionResultInvalidException, CardNotFoundException {
		// The runner's sites are his cards too; a site whose title is not on the path is still laid
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(4);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		assertTrue(scn.AwaitingShadowPhaseActions());
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
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceOneCopyOfABornePossessionIsPlayedTheSecondCannotBe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.MoveCardsToHand(armor1, armor2);
		scn.StartGame();

		// With neither in play, each copy may be played.
		assertTrue(scn.FreepsPlayAvailable(armor1));
		assertTrue(scn.FreepsPlayAvailable(armor2));

		scn.FreepsPlayCard(armor1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard1));
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(armor1, gimli);

		// Dwarf Guard could bear the second copy, but its title is now in play.
		assertFalse(scn.FreepsPlayAvailable(armor2));
		assertInZone(Zone.HAND, armor2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACopyInTheOwnersDeadPileBarsAnotherFromBeingPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToDeadPile(guard1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		// No Dwarf Guard is in play, and Gimli is there to be spotted: the dead one is what bars the play.
		assertInZone(Zone.DEAD, guard1);
		assertFalse(scn.FreepsPlayAvailable(guard2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OwnerMayDiscardACopyFromHandToHealHisWoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");

		scn.MoveCompanionsToTable(gimli, guard1);
		scn.AddWoundsToChar(guard1, 1);
		scn.MoveCardsToHand(guard2);
		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsPlayAvailable(guard2));
		assertTrue(scn.FreepsHasDiscardToHealAvailable());

		scn.FreepsDiscardToHeal(guard2);
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, guard2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModifierACopyInTheDeadPileBarsNothingAndNoCopyIsDiscardedToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");

		scn.MoveCompanionsToTable(gimli, guard2);
		scn.MoveCardsToDeadPile(guard1);
		scn.AddWoundsToChar(guard2, 1);
		scn.MoveCardsToHand(guard3);
		scn.StartGame();

		assertInZone(Zone.DEAD, guard1);
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertFalse(scn.FreepsHasDiscardToHealAvailable());
		assertTrue(scn.FreepsPlayAvailable(guard3));

		scn.FreepsPlayCard(guard3);
		assertInZone(Zone.FREE_CHARACTERS, guard3);
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
