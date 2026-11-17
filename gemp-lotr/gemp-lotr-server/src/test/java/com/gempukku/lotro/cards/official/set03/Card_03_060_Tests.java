package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smith1", "3_60");
					put("smith2", "3_60");      // not unique: "this minion" is read beside another copy
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; his text answers only a weapon PLAYED on him
					put("banner", "6_98");      // Banner of the Eye: [sauron] hand weapon possession, bearer a [sauron] Orc; acts on a skirmish won

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, no archer; strength +3 against a Nazgul
					put("sam", "1_311");        // Sam, Son of Hamfast: the fifth companion; his abilities used only on purpose
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: Hobbit ally, no companion; a fellowship ability

					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon possession, bearer a Dwarf; acts on a skirmish lost to bearer
					put("armor", "1_8");        // Dwarven Armor: [dwarven] armor possession, no weapon, bearer a Dwarf; an overwhelm bar only
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] hand weapon ARTIFACT, bearer a [gondor] Man; a Response used only on purpose
					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon possession, bearer an Elf; makes bearer an archer
					put("sword", "1_299");      // Hobbit Sword: [shire] hand weapon possession, bearer a Hobbit; no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardSmithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Isengard Smith
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Exert this minion to discard a weapon (or all Free Peoples possessions if you can spot 5 companions).
		*/

		var scn = GetScenario();

		var smith = scn.GetFreepsCard("smith1");

		assertEquals("Isengard Smith", smith.getBlueprint().getTitle());
		assertNull(smith.getBlueprint().getSubtitle());
		assertFalse(smith.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, smith.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, smith.getBlueprint().getCulture());
		assertEquals(CardType.MINION, smith.getBlueprint().getCardType());
		assertEquals(Race.ORC, smith.getBlueprint().getRace());
		assertEquals(4, smith.getBlueprint().getTwilightCost());
		assertEquals(8, smith.getBlueprint().getStrength());
		assertEquals(3, smith.getBlueprint().getVitality());
		assertEquals(4, smith.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFourCompanionsItExertsThisMinionToDiscardOneWeaponOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var maggot = scn.GetFreepsCard("maggot");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		// Four companions with Frodo; Farmer Maggot is an ally, and nearly the fifth.
		scn.MoveCompanionsToTable(gimli, aragorn, arwen);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(gimli, axe, armor);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(maggot, sword);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(sword, maggot);

		var smith1 = scn.GetShadowCard("smith1");
		var smith2 = scn.GetShadowCard("smith2");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.MoveMinionsToTable(smith1, smith2, chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(banner, chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		// The bow goes on once the archery phase is behind: it makes Arwen an archer, whose wound would fall on a minion.
		scn.AttachCardsTo(arwen, bow);
		assertAttachedTo(bow, arwen);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(smith1));
		assertEquals(0, scn.GetWoundsOn(smith2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(smith1));
		scn.ShadowUseCardAction(smith1);
		// The Smith used exerts, and the copy beside it does not.
		assertEquals(1, scn.GetWoundsOn(smith1));
		assertEquals(0, scn.GetWoundsOn(smith2));

		// Exactly one weapon, whatever its side, class, item type or bearer; not a possession that is no weapon.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(5, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, narsil, bow, sword, banner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(narsil);

		assertInDiscard(narsil);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(bow, arwen);
		assertAttachedTo(sword, maggot);
		assertAttachedTo(banner, chieftain);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFiveCompanionsEveryFreePeoplesPossessionIsDiscardedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		var sword = scn.GetFreepsCard("sword");
		// Exactly five companions with Frodo, and the ally.
		scn.MoveCompanionsToTable(gimli, aragorn, arwen, sam);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(gimli, axe, armor);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(maggot, sword);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(sword, maggot);

		var smith1 = scn.GetShadowCard("smith1");
		var smith2 = scn.GetShadowCard("smith2");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.MoveMinionsToTable(smith1, smith2, chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(banner, chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		// The bow goes on once the archery phase is behind: it makes Arwen an archer, whose wound would fall on a minion.
		scn.AttachCardsTo(arwen, bow);
		assertAttachedTo(bow, arwen);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(smith1));
		assertEquals(0, scn.GetWoundsOn(smith2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowUseCardAction(smith1);
		assertEquals(1, scn.GetWoundsOn(smith1));
		assertEquals(0, scn.GetWoundsOn(smith2));

		// The parenthesis, available, is what happens: no weapon is chosen, and every Free Peoples possession goes,
		// the armor and the ally's sword among them.
		assertInDiscard(axe, armor, bow, sword);
		// The artifact and the Shadow possession stay.
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(banner, chieftain);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		assertAttachedTo(axe, gimli);

		var smith1 = scn.GetShadowCard("smith1");
		var smith2 = scn.GetShadowCard("smith2");

		scn.StartGame();
		scn.MoveMinionsToTable(smith1, smith2);
		// Vitality 3 with 2 wounds: exhausted, so it cannot exert; the copy beside it can.
		scn.AddWoundsToChar(smith1, 2);
		assertEquals(2, scn.GetWoundsOn(smith1));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(smith1));
		assertEquals(0, scn.GetWoundsOn(smith2));
		assertAttachedTo(axe, gimli);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(smith1));
		assertTrue(scn.ShadowActionAvailable(smith2));
	}

	@Test
	public void WithNoWeaponInPlayTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		assertAttachedTo(armor, gimli);

		var smith1 = scn.GetShadowCard("smith1");

		scn.StartGame();
		scn.MoveMinionsToTable(smith1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(smith1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// The cost can be paid, so the ability is offered though no weapon can be chosen.
		assertTrue(scn.ShadowActionAvailable(smith1));
		scn.ShadowUseCardAction(smith1);

		assertEquals(1, scn.GetWoundsOn(smith1));
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
