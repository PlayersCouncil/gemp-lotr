package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_09_004_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "59_4");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, resistance 6, no text but a requirement to play
					put("accretion", "9_6");    // Ring of Accretion: Dwarf artifact, cost 0, bearer is damage +1; a maneuver ability used only on purpose
					put("guile", "9_8");        // Ring of Guile: Dwarf artifact, bearer is damage +1; a maneuver ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				"59_4",                         // Gimli is the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Gimli, Bearer of Grudges
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Can be the Ring-bearer: True
		 * Game Text: <b>Damage +1.</b>
		 * 	While Gimli is the Ring-bearer, at the start of each skirmish involving him, add 2 burdens or 2 threats.
		 * 	While Gimli is damage +X, he is resistance +X.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Bearer of Grudges", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void GimliIsPlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gimli));

		scn.FreepsPlayCard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AsRingBearerAtTheStartOfHisSkirmishGimliMayAdd2Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("threats"));
		scn.FreepsChooseOption("burdens");

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AsRingBearerAtTheStartOfHisSkirmishGimliMayAdd2Threats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		// Two companions: two threats can be added
		assertEquals(2, scn.GetThreatLimit());
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsChooseOption("threats");

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhenTwoThreatsCannotBeAddedGimliAdds2Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);

		// Two companions and one threat: only one more threat can be added
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BurdensOrThreatsAreAddedAtTheStartOfEachSkirmishInvolvingGimli() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsChooseOption("burdens");
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// Gimli, strength 7 with The Ruling Ring, beats the strength 5 Savage, who survives with 1 vitality left
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsChooseOption("threats");
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NothingIsAddedAtTheStartOfASkirmishNotInvolvingGimli() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var gimli = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NothingIsAddedAtTheStartOfASkirmishWhileGimliIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{frodo, savage2});

		// His own skirmish, but he is not the Ring-bearer
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage));
		assertNotSame(gimli, scn.GetRingBearer());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Ring-bearer's skirmish, which Gimli is not in
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage2));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void GimliIsResistancePlusXWhileDamagePlusX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var accretion = scn.GetFreepsCard("accretion");
		var guard = scn.GetFreepsCard("guard");
		var guile = scn.GetFreepsCard("guile");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(guard, guile);
		scn.MoveCardsToHand(accretion);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// 5 printed + 1 for damage +1
		assertEquals(6, scn.GetResistance(gimli));
		// Dwarf Guard is damage +1 too, and keeps his printed 6
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(guard));

		// Dwarf Guard already bears a ring, so the ring goes to Gimli without a choice
		scn.FreepsPlayCard(accretion);
		assertAttachedTo(accretion, gimli);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		// 5 printed + 2 for damage +2
		assertEquals(7, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
