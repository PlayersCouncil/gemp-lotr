package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

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

public class Card_92_018_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion of no race: of the Hobbits' culture, and no Hobbit
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion: a Hobbit of another culture
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally: no Hobbit, and no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_18", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion of no race: of the Hobbits' culture, and no Hobbit
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion: a Hobbit of another culture
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally: no Hobbit, and no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_18"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_18
		 * Type: MetaSite
		 * Intensity: 6
		 * Game Text: Each time your fellowship moves, discard 1 non-Hobbit companion (or 2 if at site 7 or higher).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_18", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(6, card.getBlueprint().getIntensity());
	}

	@Test
	public void DiscardsOneNonHobbitOnFellowshipMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(aragorn, merry);

		scn.StartGame();
		scn.FreepsPass();

		// Moving from site 1 to site 2 triggers the effect — discard 1 non-hobbit
		// Merry is a hobbit so not a valid target; Aragorn auto-selected
		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsOnRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(aragorn, gimli, merry);

		scn.StartGame();
		scn.FreepsPass();

		//Freeps has to choose who to discard
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));

		scn.FreepsChooseCard(aragorn);

		// First move (site 1→2): discard 1, the one chosen
		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli);

		//Now the regroup move
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// Second move (site 2→3): discard 1, gimli auto-selected
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsOneAtSite6() throws DecisionResultInvalidException, CardNotFoundException {
		// Moving from site 6 (at site 6, below 7 threshold), should discard 1
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// No companions to discard on the initial move
		scn.SkipToSite(6);

		// Now cheat aragorn in and move from site 6→7
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsPass();
		// Aragorn is the only one who can go, so this reads a discard on a later move and not how many:
		// the two tests below read the number.
		assertInZone(Zone.DISCARD, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromSite5DiscardsOneOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		// Frodo alone on the way: nobody to discard.
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.FreepsPass();

		// From site 5 to site 6: below site 7 before the move and after it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromSite6DiscardsOneOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		// Frodo alone on the way: nobody to discard.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.FreepsPass();

		// "When the fellowship moves" is carried out before the marker is moved: the fellowship is at
		// site 6, one short of site 7, so one of the two goes.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsTwoAtSite7OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// At site 7, moving should discard 2 non-hobbit companions
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		// No companions to discard on moves
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());

		// Now cheat in 2 non-hobbit companions and move from site 7→8
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.FreepsPass();

		// Move triggers, site 7 >= 7 so discard 2

		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.DISCARD, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsTwoOfThreeWhenMovingFromSite8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		// Frodo alone on the way: nobody to discard.
		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());

		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.FreepsPass();

		// Site 8 is higher than 7: two of the three, no more and no fewer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, legolas));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, legolas);

		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.DISCARD, legolas);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShireCompanionWhoIsNoHobbitMayBeChosenAndAGondorHobbitOrAnAllyMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var grimir = scn.GetFreepsCard("grimir");

		scn.MoveCompanionsToTable(aragorn, smeagol, pippin);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.FreepsPass();

		// A Man and a [shire] companion who is no Hobbit; not the Hobbit of [gondor], the Hobbit who bears
		// the Ring, or the Dwarf who is an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, grimir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(smeagol);

		assertInZone(Zone.DISCARD, smeagol);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInZone(Zone.SUPPORT, grimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DoesNotTriggerWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		// No discard triggered — shadow owns the modifier
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
