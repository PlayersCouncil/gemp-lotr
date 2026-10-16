package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("choice", "4_123");
					// Aragorn, Ranger of the North: [gondor] Man with the Gandalf signet, vitality 4; a maneuver ability
					// used only on purpose
					put("aragorn", "1_89");
					// Aragorn, Defender of Rohan: the one Aragorn who is no [gondor] card ([rohan]), and no signet; his
					// text wants the Ring-bearer assigned
					put("defender", "17_93");
					// Arwen, Daughter of Elrond: [elven] Elf, Aragorn signet, vitality 3; +3 only against a Nazgul
					put("arwen", "1_30");
					// Merry, From O'er the Brandywine: [shire] Hobbit, Aragorn signet, vitality 4; +2 only with a weapon
					put("merry", "1_303");
					// Boromir, Lord of Gondor: [gondor] Man who is no Aragorn, Aragorn signet, vitality 3
					put("boromir", "1_96");
					// Gimli, Son of Gloin: Gandalf signet; a skirmish ability used only on purpose
					put("gimli", "1_13");
					// Haldir, Elf of the Golden Wood: no signet; "To play, spot an Elf", so he is cheated in
					put("haldir", "1_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HardChoiceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hard Choice
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot Aragorn to heal a companion who has the Aragorn signet twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("choice");

		assertEquals("Hard Choice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAragornHealsOneCompanionWithTheAragornSignetTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, arwen, merry, gimli, haldir);
		scn.MoveCardsToHand(choice);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(merry, 3);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(haldir, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(haldir));

		assertTrue(scn.FreepsPlayAvailable(choice));
		scn.FreepsPlayCard(choice);

		// The [elven] and the [shire] companion with the Aragorn signet; not Aragorn himself or Gimli, who have
		// the Gandalf signet, nor Haldir, who has none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, haldir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		// Twice: 3 - 2 = 1, and nobody else.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertInDiscard(choice);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRohanAragornIsSpottedAndAGondorCompanionWithTheSignetIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var defender = scn.GetFreepsCard("defender");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(defender, boromir);
		scn.MoveCardsToHand(choice);

		scn.StartGame();
		scn.AddWoundsToChar(defender, 1);
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(2, scn.GetWoundsOn(boromir));

		assertTrue(scn.FreepsPlayAvailable(choice));
		scn.FreepsPlayCard(choice);

		// Boromir is the one wounded companion with the Aragorn signet and is healed unasked; this Aragorn has no
		// signet and keeps his wound.
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(defender));
		assertInDiscard(choice);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var choice = scn.GetFreepsCard("choice");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(boromir, arwen);
		scn.MoveCardsToHand(choice);

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(arwen, 1);

		// Boromir is a [gondor] companion who is no Aragorn; both he and Arwen have the signet and a wound.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.HAND, choice);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(choice));
	}
}
