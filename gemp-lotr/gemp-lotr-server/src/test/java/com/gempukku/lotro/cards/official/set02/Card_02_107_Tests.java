package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sunlight", "2_107");

					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a [shire] Hobbit ally, cost 2; a fellowship ability never used
					// Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion (cheated in);
					// a skirmish ability never used.
					put("trollslayer", "103_31");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8
					// Smeagol, Once Like Us: a [shire] companion with no race; his assignment ability is never reached.
					put("smeagol", "103_116");
					put("guard", "1_7");        // Dwarf Guard: a companion of strength 4, vitality 2; "To play, spot a Dwarf."
					put("stairs", "1_24");      // Stairs of Khazad-dum: another Free Peoples support condition; its bonus is for a Dwarf against a [moria] minion

					put("nelya", "2_84");       // Úlairë Nelya: [wraith] Nazgûl, strength 10; fierce only at 2 burdens or 2 wounds on the Ring-bearer
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgûl, strength 12; fierce; his texts need 3 or 5 burdens
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9, cost 4; text only touches bearers of [wraith] conditions
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] Orc and no Nazgûl, strength 8; its text needs 3 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotFearedinSunlightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Not Feared in Sunlight
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Hobbits. Plays to your support area.
		 * 	Each Nazgûl is strength -4. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sunlight");

		assertEquals("Not Feared in Sunlight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOneHobbitAndTwoCompanionsWhoAreNoHobbitsButCanOnceAHobbitAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		// Smeagol is a [shire] companion as printed and no Hobbit: the text names a race (F-0019).
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(sunlight, bilbo);

		scn.StartGame();

		// Frodo is one Hobbit; Aragorn is a Man, and Smeagol a [shire] companion but no Hobbit.
		assertFalse(scn.FreepsPlayAvailable(sunlight));

		scn.FreepsPlayCard(bilbo);
		assertInZone(Zone.SUPPORT, bilbo);

		// Frodo and a Hobbit who is an ally are 2 Hobbits.
		assertTrue(scn.FreepsPlayAvailable(sunlight));
		scn.FreepsPlayCard(sunlight);
		assertInZone(Zone.SUPPORT, sunlight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedBesideAHobbitOfAnotherCultureEachNazgulIsStrengthMinus4AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(trollslayer, aragorn);
		scn.MoveCardsToHand(sunlight);

		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var creeper = scn.GetShadowCard("creeper");

		scn.StartGame();
		// Laid early, to be read before and after the play (Q-0228).
		scn.MoveMinionsToTable(nelya, attea, creeper);
		scn.FreepsRefreshDecision();

		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(8, scn.GetStrength(aragorn));

		// Frodo and Pippin, a [gondor] Hobbit, are the 2 Hobbits.
		assertTrue(scn.FreepsPlayAvailable(sunlight));
		scn.FreepsPlayCard(sunlight);
		assertInZone(Zone.SUPPORT, sunlight);

		// A [wraith] Nazgûl and a [raider] Nazgûl are each -4.
		assertEquals(6, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(attea));
		// Neither a [wraith] Orc nor a companion is a Nazgûl.
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ANazgulPlayedLaterIsWeakenedThroughTheSkirmishesAndTheConditionIsDiscardedAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var stairs = scn.GetFreepsCard("stairs");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(sunlight, stairs);

		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, sunlight, stairs);
		assertEquals(6, scn.GetStrength(nelya));

		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A standing text: a Nazgûl played afterwards is strength -4 as he comes into play.
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(5, scn.GetStrength(lemenya));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, nelya},
				new PhysicalCardImpl[]{guard, lemenya}
		);

		// Aragorn (8) beats Nelya (6), and is not twice her strength.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(nelya));
		scn.PassCurrentPhaseActions();

		// The second skirmish, the phase before regroup, after one already resolved: still in play, still -4.
		scn.FreepsResolveSkirmish(guard);
		assertInZone(Zone.SUPPORT, sunlight);
		assertEquals(5, scn.GetStrength(lemenya));
		assertEquals(6, scn.GetStrength(nelya));
		scn.PassCurrentPhaseActions();

		// At the first regroup decision the condition is gone, and with it the -4; the other support condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(sunlight);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(lemenya));
	}
}
