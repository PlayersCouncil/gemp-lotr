package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("war", "4_135");
					put("war2", "4_135");
					put("war3", "4_135");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; Knight is his whole text
					// Gondorian Merchant: the [gondor] Man ally, strength 6; his maneuver ability needs initiative
					// and is never used here.
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] Hobbit, strength 3; Enduring (no wounds on him here) and a
					// skirmish ability used only on purpose. To play, spot a [gondor] companion: cheated in.
					put("pippin", "103_31");
					put("rohirrim", "5_83");    // Household Guard: a [rohan] Man companion, strength 6; Valiant is his whole text

					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10, site 2, so not roaming at site 2; fierce and damage +1 only at 2 burdens
					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3, site 5, so roaming at site 2; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarandValorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: War and Valor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] Man strength +2 (or +3 and <b>damage +1</b> if skirmishing a roaming minion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("war");

		assertEquals("War and Valor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenGondorManWhoIsNotSkirmishingARoamingMinionStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var war2 = scn.GetFreepsCard("war2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		scn.MoveCompanionsToTable(aragorn, knight, pippin, rohirrim);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(war, war2);
		assertInHand(war, war2);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(war));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(rohirrim));
		assertTrue(scn.FreepsPlayAvailable(war));

		scn.FreepsPlayCard(war);
		// [gondor] Men: two companions, one in the skirmish, and the ally. Not the [gondor] Hobbit, the [rohan]
		// Man, Frodo or the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rohirrim, frodo, nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// Úlairë Nelya is not roaming at site 2: +2, and no damage bonus.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(merchant));
		assertInDiscard(war);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(war2);
		scn.FreepsChooseCard(merchant);
		// The ally, in no skirmish: +2.
		assertEquals(8, scn.GetStrength(merchant));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Once the skirmish is over the bonuses are gone (an event's effect lasts for its skirmish phase).
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(merchant));
	}

	@Test
	public void IsPlayedForNothingWithNoGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var rohirrim = scn.GetFreepsCard("rohirrim");
		scn.MoveCompanionsToTable(pippin, rohirrim);
		scn.MoveCardsToHand(war);
		assertInHand(war);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rohirrim, savage);
		scn.FreepsResolveSkirmish(rohirrim);

		// A [gondor] Hobbit and a [rohan] Man skirmishing a roaming minion: nobody for the event to strengthen.
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		var frodoStrength = scn.GetStrength(frodo);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(rohirrim));
		assertEquals(0, scn.GetKeywordCount(rohirrim, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(war));

		scn.FreepsPlayCard(war);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(rohirrim));
		assertEquals(0, scn.GetKeywordCount(rohirrim, Keyword.DAMAGE));
		assertInDiscard(war);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MakesAGondorManSkirmishingARoamingMinionStrengthPlus3AndDamagePlus1ButNotBeforeHisSkirmishNorAnotherMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(war, war2, war3);
		assertInHand(war, war2, war3);

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
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		scn.FreepsPlayCard(war);
		scn.FreepsChooseCard(aragorn);
		// Assigned to a roaming minion, but not yet skirmishing it: +2, and no damage bonus.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(war2);
		scn.FreepsChooseCard(aragorn);
		// Skirmishing the roaming Savage: +3 and damage +1.
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		assertFalse(scn.IsCharAssigned(knight));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		scn.FreepsPlayCard(war3);
		scn.FreepsChooseCard(knight);
		// Another [gondor] Man, his own skirmish over, chosen while Aragorn skirmishes the roaming Savage: +2 only.
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertInDiscard(war, war2, war3);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The skirmish is over: the strength and the damage bonus are gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(knight));
	}
}
