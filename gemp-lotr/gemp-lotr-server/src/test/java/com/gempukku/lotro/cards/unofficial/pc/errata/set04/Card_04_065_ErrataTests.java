package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_065_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erethon", "54_65");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion and archer, no requirement to play; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, given [elven] where a test says so
					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon, bearer must be an Elf, bearer is an archer
					put("gondorbow", "11_58");  // Bow of Minas Tirith: [gondor] ranged weapon, strength +1; a start-of-archery trigger at a battleground only
					put("sword", "4_64");       // Elven Sword: [elven] hand weapon, strength +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErethonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 4
		 * Name: Erethón, Naith Lieutenant
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
		 * While Erethón bears a ranged weapon, he is strength +1 and damage +1, and he does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erethon");

		assertEquals("Erethón", card.getBlueprint().getTitle());
		assertEquals("Naith Lieutenant", card.getBlueprint().getSubtitle());
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

		var erethon = scn.GetFreepsCard("erethon");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn is of the [elven] culture but no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));
		scn.MoveCardsToHand(erethon, legolas);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(erethon));
		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.FreepsPlayAvailable(erethon));
		scn.FreepsPlayCard(erethon);
		// 2 for Legolas + 2 for Erethón
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, erethon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyElfIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erethon = scn.GetFreepsCard("erethon");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(erethon);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(erethon));
		scn.FreepsPlayCard(erethon);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, erethon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearingARangedWeaponMakesHimStrengthPlusOneAndDamagePlusOneAndLeavesHimOutOfTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erethon = scn.GetFreepsCard("erethon");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(erethon, legolas);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(erethon));
		assertEquals(0, scn.GetKeywordCount(erethon, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(erethon, Keyword.ARCHER));
		// Legolas alone.
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(bow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(erethon, legolas));
		scn.FreepsChooseCard(erethon);
		assertAttachedTo(bow, erethon);

		// 6 + 1; the bow itself gives no strength.
		assertEquals(7, scn.GetStrength(erethon));
		assertEquals(1, scn.GetKeywordCount(erethon, Keyword.DAMAGE));
		// The bow makes him an archer, and he still does not add to the total: Legolas alone.
		assertTrue(scn.HasKeyword(erethon, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHandWeaponOrARangedWeaponOnAnotherCompanionDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erethon = scn.GetFreepsCard("erethon");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(erethon, legolas);
		scn.AttachCardsTo(erethon, sword);
		scn.AttachCardsTo(legolas, bow);
		// An archer by another card, so that the archery total can show whether he adds to it.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, erethon, null, Keyword.ARCHER));

		scn.StartGame();

		// 6 + 1 for the sword.
		assertEquals(7, scn.GetStrength(erethon));
		assertEquals(0, scn.GetKeywordCount(erethon, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(erethon, Keyword.ARCHER));
		// Legolas and Erethón.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		// Legolas bears the ranged weapon and gets none of it: the bow gives no strength of its own.
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARangedWeaponOfAnotherCultureThatGivesNoArcherKeywordSwitchesItOnToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erethon = scn.GetFreepsCard("erethon");
		var legolas = scn.GetFreepsCard("legolas");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		scn.MoveCompanionsToTable(erethon, legolas);
		// A [gondor] ranged weapon whose bearer must be a [gondor] Man: put on him by cheating.
		scn.AttachCardsTo(erethon, gondorbow);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, erethon, null, Keyword.ARCHER));

		scn.StartGame();

		assertAttachedTo(gondorbow, erethon);
		// 6 + 1 for the bow + 1
		assertEquals(8, scn.GetStrength(erethon));
		assertEquals(1, scn.GetKeywordCount(erethon, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(erethon, Keyword.ARCHER));
		// Legolas alone.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
