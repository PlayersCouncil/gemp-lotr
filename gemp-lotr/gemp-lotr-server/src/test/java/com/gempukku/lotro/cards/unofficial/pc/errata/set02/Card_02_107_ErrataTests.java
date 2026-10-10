package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_107_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sunlight", "52_107");
					put("sunlight2", "52_107");

					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a Hobbit ally, cost 2; an ability only used on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Hobbit, strength 8
					put("guard", "1_7");        // Dwarf Guard: a companion with no text that matters, strength 4, vitality 2

					put("nelya", "2_84");       // Úlairë Nelya: [ringwraith] Nazgûl, strength 10; fierce only at 2 burdens or 2 wounds on the Ring-bearer
					put("lemenya", "1_232");    // Úlairë Lemenya: [ringwraith] Nazgûl, strength 9, cost 4, site 3; text only touches bearers of [ringwraith] conditions
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: [raider] Nazgûl, strength 12; fierce
					put("ambusher", "8_74");    // Morgul Ambusher: [ringwraith] Orc, not a Nazgûl, strength 8; text needs 3 threats
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
		 * Game Text: To play, spot 2 Hobbits.
		 * Fellowship: Discard this condition to make each Nazgûl strength -4 until the regroup phase.
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
	public void CannotBePlayedWithOneHobbitAndAManButCanOnceAHobbitAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sunlight, bilbo);
		// Aragorn is of the [shire] culture but no Hobbit: the text names a race (F-0019).
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.SHIRE));

		scn.StartGame();

		// Frodo is one Hobbit; Aragorn is a [shire] companion but no Hobbit.
		assertFalse(scn.FreepsPlayAvailable(sunlight));

		scn.FreepsPlayCard(bilbo);
		assertEquals(2, scn.GetTwilight());

		// Frodo and an ally Hobbit are 2 Hobbits.
		assertTrue(scn.FreepsPlayAvailable(sunlight));
		scn.FreepsPlayCard(sunlight);
		// 2 for Bilbo + 1
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sunlight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsThisCopyToMakeEachNazgulStrengthMinus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var sunlight2 = scn.GetFreepsCard("sunlight2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(sunlight, sunlight2);

		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, attea, ambusher);

		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(8, scn.GetStrength(ambusher));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(sunlight));

		scn.FreepsUseCardAction(sunlight);

		assertInDiscard(sunlight);
		assertInZone(Zone.SUPPORT, sunlight2);
		assertEquals(6, scn.GetStrength(nelya));
		assertEquals(8, scn.GetStrength(attea));
		// Neither a [ringwraith] Orc nor a companion is a Nazgûl.
		assertEquals(8, scn.GetStrength(ambusher));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(sunlight2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		scn.MoveCardsToSupportArea(sunlight);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		assertTrue(scn.FreepsActionAvailable(sunlight));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sunlight));
		assertInZone(Zone.SUPPORT, sunlight);
		assertEquals(10, scn.GetStrength(nelya));
	}

	@Test
	public void NazgulStayMinus4ThroughEverySkirmishAndRecoverAtTheStartOfRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(sunlight);

		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya);

		scn.FreepsUseCardAction(sunlight);
		assertEquals(6, scn.GetStrength(nelya));
		assertEquals(5, scn.GetStrength(lemenya));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, nelya},
				new PhysicalCardImpl[]{guard, lemenya}
		);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(nelya));
		scn.PassCurrentPhaseActions();

		// The second skirmish, after the first has been resolved: still -4.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(5, scn.GetStrength(lemenya));
		assertEquals(6, scn.GetStrength(nelya));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(lemenya));
	}

	@Test
	public void ANazgulPlayedAfterTheActionIsUsedIsWeakenedTooUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(sunlight);

		var lemenya = scn.GetShadowCard("lemenya");
		var ambusher = scn.GetShadowCard("ambusher");
		scn.MoveCardsToHand(lemenya, ambusher);

		scn.StartGame();

		// As in a game: no Nazgûl is on the table in the fellowship phase when the condition is discarded.
		scn.FreepsUseCardAction(sunlight);
		assertInDiscard(sunlight);

		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Not a snapshot (Ketura on F-0217): a Nazgûl played afterwards is strength -4 as he comes into play.
		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(5, scn.GetStrength(lemenya));

		// A [ringwraith] Orc played afterwards is no Nazgûl.
		scn.ShadowPlayCard(ambusher);
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher);
		assertEquals(8, scn.GetStrength(ambusher));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(lemenya));
		scn.PassCurrentPhaseActions();

		// Aragorn (8) beat a Nazgûl of 5 (one wound of his 2 vitality), and the regroup phase ends the -4.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(8, scn.GetStrength(ambusher));
	}
}
