package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worker", "15_135");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; Valiant is his whole text
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside 3 [rohan] companions
					// or Éowyn); his trigger answers a [rohan] Man exerting, and his abilities are used only on purpose
					put("merry", "103_85");
					// Haethen, Veteran Fighter: a [rohan] Man companion, strength 7, vitality 3; cannot take wounds during
					// fierce skirmishes; a skirmish ability used only on purpose
					put("haethen", "12_116");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
					// Uruk Seeker: fierce, strength 6, vitality 2; its other text is only against a bearer of a search card
					put("seeker", "4_195");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohanWorkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rohan Worker
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: 
		 * Game Text: <b>Aid</b> – (1).<br>Each time you transfer this to a companion, except a [rohan] companion, exert bearer twice.<br>Each time bearer wins a skirmish, you may liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("worker");

		assertEquals("Rohan Worker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, scn.GetAidTwilightCost(card));
	}

	@Test
	public void TransferredByItsAidToACompanionWhoIsNotRohanItExertsHimTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, guard, merry);
		scn.MoveCardsToSupportArea(worker);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// The Aid: (1) added.
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, merry, frodo));
		scn.FreepsChooseCard(aragorn);

		// A [gondor] Man: he exerts twice, and nobody else does.
		assertAttachedTo(worker, aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToARohanManNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToSupportArea(worker);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(guard);

		assertAttachedTo(worker, guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToARohanHobbitNobodyExertsAndTransferredOnToADwarfHeExertsAsFarAsHeCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(merry, gimli);
		scn.MoveCardsToSupportArea(worker);
		// Vitality 3 with 1 wound: he can exert once, and two wounds would kill him.
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.StartGame();
		assertInZone(Zone.SUPPORT, worker);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// A [rohan] companion who is no Man: the culture keeps him out, not the race.
		assertFalse(scn.IsRace(merry, Race.MAN));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(merry);
		assertAttachedTo(worker, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Nobody is assigned to the Troop, and the fellowship moves again: a second maneuver phase in the turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);

		// The second transfer exerts its bearer too: a [dwarven] Dwarf, exerted once and then exhausted, and not
		// wounded to death.
		assertAttachedTo(worker, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishHeMayLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		// Cheated on now: a follower goes back to the support area as each turn ends.
		scn.AttachCardsTo(haethen, worker);
		assertAttachedTo(worker, haethen);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.FreepsResolveSkirmish(haethen);

		// 7 against 5: Haethen wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker));
		scn.FreepsAcceptOptionalTrigger();

		// The highest site an opponent controls goes back on the path; the lower one, and the player's own, stay.
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertAttachedTo(worker, haethen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheLiberationLeavesTheSiteControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		scn.AttachCardsTo(haethen, worker);
		assertAttachedTo(worker, haethen);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker));
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		scn.AttachCardsTo(haethen, worker);
		assertAttachedTo(worker, haethen);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, troop);
		scn.FreepsResolveSkirmish(haethen);

		// 13 against 7, not doubled: Haethen loses and lives through one wound.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(haethen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(worker));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenACompanionWhoIsNotBearerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var haethen = scn.GetFreepsCard("haethen");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(haethen, guard);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		scn.AttachCardsTo(haethen, worker);
		assertAttachedTo(worker, haethen);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		// 6 against 5: the Guard, a [rohan] Man companion who could have borne the follower, wins without it.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(worker));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		// The player's own site is controlled, and no opponent's: there is nothing he can liberate.
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.AttachCardsTo(haethen, worker);
		assertAttachedTo(worker, haethen);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(worker));
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerWinningTheFierceSkirmishAsWellLiberatesASecondSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetFreepsCard("worker");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		scn.AttachCardsTo(haethen, worker);
		assertAttachedTo(worker, haethen);

		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, seeker);
		scn.FreepsResolveSkirmish(haethen);

		// 7 against 6: Haethen wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker));
		scn.FreepsAcceptOptionalTrigger();
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The fierce skirmish: he wins again, and the next site is liberated.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(haethen, seeker);
		scn.FreepsResolveSkirmish(haethen);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker));
		scn.FreepsAcceptOptionalTrigger();
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
