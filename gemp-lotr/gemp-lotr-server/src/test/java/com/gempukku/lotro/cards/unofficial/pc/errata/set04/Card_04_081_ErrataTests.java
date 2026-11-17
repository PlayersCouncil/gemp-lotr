package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_081_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pengedhel", "54_81");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion and archer, no requirement to play; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, an archery ability used only on purpose
					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon, bearer must be an Elf, bearer is an archer
					put("longbow", "6_21");     // Naith Longbow: [elven] ranged weapon, strength +1, bearer must be an Elf companion; no archer keyword, an archery ability used only on purpose
					put("sword", "4_64");       // Elven Sword: [elven] hand weapon, strength +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PengedhelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Pengedhel, Naith Warrior
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant.
		 * To play, spot an Elf.
		 * While Pengedhel bears a ranged weapon, he is strength +2 and he does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pengedhel");

		assertEquals("Pengedhel", card.getBlueprint().getTitle());
		assertEquals("Naith Warrior", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndThenPlaysForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pengedhel = scn.GetFreepsCard("pengedhel");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(pengedhel, legolas);

		scn.StartGame();

		// Frodo, the one companion, is no Elf.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pengedhel));
		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(pengedhel));
		scn.FreepsPlayCard(pengedhel);
		assertInZone(Zone.FREE_CHARACTERS, pengedhel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pengedhel = scn.GetFreepsCard("pengedhel");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(pengedhel);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(pengedhel));
		scn.FreepsPlayCard(pengedhel);
		assertInZone(Zone.FREE_CHARACTERS, pengedhel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearingARangedWeaponMakesHimStrengthPlusTwoAndLeavesHimOutOfTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pengedhel = scn.GetFreepsCard("pengedhel");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(pengedhel, legolas);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(pengedhel));
		assertFalse(scn.HasKeyword(pengedhel, Keyword.ARCHER));
		// Legolas alone.
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(bow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pengedhel, legolas));
		scn.FreepsChooseCard(pengedhel);
		assertAttachedTo(bow, pengedhel);

		// 6 + 2; the bow itself gives no strength.
		assertEquals(8, scn.GetStrength(pengedhel));
		// The bow makes him an archer, and he still does not add to the total: Legolas alone.
		assertTrue(scn.HasKeyword(pengedhel, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHandWeaponOrARangedWeaponOnAnotherCompanionDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pengedhel = scn.GetFreepsCard("pengedhel");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(pengedhel, legolas);
		scn.AttachCardsTo(pengedhel, sword);
		scn.AttachCardsTo(legolas, bow);
		// An archer by another card, so that the archery total can show whether he adds to it.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, pengedhel, null, Keyword.ARCHER));

		scn.StartGame();

		// 6 + 1 for the sword.
		assertEquals(7, scn.GetStrength(pengedhel));
		assertTrue(scn.HasKeyword(pengedhel, Keyword.ARCHER));
		// Legolas and Pengedhel.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		// Legolas bears the ranged weapon and gets none of it: the bow gives no strength of its own.
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARangedWeaponThatGivesNoArcherKeywordSwitchesItOnToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pengedhel = scn.GetFreepsCard("pengedhel");
		var legolas = scn.GetFreepsCard("legolas");
		var longbow = scn.GetFreepsCard("longbow");
		scn.MoveCompanionsToTable(pengedhel, legolas);
		// Naith Longbow goes on an Elf companion and, unlike the Elven Bow, gives strength and no archer keyword.
		scn.AttachCardsTo(pengedhel, longbow);
		// An archer by another card, so that the archery total can show whether he adds to it.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, pengedhel, null, Keyword.ARCHER));

		scn.StartGame();

		assertAttachedTo(longbow, pengedhel);
		// 6 + 1 for the bow + 2
		assertEquals(9, scn.GetStrength(pengedhel));
		assertTrue(scn.HasKeyword(pengedhel, Keyword.ARCHER));
		// Legolas alone.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
