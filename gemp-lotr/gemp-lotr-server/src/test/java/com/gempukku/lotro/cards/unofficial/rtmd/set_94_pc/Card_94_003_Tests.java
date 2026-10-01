package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_003_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("gimli", "1_13"); // Gimli, Son of Glóin: twilight 2 companion, the only card type a starting fellowship normally allows
		put("sword", "1_299"); // Hobbit Sword: twilight 1 possession, bearer must be a Hobbit, so the Ring-bearer can carry it
		put("pony", "13_143"); // Bill the Pony (dearly-loved): twilight 2 possession, bearer must be Ring-bound Hobbit, to prove a nonzero item cost never touches the twilight pool
		put("pan", "3_108"); // Frying Pan: twilight 0 possession
		put("bill", "3_106"); // Bill the Pony: a second twilight 0 possession, for the one-per-card-type cap
		put("trolls", "3_114"); // Three Monstrous Trolls: twilight 0 support area condition, no bearer needed
		put("proudfoot", "1_301"); // Master Proudfoot: twilight 1 ally
		put("twofoot", "13_144"); // Daddy Twofoot: twilight 1 follower
		put("smeagol", "6_45"); // Sméagol: twilight 0 companion, to prove the 0-cost-per-type cap does not apply to companions
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_3", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_3"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_3
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: You may include items, conditions, allies, and followers in your starting fellowship. You cannot play more than 1 card with 0 twilight cost of each card type this way.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_3", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ItemsConditionsAlliesAndFollowersAreSelectable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var trolls = scn.GetFreepsCard("trolls");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var twofoot = scn.GetFreepsCard("twofoot");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, sword, trolls, proudfoot, twofoot));
	}

	@Test
	public void AnItemPlayedAtStartAttachesToTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseCard(sword);

		assertInPlay(sword);
		assertAttachedTo(sword, frodo);
		// During the starting-fellowship phase the twilight pool itself is the running budget total.
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void ConditionAllyAndFollowerPlayAtStartAndSpendTheBudget() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var trolls = scn.GetFreepsCard("trolls");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var twofoot = scn.GetFreepsCard("twofoot");

		scn.FreepsChooseCard(trolls);
		scn.FreepsChooseCard(proudfoot);
		scn.FreepsChooseCard(twofoot);

		assertInPlay(trolls, proudfoot, twofoot);
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void PoolIsZeroAtStartOfTurnOneAfterACostTwoItemAtStart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var pony = scn.GetFreepsCard("pony");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();

		scn.FreepsChooseCard(pony);
		scn.FreepsDeclineOptionalTrigger(); // "you may reinforce a [shire] token" — irrelevant to this test

		assertInPlay(pony);
		assertAttachedTo(pony, frodo);
		// The pool is the running budget total during the starting-fellowship phase itself.
		assertEquals(2, scn.GetTwilight());

		// 2 of the 4 budget spent so far: a further cost-2 companion exactly fits...
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(4, scn.GetTwilight());

		// ...and now the whole budget (2 + 2) is spent, so even a cost-1 item no longer fits.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));

		// Finish starting fellowship setup for both players and reach P1's first turn.
		scn.FreepsChoose("");
		scn.ShadowChoose("");
		scn.StartGame(true, false);

		// GameState.startPlayerTurn() zeroes the pool for every real turn, including the first one,
		// so none of the starting-fellowship "budget" survives into P1's actual Fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void TwilightBudgetStillCapsTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var twofoot = scn.GetFreepsCard("twofoot");
		var trolls = scn.GetFreepsCard("trolls");

		scn.FreepsChooseCard(sword);
		scn.FreepsChooseCard(proudfoot);
		scn.FreepsChooseCard(twofoot);

		// 3 of the 4 twilight budget spent: the 2-cost companion no longer fits, the 0-cost condition does.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(trolls));
	}

	@Test
	public void OnlyOneZeroCostCardOfEachCardTypeMayGoDown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var pan = scn.GetFreepsCard("pan");
		var bill = scn.GetFreepsCard("bill");
		var trolls = scn.GetFreepsCard("trolls");
		var smeagol = scn.GetFreepsCard("smeagol");

		assertTrue(scn.FreepsHasCardChoiceAvailable(pan, bill, trolls));
		scn.FreepsChooseCard(pan);

		// The cap is per card type: no second 0-cost possession, but the 0-cost condition is untouched
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bill));
		assertTrue(scn.FreepsHasCardChoiceAvailable(trolls));
		// The cap only applies to the extra card types this mod grants; a 0-cost companion is unaffected.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol));
	}

	@Test
	public void WithoutTheModOnlyCompanionsAreSelectable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var trolls = scn.GetFreepsCard("trolls");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var twofoot = scn.GetFreepsCard("twofoot");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, trolls, proudfoot, twofoot));
	}

	@Test
	public void ScopingOnlyTheOwnerGetsTheExtraCardTypes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var nonOwnerGimli = scn.GetFreepsCard("gimli");
		var nonOwnerSword = scn.GetFreepsCard("sword");
		var nonOwnerTrolls = scn.GetFreepsCard("trolls");
		var ownerSword = scn.GetShadowCard("sword");
		var ownerTrolls = scn.GetShadowCard("trolls");
		var ownerProudfoot = scn.GetShadowCard("proudfoot");
		var ownerTwofoot = scn.GetShadowCard("twofoot");

		// The non-owner is prompted first and sees companions only
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(nonOwnerGimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nonOwnerSword, nonOwnerTrolls));
		scn.FreepsChoose("");

		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(ownerSword, ownerTrolls, ownerProudfoot, ownerTwofoot));
	}
}
