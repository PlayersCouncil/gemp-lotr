package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isen", "4_31");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, no text
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man; "Saruman may not be
					// assigned to a skirmish".  His trigger answers a move: he is laid after it.
					put("saruman", "4_33");
					// Saruman's Staff: on Saruman; its maneuver ability makes his first sentence not apply until the
					// regroup phase, so that nothing but the card under test keeps him from being assigned.
					put("staff", "4_174");
					// Easterling Axeman: a [raider] Man; stronger and fierce only at 2 burdens.
					put("axeman", "4_224");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					// Thrarin, Dwarven Smith: a Dwarf ally whose home is site 3, where he may be assigned to a skirmish.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OvertheIsenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Over the Isen
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Assignment:</b> Spot a site you control and remove (2) to assign a [dunland] Man to an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("isen");

		assertEquals("Over the Isen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AssignmentActionAssignsADunlandManOfYourChoiceToAnUnboundCompanionOfYourChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(thrarin);

		var isen = scn.GetShadowCard("isen");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(isen);
		scn.SetTwilight(0);

		// To site 3, Thrarin's home.
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage, saruman, axeman);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The ally at his home, and Saruman under his Staff, may each be assigned by an action.
		assertTrue(scn.CanBeAssignedViaAction(thrarin));
		assertTrue(scn.CanBeAssignedViaAction(saruman));
		// 4 companions + 0 for Rivendell Valley
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(isen));
		scn.ShadowUseCardAction(isen);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		// The Ring-bearer, a Ring-bound companion who is not, and an ally who may be assigned at his home.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, thrarin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		// A [dunland] minion who is no Man, and a Man of another culture.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertTrue(scn.IsCharAssignedAgainst(gimli, savage));
		assertFalse(scn.IsCharAssigned(brigand));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentActionIsNotOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var isen = scn.GetShadowCard("isen");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		scn.MoveCardsToSupportArea(isen);
		scn.SetTwilight(10);

		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// 10 set + 2 companions + 0 for Rivendell Valley: the cost could be paid.
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(isen));
	}

	@Test
	public void AssignmentActionIsNotOfferedWithOneTwilightTokenAndIsWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var isen = scn.GetShadowCard("isen");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(isen);

		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(isen));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(isen));
		scn.ShadowUseCardAction(isen);

		// One Man and one unbound companion: each is chosen unasked.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, brigand));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoDunlandManInPlayTheTwilightIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var isen = scn.GetShadowCard("isen");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(isen);
		scn.SetTwilight(2);

		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// 2 set + 2 companions + 0 for Rivendell Valley
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(isen));
		scn.ShadowUseCardAction(isen);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(axeman));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheTwilightIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo, the Ring-bearer, and Sam, who is Ring-bound: neither is an unbound companion.  Thrarin is an ally, at
		// his home site 3, where he may be assigned by an action.
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);

		var isen = scn.GetShadowCard("isen");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(isen);
		scn.SetTwilight(2);

		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.CanBeAssignedViaAction(thrarin));
		// 2 set + 2 companions + 0 for Rivendell Valley
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(isen));
		scn.ShadowUseCardAction(isen);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.IsCharAssigned(brigand));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(thrarin));
		assertFalse(scn.IsCharAssigned(scn.GetRingBearer()));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
