package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battlecry", "11_56");
					put("battlecry2", "11_56");
					put("battlecry3", "11_56");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, strength 8; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, strength 6; +3 only while skirmishing a Nazgul
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger, strength 6; Knight is his whole text

					put("nelya", "2_84");       // Ulaire Nelya: [wraith] Nazgul, strength 10; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 is its whole text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, strength 15; damage +1 and fierce; its other text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BattleCryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Battle Cry
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a ranger strength +2 (or +3 if he or she is skirmishing a minion who has a damage bonus).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battlecry");

		assertEquals("Battle Cry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenRangerWhoIsNotSkirmishingAMinionWithADamageBonusStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlecry = scn.GetFreepsCard("battlecry");
		var battlecry2 = scn.GetFreepsCard("battlecry2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(battlecry, battlecry2);
		assertInHand(battlecry, battlecry2);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(battlecry));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertFalse(scn.IsCharAssigned(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.FreepsPlayAvailable(battlecry));

		scn.FreepsPlayCard(battlecry);
		// The rangers, a [gondor] Man in the skirmish and an [elven] Elf in none; not the Knight, who is no ranger,
		// nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo, nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// Úlairë Nelya has no damage bonus with no burden and no wound on the Ring-bearer: +2.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(battlecry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(battlecry2);
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

		var battlecry = scn.GetFreepsCard("battlecry");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(battlecry);
		assertInHand(battlecry);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		// The Ring-bearer and a Man who is no ranger are the only companions: nobody for the event to strengthen.
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		var frodoStrength = scn.GetStrength(frodo);
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(battlecry));

		scn.FreepsPlayCard(battlecry);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(battlecry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MakesARangerSkirmishingAMinionWithADamageBonusStrengthPlus3ButNotBeforeHisSkirmishNorAnotherRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlecry = scn.GetFreepsCard("battlecry");
		var battlecry2 = scn.GetFreepsCard("battlecry2");
		var battlecry3 = scn.GetFreepsCard("battlecry3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToHand(battlecry, battlecry2, battlecry3);
		assertInHand(battlecry, battlecry2, battlecry3);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, nelya},
				new PhysicalCardImpl[]{aragorn, savage});
		// The Knight's skirmish first, with Aragorn assigned to the Savage and waiting.
		scn.FreepsResolveSkirmish(knight);

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(battlecry);
		scn.FreepsChooseCard(aragorn);
		// Assigned to a minion with a damage bonus, but not yet skirmishing it: +2.
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(battlecry2);
		scn.FreepsChooseCard(aragorn);
		// Skirmishing the Savage, damage +1: +3.
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		assertFalse(scn.IsCharAssigned(arwen));
		assertEquals(6, scn.GetStrength(arwen));
		scn.FreepsPlayCard(battlecry3);
		scn.FreepsChooseCard(arwen);
		// Another ranger, in no skirmish, chosen while Aragorn skirmishes the Savage: +2.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(11, scn.GetStrength(aragorn));
		assertInDiscard(battlecry, battlecry2, battlecry3);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OneMinionWithADamageBonusInTheSkirmishIsEnoughForPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlecry = scn.GetFreepsCard("battlecry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(battlecry);
		assertInHand(battlecry);

		var nelya = scn.GetShadowCard("nelya");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, troll);
		scn.SkipToAssignments();
		// Nelya from the Free Peoples player, the Troll put on Aragorn by the Shadow player.
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowAssignToMinions(aragorn, troll);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, nelya));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troll));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(battlecry);
		// The [moria] Troll, damage +1, is one of the minions he is skirmishing: +3.
		assertEquals(11, scn.GetStrength(aragorn));
		assertInDiscard(battlecry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
