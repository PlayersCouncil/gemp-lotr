package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alliance", "1_49");
					put("alliance2", "1_49");
					put("alliance3", "1_49");
					put("alliance4", "1_49");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, strength 6; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("eowyn", "4_271");      // Eowyn, Sister-daughter of Theoden: a [rohan] Man companion; a skirmish response only
					put("evenstar", "3_19");    // Gift of the Evenstar: another [elven] condition, borne by Aragorn (cheated on); a skirmish ability

					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an archery ability
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; an archery ability
					put("rumil", "1_57");       // Rumil, Elven Protector: Elf ally, cost 2; an archery ability
					put("lorien", "1_53");      // Lorien Elf: Elf companion, cost 1; to play, spot an Elf
					put("mallorn", "1_54");     // Mallorn-trees: an [elven] card that is no Elf, in the support area; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheLastAllianceofElvesandMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Last Alliance of Elves and Men
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Tale
		 * Game Text: <b>Tale</b>. Bearer must be a [gondor] Man. Limit 1 per bearer.<br>Bearer is strength +1 for each Elf you can spot (limit +3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alliance");

		assertEquals("The Last Alliance of Elves and Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAGondorManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var alliance = scn.GetFreepsCard("alliance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(alliance);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsPlayAvailable(alliance));
		scn.FreepsPlayCard(alliance);

		// The [gondor] Man companion and the [gondor] Man ally; not the [gondor] Hobbit, the [rohan] Man, the Elf or Frodo.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, eowyn, legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(alliance, merchant);
		// One Elf to spot, Legolas: 6 + 1.
		assertEquals(7, scn.GetStrength(merchant));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABearerOfOneIsNotOfferedAnotherAndWithEveryGondorManBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");
		var alliance2 = scn.GetFreepsCard("alliance2");
		var alliance3 = scn.GetFreepsCard("alliance3");
		var alliance4 = scn.GetFreepsCard("alliance4");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var evenstar = scn.GetFreepsCard("evenstar");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, evenstar);
		scn.AttachCardsTo(boromir, alliance);
		scn.MoveCardsToHand(alliance2, alliance3, alliance4);

		scn.StartGame();

		// Boromir bears one already; Aragorn bears another [elven] condition, which does not count, and the
		// Merchant bears nothing.
		scn.FreepsPlayCard(alliance2);
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(alliance2, aragorn);
		assertAttachedTo(evenstar, aragorn);

		// The Merchant is the one bearer left, and takes the third without a choice being put.
		scn.FreepsPlayCard(alliance3);
		assertAttachedTo(alliance3, merchant);

		// Every [gondor] Man bears one: the fourth cannot be played.
		assertFalse(scn.FreepsPlayAvailable(alliance4));
		assertInHand(alliance4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneForEachElfInPlayUpToThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var lorien = scn.GetFreepsCard("lorien");
		var rumil = scn.GetFreepsCard("rumil");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn, boromir, legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(mallorn);
		scn.MoveCardsToHand(alliance, lorien, rumil);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsPlayCard(alliance);
		scn.FreepsChooseCard(aragorn);
		// Two Elves, an Elf companion and an Elf ally: 8 + 2.  Mallorn-trees and the tale itself are [elven] cards
		// and no Elves (counted, they would make it 8 + 3).
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		// A third Elf: 8 + 3.
		scn.FreepsPlayCard(lorien);
		assertEquals(11, scn.GetStrength(aragorn));

		// A fourth Elf: still 8 + 3, the limit.
		scn.FreepsPlayCard(rumil);
		assertInPlay(rumil);
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElfInPlayBearerIsNotStrongerAndEachCopyGivesOnlyItsOwnBearerItsOwnThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");
		var alliance2 = scn.GetFreepsCard("alliance2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var lorien = scn.GetFreepsCard("lorien");
		var rumil = scn.GetFreepsCard("rumil");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, alliance);
		scn.AttachCardsTo(boromir, alliance2);

		scn.StartGame();

		// No Elf: each bearer at his printed strength.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.MoveCompanionsToTable(legolas, lorien);
		scn.MoveAlliesToTable(orophin, rumil);

		// Four Elves: each bearer +3 from his own copy, and nothing from the other's.
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(10, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
