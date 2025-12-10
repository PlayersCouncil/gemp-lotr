package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eilenach", "103_29");
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");

					put("aragorn", "1_89");      // Ranger, vitality 4
					put("knight", "8_39");       // Knight, vitality 3
					put("boromir", "1_97");      // Plain Man, vitality 3
					put("eowyn", "5_122");       // Valiant Man
					put("faramir", "4_116");     // Ring-bound Ranger
					put("brego", "13_63");       // Mount for Gondor companion

					put("runner", "1_178");      // Generic minion for skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Eilenach
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		* 	Skirmish: Hinder this beacon to make your unbound Man strength +1 for each of the following you can spot: ranger, knight, mounted Man, valiant Man, ring-bound Man, exhausted Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eilenach");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Eilenach", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Northern Signal-fire, Flame of Eilenach (103_29) Tests



//
// Extra Cost tests - requires hindering 2 beacons
//

	@Test
	public void EilenachCannotPlayWithoutTwoBeaconsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eilenach);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(eilenach));
	}

	@Test
	public void EilenachCanPlayByHinderingTwoBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eilenach);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(eilenach));

		scn.FreepsPlayCard(eilenach);
		scn.FreepsChooseCards(beacon1, beacon2);

		assertInZone(Zone.SUPPORT, eilenach);
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

//
// Skirmish ability tests - hinder self to pump unbound Man
//

	@Test
	public void EilenachSkirmishAbilityHindersSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		assertFalse(scn.IsHindered(eilenach));
		assertTrue(scn.FreepsActionAvailable(eilenach));

		scn.FreepsUseCardAction(eilenach);
		// Only one unbound Man in skirmish, auto-selected

		assertTrue(scn.IsHindered(eilenach));
	}

	@Test
	public void EilenachGivesPlus1ForRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var aragorn = scn.GetFreepsCard("aragorn"); // Ranger
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		int baseStrength = scn.GetStrength(aragorn);

		scn.FreepsUseCardAction(eilenach);

		// +1 for ranger
		assertEquals(baseStrength + 1, scn.GetStrength(aragorn));
	}

	@Test
	public void EilenachGivesPlus1ForKnight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var knight = scn.GetFreepsCard("knight");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner);
		scn.FreepsResolveSkirmish(knight);

		int baseStrength = scn.GetStrength(knight);

		scn.FreepsUseCardAction(eilenach);

		// +1 for knight
		assertEquals(baseStrength + 1, scn.GetStrength(knight));
	}

	@Test
	public void EilenachGivesPlus1ForMountedMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var boromir = scn.GetFreepsCard("boromir");
		var brego = scn.GetFreepsCard("brego");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, brego);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		int baseStrength = scn.GetStrength(boromir);

		scn.FreepsUseCardAction(eilenach);

		// +1 for mounted Man
		assertEquals(baseStrength + 1, scn.GetStrength(boromir));
	}

	@Test
	public void EilenachGivesPlus1ForValiantMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var eowyn = scn.GetFreepsCard("eowyn"); // Valiant
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, runner);
		scn.FreepsResolveSkirmish(eowyn);

		int baseStrength = scn.GetStrength(eowyn);

		scn.FreepsUseCardAction(eilenach);

		// +1 for valiant Man
		assertEquals(baseStrength + 1, scn.GetStrength(eowyn));
	}

	@Test
	public void EilenachGivesPlus1ForRingBoundManAndPlus1ForRanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var faramir = scn.GetFreepsCard("faramir"); // Ring-bound (and Ranger)
		var boromir = scn.GetFreepsCard("boromir"); // Plain Man to be the skirmisher
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(faramir, boromir);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		int baseStrength = scn.GetStrength(boromir);

		scn.FreepsUseCardAction(eilenach);

		// +1 for ring-bound Man, +1 for ranger (Faramir is both)
		assertEquals(baseStrength + 2, scn.GetStrength(boromir));
	}

	@Test
	public void EilenachGivesPlus1ForExhaustedMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var boromir = scn.GetFreepsCard("boromir"); // Vitality 3
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);
		scn.AddWoundsToChar(boromir, 2); // 2 wounds on 3 vitality = exhausted

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		int baseStrength = scn.GetStrength(boromir);

		scn.FreepsUseCardAction(eilenach);

		// +1 for exhausted Man
		assertEquals(baseStrength + 1, scn.GetStrength(boromir));
	}

	@Test
	public void EilenachCombinesMultipleBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var aragorn = scn.GetFreepsCard("aragorn"); // Ranger
		var knight = scn.GetFreepsCard("knight");   // Knight
		var eowyn = scn.GetFreepsCard("eowyn");     // Valiant
		var faramir = scn.GetFreepsCard("faramir"); // Ring-bound + Ranger (but ranger already counted)
		var brego = scn.GetFreepsCard("brego");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, knight, eowyn, faramir);
		scn.AttachCardsTo(aragorn, brego); // Aragorn is now mounted
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		int baseStrength = scn.GetStrength(aragorn);

		scn.FreepsUseCardAction(eilenach);
		// Multiple unbound Men, need to choose
		scn.FreepsChooseCard(aragorn);

		// +1 ranger (Aragorn or Faramir), +1 knight, +1 mounted (Aragorn), +1 valiant (Eowyn), +1 ring-bound (Faramir)
		// = +5 total
		assertEquals(baseStrength + 5, scn.GetStrength(aragorn));
	}

	@Test
	public void EilenachRangerBonusLimitedToPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		// Having 2 rangers should still only give +1
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var aragorn = scn.GetFreepsCard("aragorn"); // Ranger
		var faramir = scn.GetFreepsCard("faramir"); // Also Ranger (and ring-bound)
		var boromir = scn.GetFreepsCard("boromir"); // Plain Man to be the skirmisher
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, faramir, boromir);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		int baseStrength = scn.GetStrength(boromir);

		scn.FreepsUseCardAction(eilenach);
		scn.FreepsChooseCard(boromir);

		// +1 ranger (limited to 1 despite having 2 rangers), +1 ring-bound (Faramir)
		// = +2 total, NOT +3
		assertEquals(baseStrength + 2, scn.GetStrength(boromir));
	}

	@Test
	public void EilenachGivesNoBonusWithNoMatchingTraits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eilenach = scn.GetFreepsCard("eilenach");
		var boromir = scn.GetFreepsCard("boromir"); // Plain Man - no ranger, knight, mounted, valiant, ring-bound, exhausted
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(eilenach);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		int baseStrength = scn.GetStrength(boromir);

		scn.FreepsUseCardAction(eilenach);

		// No matching traits = +0
		assertEquals(baseStrength, scn.GetStrength(boromir));
	}
}
