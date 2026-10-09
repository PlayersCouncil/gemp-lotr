package com.gempukku.lotro.cards.unofficial.pc.errata.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_020_ErrataTests
{
	// Every skirmish here is fought at site 2 of the Fellowship path, Ettenmoors ("Skirmish: Exert your
	// companion or minion to make that character strength +2."), which is the Bounder's home site.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("protector", "63_20");
					put("protector2", "63_20");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("bounder", "1_286");    // Bounder: ally, home site 2, strength 2, vitality 2; a skirmish ability never used here

					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3; Damage +1 is its whole text
					put("party2", "1_158");
					put("soldier", "1_271");    // Orc Soldier: strength 7, vitality 2; "Skirmish: Exert this minion to wound a character he is skirmishing."
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("skirmisher", "18_90"); // Orkish Skirmisher: strength 9, vitality 3; each time it wins a skirmish, may exert twice to wound a companion
					put("skirmisher2", "18_90");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienProtectorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Lorien Protector
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	While another companion is assigned to a skirmish, during each skirmish, prevent all wounds to this
		 * 	companion after the first wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("protector");

		assertEquals("Lorien Protector", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedFromHandForTwoAsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		scn.MoveCardsToHand(protector);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(protector));
		scn.FreepsPlayCard(protector);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, protector);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileAnotherCompanionIsAssignedTheDamageWoundAfterTheFirstWoundIsPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(protector, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(protector);

		// Aragorn's skirmish has not been fought: he is still assigned to it.
		assertTrue(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetWoundsOn(protector));
		scn.PassCurrentPhaseActions();

		// The Protector (6) loses to the Raiding Party (9, damage +1): the first wound is taken and the second prevented.
		assertEquals(1, scn.GetWoundsOn(protector));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void EveryWoundAfterTheFirstIsPreventedWhateverItComesFrom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(protector, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		// The Orc Soldier is given Damage +1, so that losing to him is two wounds.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, soldier, null, Keyword.DAMAGE, 1));
		assertTrue(scn.HasKeyword(soldier, Keyword.DAMAGE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, soldier}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(protector);

		assertEquals(0, scn.GetWoundsOn(protector));
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.FreepsPass();
		// The Soldier exerts to wound the character he is skirmishing: the first wound of this skirmish, and taken.
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(protector));

		scn.PassCurrentPhaseActions();

		// The Protector (6) then loses to the Soldier (7): the wound for losing and the damage wound are both prevented.
		assertEquals(1, scn.GetWoundsOn(protector));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void OnlyTheProtectorIsProtectedAndNotOnceTheOtherCompanionsSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var protector = scn.GetFreepsCard("protector");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(protector, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, party}, new PhysicalCardImpl[]{protector, party2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharAssigned(protector));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassCurrentPhaseActions();

		// Aragorn (8) loses to a Raiding Party (9, damage +1) while the Protector is assigned to a skirmish:
		// both wounds are taken. The Protector's text is about the Protector.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(protector);

		// Aragorn's skirmish is over, so he is no longer assigned to one; Frodo never was.
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(protector));
		scn.PassCurrentPhaseActions();

		// With no other companion assigned to a skirmish the Protector takes both wounds.
		assertEquals(2, scn.GetWoundsOn(protector));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASecondProtectorIsAnotherCompanionUntilItsOwnSkirmishIsTheOnlyOneLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		var protector2 = scn.GetFreepsCard("protector2");
		var party = scn.GetShadowCard("party");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(protector, protector2);

		scn.StartGame();
		scn.MoveMinionsToTable(party, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{protector2, party2});
		scn.FreepsResolveSkirmish(protector);

		assertTrue(scn.IsCharAssigned(protector2));
		scn.PassCurrentPhaseActions();

		// The first to fight has the other still assigned: one wound of two. The other, waiting, takes none.
		assertEquals(1, scn.GetWoundsOn(protector));
		assertEquals(0, scn.GetWoundsOn(protector2));

		scn.FreepsResolveSkirmish(protector2);

		assertFalse(scn.IsCharAssigned(protector));
		scn.PassCurrentPhaseActions();

		// The second fights with no other companion assigned: both wounds.
		assertEquals(2, scn.GetWoundsOn(protector2));
		assertEquals(1, scn.GetWoundsOn(protector));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRingBearerAssignedToASkirmishIsAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var protector = scn.GetFreepsCard("protector");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(protector);

		scn.StartGame();
		scn.MoveMinionsToTable(party, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{frodo, savage});
		scn.FreepsResolveSkirmish(protector);

		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(protector));
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(protector));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnAllyAssignedToASkirmishIsNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var protector = scn.GetFreepsCard("protector");
		var bounder = scn.GetFreepsCard("bounder");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(protector);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(party, savage);
		scn.SkipToAssignments();
		// Site 2 is the Bounder's home, so he may be assigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{bounder, savage});
		scn.FreepsResolveSkirmish(protector);

		// A Free Peoples character other than the Protector is assigned to a skirmish, but he is an ally; the
		// only other companion, Frodo, is not assigned.
		assertTrue(scn.IsCharAssigned(bounder));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(protector));
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(protector));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AnExertionInTheSkirmishIsNotTheFirstWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(protector, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(protector);

		assertEquals(0, scn.GetWoundsOn(protector));
		assertEquals(6, scn.GetStrength(protector));
		// Ettenmoors: the Protector exerts to be strength +2.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(protector);
		assertEquals(1, scn.GetWoundsOn(protector));
		assertEquals(8, scn.GetStrength(protector));

		scn.PassCurrentPhaseActions();

		// 8 against 9: he loses. An exertion is not a wound, so the wound for losing is the first wound of
		// this skirmish and is taken; the damage wound is prevented. 1 exertion + 1 wound
		assertEquals(2, scn.GetWoundsOn(protector));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void EachSkirmishHasAFirstWoundOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(protector, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party, savage);
		// Every minion is made fierce, so that the Protector fights twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(protector);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(protector));

		// Aragorn (8) beats the Uruk Savage (5), who survives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{protector, party}, new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(protector);

		assertTrue(scn.IsCharAssigned(aragorn));
		scn.PassCurrentPhaseActions();

		// A new skirmish: its first wound is taken and its damage wound prevented. With one first wound a
		// turn he would still have 1; with no protection the second skirmish would have killed him (vitality 3).
		assertEquals(2, scn.GetWoundsOn(protector));
		assertInZone(Zone.FREE_CHARACTERS, protector);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WoundsToTheProtectorInAnotherCompanionsSkirmishAreAlsoPreventedAfterTheFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protector = scn.GetFreepsCard("protector");
		var aragorn = scn.GetFreepsCard("aragorn");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var skirmisher2 = scn.GetShadowCard("skirmisher2");
		scn.MoveCompanionsToTable(protector, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, skirmisher2);
		// Aragorn faces both Skirmishers (defender +1) and loses without being overwhelmed: 13 against 18, and
		// 18 is under double 13. The Protector is assigned to nothing.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.DEFENDER, 1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, skirmisher, skirmisher2);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(aragorn));
		assertFalse(scn.IsCharAssigned(protector));
		assertFalse(scn.IsCharSkirmishing(protector));
		assertEquals(0, scn.GetWoundsOn(protector));
		scn.PassCurrentPhaseActions();

		// Both Skirmishers have won, and wound a companion each; Aragorn's skirmish is the one being fought until
		// these have been carried out (CR 4.2 "skirmish phase(s)"). Each Skirmisher exerts twice.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher2));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertTrue(scn.ShadowHasCardChoiceAvailable(protector));
		scn.ShadowChooseCard(protector);

		assertEquals(2, scn.GetWoundsOn(skirmisher));
		assertEquals(1, scn.GetWoundsOn(protector));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher2));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(protector);

		// The second wound of this skirmish phase is prevented. Without the card the Protector would have 2.
		assertEquals(2, scn.GetWoundsOn(skirmisher2));
		assertEquals(1, scn.GetWoundsOn(protector));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
