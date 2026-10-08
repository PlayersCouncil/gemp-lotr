package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_080_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ordulus", "54_80");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion and archer, no requirement to play; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, given [elven] where a test says so
					put("arwensbow", "18_4");   // Arwen's Bow: unique [elven] ranged weapon, cost 2, strength +1; bearer must be an [elven] companion; an archery ability for Arwen only
					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon, cost 1, bearer must be an Elf, bearer is an archer
					put("sword", "4_64");       // Elven Sword: [elven] hand weapon, cost 1, strength +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrdulusStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Ordulus, Young Warrior
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: Valiant.
		 * To play, spot an Elf.
		 * The twilight cost of each ranged weapon played on Ordulus is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ordulus");

		assertEquals("Ordulus", card.getBlueprint().getTitle());
		assertEquals("Young Warrior", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndThenPlaysForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ordulus = scn.GetFreepsCard("ordulus");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn is of the [elven] culture but no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));
		scn.MoveCardsToHand(ordulus, legolas);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(ordulus));
		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(ordulus));
		scn.FreepsPlayCard(ordulus);
		// 2 for Legolas + 1 for Ordulus
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, ordulus);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ordulus = scn.GetFreepsCard("ordulus");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(ordulus);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(ordulus));
		scn.FreepsPlayCard(ordulus);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, ordulus);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARangedWeaponPlayedOnHimCostsOneLessAndOnAnotherElfCostsInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ordulus = scn.GetFreepsCard("ordulus");
		var legolas = scn.GetFreepsCard("legolas");
		var arwensbow = scn.GetFreepsCard("arwensbow");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(ordulus, legolas);
		scn.MoveCardsToHand(arwensbow, bow);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(arwensbow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ordulus, legolas));
		scn.FreepsChooseCard(ordulus);
		assertAttachedTo(arwensbow, ordulus);
		// 2 - 1
		assertEquals(1, scn.GetTwilight());

		// Ordulus now bears a ranged weapon, so the Elven Bow can only go on Legolas.
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, legolas);
		// 1 + 1 for the Elven Bow in full
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACostOneRangedWeaponIsFreeOnHimAndAHandWeaponCostsInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ordulus = scn.GetFreepsCard("ordulus");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(ordulus);
		scn.MoveCardsToHand(bow, sword);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, ordulus);
		// 3 + 1 for the Elven Sword in full
		assertEquals(4, scn.GetTwilight());

		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, ordulus);
		// 4 + (1 - 1)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARangedWeaponTransferredToHimCostsInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ordulus = scn.GetFreepsCard("ordulus");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(ordulus, legolas);
		scn.AttachCardsTo(legolas, bow);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(bow));
		scn.FreepsTransferCard(bow);
		assertAttachedTo(bow, ordulus);
		// A transfer is not a play: the Elven Bow's 1 in full.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
