package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("champion", "7_49");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and no ability
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("horde", "6_28");       // Ent Horde: [gandalf] Ent companion, vitality 5; defender +1 only with more minions than companions
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, a character who is no companion; a fellowship ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SteadfastChampionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Steadfast Champion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be Gandalf.<br>Each minion gains this ability: "<b>Assignment:</b> Assign this minion to Gandalf."<br><b>Regroup:</b> Discard this condition to discard a minion and heal a companion 3 times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("champion");

		assertEquals("Steadfast Champion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnGandalfAndOnNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(champion, gandalf);
		scn.MoveCompanionsToTable(avenger, aragorn);

		scn.StartGame();

		// A [gandalf] companion of another title and a companion of another culture: neither may bear it.
		assertInHand(champion, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(champion));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(champion));
		scn.FreepsPlayCard(champion);

		// Gandalf is the only bearer there can be (one Gandalf in play), so no choice of bearer is asked.
		assertAttachedTo(champion, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionMayAssignItselfToGandalfInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		// Ent Avenger is a [gandalf] companion who is not Gandalf, Aragorn a companion of another culture.
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		assertAttachedTo(champion, gandalf);
		scn.MoveMinionsToTable(troop, savage);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The maneuver phase: the ability is an assignment one.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop));
		assertFalse(scn.ShadowActionAvailable(savage));
		scn.ShadowPass();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();

		// A [moria] Orc and an [isengard] Uruk-hai each have it.
		assertTrue(scn.ShadowActionAvailable(troop));
		assertTrue(scn.ShadowActionAvailable(savage));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertFalse(scn.IsCharAssigned(savage));

		scn.ShadowUseCardAction(savage);

		// Gandalf is the only one it can name, so no choice is asked.
		assertTrue(scn.IsCharAssignedAgainst(gandalf, savage));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(avenger));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToDiscardAMinionAndHealOneCompanionThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horde = scn.GetFreepsCard("horde");
		var celeborn = scn.GetFreepsCard("celeborn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, aragorn, horde);
		scn.MoveAlliesToTable(celeborn);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		// Ent Horde, vitality 5, carries four wounds, so a fourth heal (or only two) would show.
		scn.AddWoundsToChar(horde, 4);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(4, scn.GetWoundsOn(horde));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertAttachedTo(champion, gandalf);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(champion));
		scn.FreepsUseCardAction(champion);

		// The cost.
		assertInDiscard(champion);
		// Minions of two cultures and races are offered; companions are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn, horde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInDiscard(troop);
		assertInPlay(savage);

		// Wounded companions are offered; a wounded ally and a wounded minion are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(horde, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(horde);

		// Three heals, all on the one companion chosen: 4 - 3.
		assertEquals(1, scn.GetWoundsOn(horde));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionTheCompanionIsStillHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var horde = scn.GetFreepsCard("horde");
		scn.MoveCompanionsToTable(gandalf, horde);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.AddWoundsToChar(horde, 4);
		assertEquals(4, scn.GetWoundsOn(horde));
		assertAttachedTo(champion, gandalf);

		// No minion, so the game goes from the Shadow phase straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(champion));
		scn.FreepsUseCardAction(champion);

		assertInDiscard(champion);
		// The one wounded companion is healed with nothing asked.
		assertEquals(1, scn.GetWoundsOn(horde));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedCompanionTheMinionIsStillDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertAttachedTo(champion, gandalf);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInPlay(troop);
		assertTrue(scn.FreepsActionAvailable(champion));
		scn.FreepsUseCardAction(champion);

		assertInDiscard(champion, troop);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var horde = scn.GetFreepsCard("horde");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, horde);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(horde, 2);
		assertEquals(2, scn.GetWoundsOn(horde));
		assertAttachedTo(champion, gandalf);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(champion));
		assertAttachedTo(champion, gandalf);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("champion", "7_49");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and no ability
					// Reckless Pride: the same ability for its own bearer, a [dwarven] companion
					put("pride", "7_13");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability, not used here
					put("troop1", "1_177");     // Goblin Patrol Troop: no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The ability costs nothing, so it is not offered where it would assign nobody (Ketura on Q-0034, F-0353; the
	// erratum 57_49 was corrected the same way, F-0252; CR 4.2 "assignment phase": "Many assignment actions assign
	// a minion to a companion.  You cannot do this unless both of them are unassigned").
	@Test
	public void EachMinionMayAssignItselfToGandalfAndNotOnceEitherIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pride = scn.GetFreepsCard("pride");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.AttachCardsTo(gandalf, champion);
		scn.AttachCardsTo(gimli, pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion has the ability, in the words of this card.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Assign this minion to Gandalf"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Assign this minion to Gandalf"));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.ShadowUseCardAction(troop1, "Assign this minion to Gandalf");

		assertTrue(scn.IsCharAssignedAgainst(gandalf, troop1));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(troop2));

		scn.FreepsPass();

		// The assigned minion has the ability no longer, and neither has the other minion now that Gandalf is
		// assigned.  Reckless Pride's, whose bearer is not, is still the unassigned minion's to use.
		assertFalse(scn.ShadowActionAvailable(troop1, "Assign this minion to Gandalf"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Assign this minion to Gandalf"));
		assertFalse(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Reckless Pride"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
