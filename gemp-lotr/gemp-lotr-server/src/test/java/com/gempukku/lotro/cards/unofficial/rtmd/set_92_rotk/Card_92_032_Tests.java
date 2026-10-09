package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_032_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion, Aragorn signet, vitality 3
					put("boromir", "1_96");    // Boromir, Lord of Gondor: Man companion, Aragorn signet, vitality 3
					put("merry", "1_303");     // Merry, From O'er the Brandywine: Hobbit companion, Aragorn signet, vitality 4
					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion, Gandalf signet, vitality 3
					put("legolas", "1_50");    // Legolas, Greenleaf: Elf companion, Frodo signet (the Ring-bearer's), vitality 3
					put("haldir", "1_48");     // Haldir, Elf of the Golden Wood: Elf companion with no signet, vitality 3
					put("lorienelf", "1_53");  // Lorien Elf: Elf companion with no signet, vitality 2
					put("bounder", "1_286");   // Bounder: Hobbit ally, vitality 2; able to exert, and no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_32", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion, Aragorn signet, vitality 3
					put("boromir", "1_96");    // Boromir, Lord of Gondor: Man companion, Aragorn signet, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_32"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_32
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Fellowship: Exert a companion to heal another companion with the same signet.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_32", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void ExertCompanionToHealAnotherWithSameSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(arwen, boromir, merry);
		// Wound boromir so he can be healed
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Choose companion to exert
		scn.FreepsChooseCard(arwen);

		// Choose companion to heal: Boromir and Merry have Arwen's signet, and Arwen is not "another"
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnyCompanionExertsAndOnlyAnotherCompanionOfHisSignetIsHealedOfOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(arwen, boromir, merry, gimli, legolas, haldir);
		scn.MoveAlliesToTable(bounder);
		// Everyone but Arwen is wounded, and so could be healed
		scn.AddWoundsToChar(boromir, 1);
		// Merry has vitality 4: two wounds, of which one is to be healed, and he can still exert
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bounder, 1);
		// Legolas has vitality 3: with 2 wounds he is exhausted and cannot exert
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();

		scn.FreepsUseCardAction(mod);
		// Any companion able to exert may pay, whatever his signet or want of one; the ally may not
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, boromir, merry, gimli, haldir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));

		// Arwen has the Aragorn signet: so have Boromir and Merry. Gimli has Gandalf's, Frodo and Legolas
		// have Frodo's, Haldir and the Bounder have none, and Arwen, wounded now, is not "another"
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, gimli, frodo, legolas, haldir, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(merry);

		// One wound of Merry's two is healed
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Whose signet it is depends on who exerts: with Frodo paying, the one other companion of his signet
		// is the exhausted Legolas (who can be healed though he cannot exert), and he is the one healed
		scn.FreepsUseCardAction(mod);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, boromir, merry, gimli, haldir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, bounder));
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotHealCompanionWithDifferentSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(arwen, gimli);
		// Wound gimli so he could theoretically be healed
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		// Arwen has Aragorn signet, Gimli has Gandalf signet — no valid heal target
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCompanionsWithNoSignetDoNotHaveTheSameSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");

		scn.MoveCompanionsToTable(haldir, lorienelf);
		scn.AddWoundsToChar(lorienelf, 1);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(haldir));
		scn.FreepsUseCardAction(mod);
		// Frodo could exert as well: Haldir is the one chosen
		scn.FreepsChooseCard(haldir);

		// Neither Elf has a signet, so the Lorien Elf is no companion "with the same signet": Haldir has
		// exerted for nothing
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileEveryCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(legolas);
		// An ally who could exert pays nothing here
		scn.MoveAlliesToTable(bounder);
		// Frodo (vitality 4) and Legolas (vitality 3) have the same signet and wounds to heal, and each is
		// exhausted
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentCanUse() throws DecisionResultInvalidException, CardNotFoundException {
		// The text has no "your": the modifier the other player holds is the Free Peoples player's to use.
		var scn = GetShadowScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(arwen, boromir);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));

		// Arwen exerts, and Boromir is the one other companion of her signet
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
