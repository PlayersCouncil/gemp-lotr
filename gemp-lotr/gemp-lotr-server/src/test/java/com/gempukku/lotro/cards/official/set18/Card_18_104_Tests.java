package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weapons", "18_104");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; valiant is its whole text
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion; a skirmish ability needing a mount, not used
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion (a Man only beside 3 [rohan] companions or
					// Éowyn); his trigger answers another [rohan] Man exerting, and nobody exerts here
					put("merry", "103_85");
					put("spear", "4_288");      // Rider's Spear: [rohan] hand weapon, "Bearer must be a [rohan] Man"
					put("mount", "4_287");      // Rider's Mount: [rohan] mount, "Bearer must be a [rohan] Man"; a start-of-skirmish trigger
					put("chain1", "4_278");     // Heavy Chain: [rohan] armor, "Bearer must be a Man"; no number
					put("chain2", "4_278");
					put("armor", "1_92");       // Armor: [gondor] armor, "Bearer must be a Man"; a wound cap
					// Protecting the Hall: [rohan] condition on a [rohan] Man; an assignment ability used only on purpose
					put("hall", "11_149");
					// Stronghold of My Fathers: [rohan] possession in the support area, borne by nobody; a response to a
					// [rohan] companion's wound, and none is wounded where it lies
					put("stronghold", "102_48");
					put("oldtoby", "1_305");    // Old Toby: [shire] possession in the support area, borne by nobody
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability only
					put("phial", "10_13");      // Phial of Galadriel: [elven] artifact borne by a Hobbit; a skirmish ability only

					put("bree", "11_90");       // Man of Bree: [men] Man minion, vitality 1; strength +1 for each card in the Free Peoples hand
					put("bree2", "11_90");
					put("dagger", "18_69");     // Henchman's Dagger: [men] possession on a [men] minion; strength +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SurrenderedWeaponsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Surrendered Weapons
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time the fellowship moves, you may spot a [rohan] companion to discard a possession a character is bearing from play.<br><b>Skirmish:</b> Add (1) to transfer a [rohan] possession borne by a companion to another eligible bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weapons");

		assertEquals("Surrendered Weapons", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveDiscardsAPossessionACharacterIsBearingAndAMinionsAtTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		var saga = scn.GetFreepsCard("saga");
		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(weapons, oldtoby);
		scn.AttachCardsTo(guard, spear);
		scn.AttachCardsTo(herdsman, mount);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(frodo, phial);
		assertAttachedTo(spear, guard);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(phial, frodo);
		assertInZone(Zone.SUPPORT, weapons, oldtoby);

		var bree = scn.GetShadowCard("bree");
		var dagger = scn.GetShadowCard("dagger");

		scn.StartGame();

		// The move out of the fellowship phase.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(weapons));
		scn.FreepsAcceptOptionalTrigger();
		// Possessions borne by a companion and by an ally; not one in the support area, not a condition borne,
		// not an artifact borne.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, mount));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(oldtoby, saga, phial));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(spear);

		assertInDiscard(spear);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(phial, frodo);
		assertInZone(Zone.SUPPORT, oldtoby);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup phase, with a minion that lived through the turn bearing a Shadow possession.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(bree);
		scn.AttachCardsTo(bree, dagger);
		assertAttachedTo(dagger, bree);
		scn.FreepsRefreshDecision();
		scn.PassCurrentPhaseActions();

		// The second move of the turn: offered again.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(weapons));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(mount, dagger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(oldtoby, saga, phial));
		scn.FreepsChooseCard(dagger);

		assertInDiscard(dagger);
		assertInZone(Zone.SHADOW_CHARACTERS, bree);
		assertAttachedTo(mount, herdsman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesThePossessionAndStayingAtTheRegroupMoveOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(weapons);
		scn.AttachCardsTo(guard, spear);
		assertAttachedTo(spear, guard);

		scn.StartGame();

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(weapons));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(spear, guard);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Staying at the regroup move decision is no move: nothing is offered, and the turn ends.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertAttachedTo(spear, guard);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void NotOfferedWithNoRohanCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var armor = scn.GetFreepsCard("armor");
		// A [rohan] ally and a [gondor] companion, each bearing a possession.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(weapons);
		scn.AttachCardsTo(herdsman, mount);
		scn.AttachCardsTo(aragorn, armor);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(armor, aragorn);

		scn.StartGame();

		scn.FreepsPass();
		// Nothing was offered: the move is made and the Shadow phase begins.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(armor, aragorn);
	}

	@Test
	public void ARohanCompanionWhoIsAHobbitCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount = scn.GetFreepsCard("mount");
		// Merry, the one [rohan] companion, is no Man here.
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(weapons);
		scn.AttachCardsTo(herdsman, mount);
		assertAttachedTo(mount, herdsman);

		scn.StartGame();

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(weapons));
		scn.FreepsAcceptOptionalTrigger();
		// The one possession borne is discarded unasked.
		assertInDiscard(mount);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoPossessionBorneByAnyone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var guard = scn.GetFreepsCard("guard");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(weapons, oldtoby);
		assertInZone(Zone.SUPPORT, weapons, oldtoby);

		scn.StartGame();

		scn.FreepsPass();
		// The spot can be made, and the only possession in play is borne by nobody: no offer.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, oldtoby);
	}

	@Test
	public void SkirmishAbilityAddsOneToTransferARohanPossessionBorneByACompanionToAnotherEligibleBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chain1 = scn.GetFreepsCard("chain1");
		var chain2 = scn.GetFreepsCard("chain2");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var armor = scn.GetFreepsCard("armor");
		var hall = scn.GetFreepsCard("hall");
		var stronghold = scn.GetFreepsCard("stronghold");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, rider, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(weapons, stronghold);
		assertInZone(Zone.SUPPORT, stronghold);
		scn.AttachCardsTo(guard, chain1, hall);
		scn.AttachCardsTo(rider, spear);
		scn.AttachCardsTo(herdsman, mount);
		scn.AttachCardsTo(aragorn, armor);
		assertAttachedTo(chain1, guard);
		assertAttachedTo(hall, guard);
		assertAttachedTo(spear, rider);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(armor, aragorn);

		var bree = scn.GetShadowCard("bree");
		var bree2 = scn.GetShadowCard("bree2");

		scn.StartGame();
		scn.MoveMinionsToTable(bree, bree2);
		// A [rohan] possession borne by a minion (as this card's own transfer can leave one).
		scn.AttachCardsTo(bree2, chain2);
		assertAttachedTo(chain2, bree2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, bree);
		// The other Man of Bree is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		int twilight = scn.GetTwilight();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(weapons));

		scn.FreepsUseCardAction(weapons);
		// [rohan] possessions borne by companions, of two classes; not one borne by an ally or by a minion, nor one
		// borne by nobody, not a [rohan] condition, not a [gondor] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chain1, spear));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(mount, chain2, stronghold, hall, armor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chain1);

		// "Bearer must be a Man": a companion, an ally and a Shadow minion may bear it; not its bearer, not Aragorn
		// or the other Man of Bree (each bears an armor already), not Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, herdsman, bree));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn, bree2, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rider);

		assertAttachedTo(chain1, rider);
		assertAttachedTo(chain2, bree2);
		assertInZone(Zone.SUPPORT, stronghold);
		assertAttachedTo(hall, guard);
		assertAttachedTo(spear, rider);
		// The (1) of the cost, and nothing for the Chain's own twilight cost of 1.
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanPossessionOnACompanionTheTwilightIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mount = scn.GetFreepsCard("mount");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(weapons);
		scn.AttachCardsTo(herdsman, mount);
		scn.AttachCardsTo(aragorn, armor);
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(armor, aragorn);

		var bree = scn.GetShadowCard("bree");

		scn.StartGame();
		scn.MoveMinionsToTable(bree);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, bree);
		scn.FreepsResolveSkirmish(guard);

		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(weapons));

		scn.FreepsUseCardAction(weapons);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertAttachedTo(mount, herdsman);
		assertAttachedTo(armor, aragorn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var chain1 = scn.GetFreepsCard("chain1");
		scn.MoveCompanionsToTable(guard, rider);
		scn.MoveCardsToSupportArea(weapons);
		scn.AttachCardsTo(guard, chain1);
		assertAttachedTo(chain1, guard);

		var bree = scn.GetShadowCard("bree");

		scn.StartGame();
		scn.MoveMinionsToTable(bree);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// A [rohan] possession on a companion and another eligible bearer beside him.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(weapons));
		assertAttachedTo(chain1, guard);
	}
}
