package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

public class Card_V3_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lance", "103_91");
					put("rider", "4_286");
					put("mount", "4_287");
					put("eowyn", "5_122");
					put("aragorn", "1_89");

					put("savage1", "1_151");
					put("savage2", "1_151");
					put("savage3", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VanguardsLanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Vanguard's Lance
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [Rohan] companion.
		* 	While bearer is mounted, bearer is strength +2 and each time bearer wins a skirmish, you must exert them to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lance");

		assertEquals("Vanguard's Lance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}


// ======== BEARER RESTRICTION TESTS ========

	@Test
	public void VanguardsLanceCanBePlacedOnRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(lance);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(lance));
		scn.FreepsPlayCard(lance);

		assertAttachedTo(lance, rider);
	}

	@Test
	public void VanguardsLanceCannotBePlacedOnNonRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(lance);

		scn.StartGame();

		// Aragorn is Gondor, not Rohan - lance should not be playable
		assertFalse(scn.FreepsPlayAvailable(lance));
	}

// ======== STRENGTH MODIFIER TESTS ========

	@Test
	public void VanguardsLanceGivesBase2StrengthWhenNotMounted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);

		scn.StartGame();

		int baseStrength = scn.GetStrength(rider);

		scn.AttachCardsTo(rider, lance);

		// Lance gives +2 strength (printed stat)
		assertEquals(baseStrength + 2, scn.GetStrength(rider));
	}

	@Test
	public void VanguardsLanceGives4TotalStrengthWhenMounted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);

		scn.StartGame();

		int baseStrength = scn.GetStrength(rider);

		scn.AttachCardsTo(rider, lance, mount);

		// Lance gives +2 base, +2 conditional when mounted = +4 total
		assertEquals(baseStrength + 4, scn.GetStrength(rider));
	}

	@Test
	public void VanguardsLanceLosesConditionalStrengthWhenMountRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, lance, mount);

		scn.StartGame();

		int mountedStrength = scn.GetStrength(rider);

		// Discard the mount
		scn.MoveCardsToDiscard(mount);

		// Should lose the +2 conditional bonus, keeping only the base +2
		assertEquals(mountedStrength - 2, scn.GetStrength(rider));
	}

// ======== REQUIRED TRIGGER TESTS ========

	@Test
	public void VanguardsLanceRequiredTriggerFiresOnSkirmishWin() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");
		var eowyn = scn.GetFreepsCard("eowyn");
		var frodo = scn.GetRingBearer();

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");

		scn.MoveCompanionsToTable(rider, eowyn);
		scn.AttachCardsTo(rider, lance, mount);
		scn.MoveMinionsToTable(savage1, savage2, savage3);

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign: Rider vs savage1, Frodo vs savage2, savage3 unassigned
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { rider, savage1 },
				new PhysicalCardImpl[] { frodo, savage2 }
		);
		scn.ShadowDeclineAssignments();

		// Resolve Rider's skirmish
		scn.FreepsResolveSkirmish(rider);

		// Mount exerts savage1 at start of skirmish (Rider's Mount ability)
		assertEquals(1, scn.GetWoundsOn(savage1));

		// Rider (mounted with lance) should win and trigger fires
		// Rider: base strength + 4 (lance when mounted)
		// Savage: 5 strength with 1 wound

		// Required trigger should fire - exert bearer to wound a minion
		assertEquals(0, scn.GetWoundsOn(rider));

		// Pass skirmish actions to resolve
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// All 3 savages should be valid wound targets
		assertTrue(scn.FreepsHasCardChoicesAvailable(savage1, savage2, savage3));
		assertEquals(3, scn.FreepsGetCardChoiceCount());

		// Choose to wound savage1 (the one Rider beat)
		scn.FreepsChooseCard(savage1);

		// Rider should now be exerted (cost of trigger)
		assertEquals(1, scn.GetWoundsOn(rider));
		// Savage1 should have 3 wounds total (1 from mount, 1 from skirmish, 1 from lance), so it is dead
		assertInDiscard(savage1);
	}

	@Test
	public void VanguardsLanceTriggerFizzlesIfBearerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var mount = scn.GetFreepsCard("mount");
		var rider = scn.GetFreepsCard("rider");

		var savage1 = scn.GetShadowCard("savage1");

		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, lance, mount);
		// Rider has 3 vitality - exhaust them (3 wounds = exhausted)
		scn.AddWoundsToChar(rider, 2);
		scn.MoveMinionsToTable(savage1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);
		scn.PassCurrentPhaseActions();

		// Rider wins, trigger tries to fire but cost (exert) cannot be paid
		// Since rider is exhausted (3 wounds = vitality), they cannot exert
		// Required trigger should fizzle - no wound choice presented

		assertTrue(scn.IsExhausted(rider));
		// Savage should only have mount + skirmish wound, no lance wound
		assertEquals(2, scn.GetWoundsOn(savage1));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void VanguardsLanceTriggerFizzlesIfBearerNotMounted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("lance");
		var rider = scn.GetFreepsCard("rider");
		scn.AttachCardsTo(rider, lance);
		var savage1 = scn.GetShadowCard("savage1");

		scn.MoveCompanionsToTable(rider);
		scn.MoveMinionsToTable(savage1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(rider, savage1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);
		scn.PassCurrentPhaseActions();

		// Rider wins, trigger tries to fire but cost (exert) cannot be paid
		// Since rider is exhausted (3 wounds = vitality), they cannot exert
		// Required trigger should fizzle - no wound choice presented

		// Savage should only have skirmish wound, no lance wound (or mount wound)
		assertEquals(1, scn.GetWoundsOn(savage1));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
