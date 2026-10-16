package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swordsman", "1_117");
					put("swordsman2", "1_117");
					put("swordsman3", "1_117");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, strength 8; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, strength 6; +3 only while skirmishing a Nazgul
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger, strength 6; Knight is his whole text

					put("nelya", "2_84");       // Ulaire Nelya: Nazgul, strength 10, site 2, so not roaming at site 2; fierce and damage +1 only at 2 burdens
					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, site 5, so roaming at site 2; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordsmanoftheNorthernKingdomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Swordsman of the Northern Kingdom
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a ranger strength +2 (or +4 when skirmishing a roaming minion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordsman");

		assertEquals("Swordsman of the Northern Kingdom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenRangerWhoIsNotSkirmishingARoamingMinionStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordsman = scn.GetFreepsCard("swordsman");
		var swordsman2 = scn.GetFreepsCard("swordsman2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(swordsman, swordsman2);
		assertInHand(swordsman, swordsman2);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordsman));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertFalse(scn.IsCharAssigned(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.FreepsPlayAvailable(swordsman));

		scn.FreepsPlayCard(swordsman);
		// The rangers, a [gondor] Man in the skirmish and an [elven] Elf in none; not the Knight, who is no ranger,
		// nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo, nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// Úlairë Nelya is not roaming at site 2: +2.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(swordsman);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(swordsman2);
		scn.FreepsChooseCard(arwen);
		// Arwen, skirmishing nothing: +2.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Once the skirmish is over the bonuses are gone (CR 4.2: an event's phase effect lasts for its skirmish).
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
	}

	@Test
	public void IsPlayedForNothingWithNoRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordsman = scn.GetFreepsCard("swordsman");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(swordsman);
		assertInHand(swordsman);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		// The Ring-bearer and a Man who is no ranger are the only companions: nobody for the event to strengthen.
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		var frodoStrength = scn.GetStrength(frodo);
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordsman));

		scn.FreepsPlayCard(swordsman);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(swordsman);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MakesARangerSkirmishingARoamingMinionStrengthPlus4ButNotBeforeHisSkirmishNorAnotherRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordsman = scn.GetFreepsCard("swordsman");
		var swordsman2 = scn.GetFreepsCard("swordsman2");
		var swordsman3 = scn.GetFreepsCard("swordsman3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(swordsman, swordsman2, swordsman3);
		assertInHand(swordsman, swordsman2, swordsman3);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, nelya},
				new PhysicalCardImpl[]{aragorn, savage});
		// The Knight's skirmish first, with Aragorn assigned to the roaming Savage and waiting.
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(swordsman);
		scn.FreepsChooseCard(aragorn);
		// Assigned to a roaming minion, but not yet skirmishing it: +2.
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(swordsman2);
		scn.FreepsChooseCard(aragorn);
		// Skirmishing the roaming Savage: +4.
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		assertFalse(scn.IsCharAssigned(arwen));
		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(swordsman3);
		scn.FreepsChooseCard(arwen);
		// Another ranger, in no skirmish, chosen while Aragorn skirmishes the roaming Savage: +2.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(12, scn.GetStrength(aragorn));
		assertInDiscard(swordsman, swordsman2, swordsman3);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
