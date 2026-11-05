package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_022_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2); after SkipToSite(2), at Rivendell (site 3), a sanctuary with no
	// companion wounded.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bill", "33_22");
					// Thorin, Oakenshield: [dwarven] Dwarf companion; strength +1 for each [dwarven] follower (none) and a
					// fellowship ability only
					put("thorin", "30_19");
					// Elrond, Elven Lord: [elven] Elf ally, home 3H; a start-of-turn heal of a wounded Wise ally and a
					// regroup ability only
					put("elrond", "30_21");
					// Betsy Butterbur, Prancing Pony Proprietor: [gandalf] Man ally, home 1H; cannot take wounds, and a
					// fellowship ability only
					put("betsy", "33_19");
					// Master Worrywort, Hobbit Farmer: [shire] Hobbit ally, home 1H; a trigger on Bilbo's wins and a
					// skirmish ability only
					put("worrywort", "33_53");
					// Hobbit Party Guest: [shire] Hobbit ally whose home is site 1 of the Fellowship block (1F); a
					// start-of-turn heal of itself and a fellowship ability only
					put("guest", "1_297");

					// Goblin Footman: [moria] Orc, strength 8, vitality 1; a play trigger only
					put("footman", "30_42");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BillFernySrStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Bill Ferny Sr., Servant of Azog
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 2
		 * Game Text: The Free Peoples player may not assign a character to skirmish Bill Ferny Sr.<br>Bill Ferny Sr. may not be assigned to skirmish Bilbo.<br><b>Regroup:</b> Kill an ally whose home is site 1 to return Bill Ferny Sr. to your hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bill");

		assertEquals("Bill Ferny Sr.", card.getBlueprint().getTitle());
		assertEquals("Servant of Azog", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheFreePeoplesPlayerCanAssignNoCharacterToBillAndTheShadowPlayerCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var bill = scn.GetShadowCard("bill");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(elrond);
		scn.MoveMinionsToTable(bill, footman);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SUPPORT, elrond);

		scn.SkipToAssignments();
		// Rivendell, site 3, is Elrond's home: he may be assigned as the companions may.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));

		// No character, Ring-bearer, companion or ally, may be put against Bill by the Free Peoples player; each may
		// be put against the Footman beside him.
		assertFalse(scn.FreepsCanAssignAgainst(bilbo, bill));
		assertFalse(scn.FreepsCanAssignAgainst(thorin, bill));
		assertFalse(scn.FreepsCanAssignAgainst(elrond, bill));
		assertTrue(scn.FreepsCanAssignAgainst(bilbo, footman));
		assertTrue(scn.FreepsCanAssignAgainst(thorin, footman));
		assertTrue(scn.FreepsCanAssignAgainst(elrond, footman));

		scn.FreepsAssignToMinions(thorin, footman);
		assertTrue(scn.IsCharAssignedAgainst(thorin, footman));
		assertFalse(scn.IsCharAssigned(bill));

		// The Shadow player may assign him, though not to Bilbo.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowCanAssignAgainst(bilbo, bill));
		assertTrue(scn.ShadowCanAssignAgainst(thorin, bill));
		scn.ShadowAssignToMinions(thorin, bill);
		assertTrue(scn.IsCharAssignedAgainst(thorin, bill));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void BillIsNotOnTheFreePeoplesPlayersListOfMinionsToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var bill = scn.GetShadowCard("bill");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveMinionsToTable(bill, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, bill, footman);

		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertFalse(scn.FreepsCanAssign(bill));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(bill));
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsCanAssign(footman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(footman));

		scn.FreepsAssignToMinions(bilbo, footman);
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(bill));
		scn.ShadowAssignToMinions(thorin, bill);
		assertTrue(scn.IsCharAssignedAgainst(thorin, bill));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithBillTheOnlyMinionTheFreePeoplesPlayerIsNotAskedToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var bill = scn.GetShadowCard("bill");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveMinionsToTable(bill);
		assertInZone(Zone.SHADOW_CHARACTERS, bill);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// Nothing for the Free Peoples player to assign: the Shadow player is asked straight away.
		assertFalse(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowCanAssignAgainst(bilbo, bill));
		scn.ShadowAssignToMinions(thorin, bill);
		assertTrue(scn.IsCharAssignedAgainst(thorin, bill));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheShadowPlayerMayAssignBillToAnyCompanionButBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var bill = scn.GetShadowCard("bill");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveMinionsToTable(bill, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, bill, footman);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();

		// Both minions are left to the Shadow player: Bill may not go to Bilbo, the Footman may; Bill may go to Thorin.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertFalse(scn.ShadowCanAssignAgainst(bilbo, bill));
		assertTrue(scn.ShadowCanAssignAgainst(bilbo, footman));
		assertTrue(scn.ShadowCanAssignAgainst(thorin, bill));
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{bilbo, footman}, new PhysicalCardImpl[]{thorin, bill});
		assertTrue(scn.IsCharAssignedAgainst(bilbo, footman));
		assertTrue(scn.IsCharAssignedAgainst(thorin, bill));
		assertFalse(scn.IsCharAssignedAgainst(bilbo, bill));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void RegroupActionKillsAnAllyWhoseHomeIsSite1ToReturnBillToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var elrond = scn.GetFreepsCard("elrond");
		var betsy = scn.GetFreepsCard("betsy");
		var worrywort = scn.GetFreepsCard("worrywort");
		var guest = scn.GetFreepsCard("guest");
		var bill = scn.GetShadowCard("bill");

		scn.StartGame();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(elrond, betsy, worrywort, guest);
		scn.MoveMinionsToTable(bill);
		assertInZone(Zone.SUPPORT, elrond, betsy, worrywort, guest);
		assertInZone(Zone.SHADOW_CHARACTERS, bill);

		// Bill stands unassigned into the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SHADOW_CHARACTERS, bill);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(bill));
		scn.ShadowUseCardAction(bill);
		// Betsy and Master Worrywort, of two cultures and races, both of home 1H; not Elrond (home 3H), nor the
		// Hobbit Party Guest (home site 1 of another block), nor Thorin (a companion).
		assertTrue(scn.ShadowHasCardChoiceAvailable(betsy, worrywort));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, guest, thorin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worrywort);

		// Killed, so in its owner's dead pile; Bill back in the Shadow player's hand.
		assertInZone(Zone.DEAD, worrywort);
		assertInZone(Zone.HAND, bill);
		assertInZone(Zone.SUPPORT, elrond, betsy, guest);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoAllyWhoseHomeIsSite1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var guest = scn.GetFreepsCard("guest");
		var bill = scn.GetShadowCard("bill");

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, guest);
		scn.MoveMinionsToTable(bill);
		assertInZone(Zone.SUPPORT, elrond, guest);
		assertInZone(Zone.SHADOW_CHARACTERS, bill);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Allies of home 3H and 1F only: nothing to pay with.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bill));
		assertInZone(Zone.SHADOW_CHARACTERS, bill);
		assertInZone(Zone.SUPPORT, elrond, guest);
	}
}
