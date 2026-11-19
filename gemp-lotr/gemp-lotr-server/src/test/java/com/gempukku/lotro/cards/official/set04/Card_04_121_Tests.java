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

public class Card_04_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forests", "4_121");
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7, vitality 3, keywords only
					put("damrod", "4_114");     // Damrod, Ranger of Ithilien: Ring-bound Man, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability used only on purpose
					put("brigand1", "4_10");    // Dunlending Brigand: [dunland] Man minion, site 3, strength 8, vitality 1, no text
					put("brigand2", "4_10");
					// Easterling Axeman: [raider] Man minion, site 4, strength 6; +3 and fierce only at 2 burdens (none here)
					put("axeman", "4_224");
					// Goblin Runner: [moria] Orc, site 4; its "when you play" text does nothing when cheated in
					put("runner", "1_178");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forests", "4_121");
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, strength 7, vitality 3
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, site 3, strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForestsofIthilienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Forests of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>While the fellowship is at site 5[T], the site number of each Man in a skirmish is +2.<br><b>Regroup:</b> Discard this condition and exert a Ring-bound Man to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forests");

		assertEquals("Forests of Ithilien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite5TEachManInTheSkirmishBeingFoughtIsSiteNumberPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var axeman = scn.GetShadowCard("axeman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToSupportArea(forests);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(brigand1, brigand2, axeman, runner);
		scn.SkipToAssignments();

		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, brigand1 },
				new PhysicalCardImpl[] { boromir, brigand2 }
		);
		// The Shadow player puts the minions left over on Aragorn: two Men and an Orc in one skirmish.
		scn.ShadowAssignToMinions(aragorn, axeman, runner);

		// Assigned, but no skirmish is being fought yet: the printed numbers.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(3, scn.GetMinionSiteNumber(brigand1));
		assertEquals(4, scn.GetMinionSiteNumber(axeman));
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertEquals(3, scn.GetMinionSiteNumber(brigand2));
		assertFalse(scn.HasKeyword(axeman, Keyword.ROAMING));

		scn.FreepsResolveSkirmish(aragorn);
		// Each Man in the skirmish, of two cultures: +2. The Orc in it, and the Man waiting for his own skirmish, are not.
		assertEquals(5, scn.GetMinionSiteNumber(brigand1));
		assertEquals(6, scn.GetMinionSiteNumber(axeman));
		assertTrue(scn.HasKeyword(axeman, Keyword.ROAMING));
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertEquals(3, scn.GetMinionSiteNumber(brigand2));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		// The next skirmish: its Man is +2, and those whose skirmish is over are themselves again.
		assertEquals(5, scn.GetMinionSiteNumber(brigand2));
		assertEquals(3, scn.GetMinionSiteNumber(brigand1));
		assertEquals(4, scn.GetMinionSiteNumber(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite4TAManInASkirmishKeepsHisSiteNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var boromir = scn.GetFreepsCard("boromir");
		var brigand = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(forests);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();

		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite5OfTheFellowshipBlockAManInASkirmishKeepsHisSiteNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var forests = scn.GetFreepsCard("forests");
		var boromir = scn.GetFreepsCard("boromir");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(forests);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();

		// The Bridge of Khazad-dum, site 5 of the Fellowship block, which prints no block symbol.
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsItAndExertsARingBoundManToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var boromir = scn.GetFreepsCard("boromir");
		var damrod = scn.GetFreepsCard("damrod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, damrod, aragorn);
		scn.MoveCardsToSupportArea(forests);

		scn.StartGame();
		scn.SkipToSite(4);
		// Sites 1 and 2 to the Shadow player, site 3 to the Free Peoples player, lowest first.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertInZone(Zone.SUPPORT, forests);
		assertTrue(scn.FreepsActionAvailable(forests));

		scn.FreepsUseCardAction(forests);
		// Ring-bound Men of two titles; not an unbound Man, nor the Ring-bound Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, damrod));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(damrod);

		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(forests);
		// The highest site an opponent controls goes back on the path; his own site 3 is passed over.
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheCostIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(forests);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, forests);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(forests));

		scn.FreepsUseCardAction(forests);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInDiscard(forests);
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithTheOnlyRingBoundManExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forests = scn.GetFreepsCard("forests");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToSupportArea(forests);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.ShadowControls(site1));
		// Vitality 3 with 2 wounds: exhausted. Aragorn and Frodo could still exert.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forests));
		assertInZone(Zone.SUPPORT, forests);
		assertTrue(scn.ShadowControls(site1));
	}
}
