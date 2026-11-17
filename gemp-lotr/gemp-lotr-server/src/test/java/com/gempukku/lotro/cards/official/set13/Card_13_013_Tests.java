package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cavalry", "13_13");
					put("cavalry2", "13_13");
					put("ferevellon", "4_68");  // Ferevellon, Son of Thandronen: [elven] Elf companion; "Skirmish: Exert Ferevellon to make a minion skirmishing him strength -2"
					put("arwen", "0_63");       // Arwen, Maiden of Rivendell: [elven] Elf companion, ranger; "Skirmish: Exert Arwen to make a minion skirmishing an Elf strength -2"
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("shrouded", "13_23");   // Shrouded Elf: [elven] Elf companion; a Response ability to an event played in his skirmish
					put("defiance", "1_37");    // Defiance: [elven] Skirmish event, "Make an Elf strength +2"

					put("slayer", "1_153");     // Uruk Slayer: [isengard] Uruk-hai; "Skirmish: Remove (1) to make this minion strength +1 (limit +3)"
					put("slayer2", "1_153");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrashingCavalryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Crashing Cavalry
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Assignment:</b> Discard this from play and assign a minion to an [elven] companion. Those characters cannot use skirmish special abilities until the start of the regroup phase.
		*/

		var scn = GetScenario();

		var cavalry = scn.GetFreepsCard("cavalry");

		assertEquals("Crashing Cavalry", cavalry.getBlueprint().getTitle());
		assertNull(cavalry.getBlueprint().getSubtitle());
		assertFalse(cavalry.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, cavalry.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, cavalry.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, cavalry.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(cavalry, Keyword.SUPPORT_AREA));
		assertEquals(1, cavalry.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsItselfToAssignAMinionToAnElvenCompanionAndNeitherCanUseSkirmishAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetFreepsCard("cavalry");
		var cavalry2 = scn.GetFreepsCard("cavalry2");
		var frodo = scn.GetRingBearer();
		var ferevellon = scn.GetFreepsCard("ferevellon");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(ferevellon, arwen, knight);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(cavalry, cavalry2);
		assertInZone(Zone.SUPPORT, cavalry, cavalry2);

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(cavalry));
		scn.FreepsUseCardAction(cavalry);
		assertInDiscard(cavalry);
		// The other copy stays where it was.
		assertInZone(Zone.SUPPORT, cavalry2);

		// The [elven] companions; not the [gondor] companion, the [elven] Elf ally, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ferevellon, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, celeborn, frodo));
		scn.FreepsChooseCard(ferevellon);
		// Any minion: an [isengard] Uruk-hai and a [moria] Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer, slayer2, troop));
		scn.FreepsChooseCard(slayer);
		assertTrue(scn.IsCharAssignedAgainst(ferevellon, slayer));
		assertFalse(scn.IsCharAssigned(arwen));
		assertFalse(scn.IsCharAssigned(slayer2));
		assertFalse(scn.IsCharAssigned(troop));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, slayer2}, new PhysicalCardImpl[]{knight, troop});
		scn.FreepsResolveSkirmish(ferevellon);

		// Ferevellon could exert, and the Slayer could pay (1); neither is offered its Skirmish ability.  Arwen, who
		// is not one of those characters, is offered hers in this same skirmish (a minion skirmishing an Elf), and
		// the other Slayer is offered his.
		assertEquals(0, scn.GetWoundsOn(ferevellon));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ferevellon));
		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsPass();
		assertTrue(scn.GetTwilight() >= 1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
		assertTrue(scn.ShadowActionAvailable(slayer2));
	}

	@Test
	public void WithNoElvenCompanionItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetFreepsCard("cavalry");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(cavalry);
		assertInZone(Zone.SUPPORT, cavalry);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The Knight, Frodo and an Elf ally: no [elven] companion.  It is still offered, discards itself and
		// assigns nobody.
		assertTrue(scn.FreepsActionAvailable(cavalry));
		scn.FreepsUseCardAction(cavalry);
		assertInDiscard(cavalry);
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(knight));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetFreepsCard("cavalry");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(cavalry);
		assertInZone(Zone.SUPPORT, cavalry);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cavalry));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(cavalry));
	}

	@Test
	public void TheBarHoldsInALaterSkirmishOfThePhaseAndIsGoneOnceTheRegroupPhaseHasBegun() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetFreepsCard("cavalry");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(arwen, knight);
		scn.MoveCardsToSupportArea(cavalry);
		assertInZone(Zone.SUPPORT, cavalry);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Arwen, the one [elven] companion, is chosen unasked.
		scn.FreepsUseCardAction(cavalry);
		scn.FreepsChooseCard(savage);
		assertTrue(scn.IsCharAssignedAgainst(arwen, savage));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(knight, savage2);

		// The Knight's skirmish first; then Arwen's, where she is still barred.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));

		// Arwen, 6, beats the Savage, 5, and takes no wound.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(arwen));

		// A second move, and a second assignment and skirmish phase of the same turn: the bar is over.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// Both Savages lived through their losses (strength 6 against 5) and are still on the table.
		assertInPlay(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{knight, savage2});
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arwen));
	}

	@Test
	public void AResponseAbilityOfThoseCharactersUsedDuringTheSkirmishIsNotBarred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cavalry = scn.GetFreepsCard("cavalry");
		var shrouded = scn.GetFreepsCard("shrouded");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(shrouded);
		scn.MoveCardsToSupportArea(cavalry);
		scn.MoveCardsToHand(defiance);
		assertInHand(defiance);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The Shrouded Elf, the one [elven] companion, and the Troop, the one minion: both chosen unasked.
		scn.FreepsUseCardAction(cavalry);
		assertTrue(scn.IsCharAssignedAgainst(shrouded, troop));
		scn.PassAssignmentActions();
		scn.FreepsResolveSkirmish(shrouded);

		// A Response ability used in the skirmish is no skirmish special ability: it is still his to use.
		assertEquals(0, scn.GetWoundsOn(shrouded));
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(shrouded));
		scn.FreepsChooseOptionalTrigger(shrouded);
		assertEquals(1, scn.GetWoundsOn(shrouded));
		// Offered again for the same play while he can still exert (CR 4.2 "response"): declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(defiance, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
