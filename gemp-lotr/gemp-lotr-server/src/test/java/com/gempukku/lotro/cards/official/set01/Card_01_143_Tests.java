package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop1", "1_143");
					put("troop2", "1_143");
					put("troop3", "1_143");

					// Bounder: [shire] Hobbit ally, strength 2, home site 2, so he may be assigned on turn 1; a skirmish
					// ability used only on purpose
					put("bounder", "1_286");
					put("sword", "1_299");        // Hobbit Sword: possession, hand weapon, strength +2, no other text
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: strength 7; a fellowship ability
					// Glamdring: possession, hand weapon, strength +2; "He is damage +1"; a fellowship/regroup ability
					put("glamdring", "1_75");
					// Wizard Staff: possession, a staff and no weapon, strength +1; a skirmish ability used only on purpose
					put("staff", "2_29");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8; a maneuver ability
					// Anduril, King's Blade: artifact, hand weapon, strength +2; defender +1 while no threat is spotted
					put("anduril", "7_80");
					// Bow of Minas Tirith: possession, ranged weapon, strength +1; an archery trigger at battlegrounds only
					put("bow", "11_58");
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
					// Dwarven Axe: possession, hand weapon, strength +2; a trigger when a minion loses to bearer
					put("axe", "1_9");
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: strength 6; +3 against a Nazgul only
					// Gwemegil: possession, hand weapon, strength +2; "She is damage +1. Skirmish: Exert Arwen or discard
					// 2 cards from hand to make her strength +1."
					put("gwemegil", "1_47");

					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TroopofUrukhaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Troop of Uruk-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>A character skirmishing this minion does not gain strength bonuses from weapons.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop1");

		assertEquals("Troop of Uruk-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CharactersSkirmishingATroopGainNoStrengthFromWeaponsAndKeepOtherBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(bounder, sword);
		scn.AttachCardsTo(gandalf, glamdring, staff);
		scn.AttachCardsTo(aragorn, anduril, bow);
		scn.AttachCardsTo(gimli, axe);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, troop3, patrol);

		// Turn 1 ends its fellowship phase with the move to site 2, Bounder's home: he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bounder, troop1},
				new PhysicalCardImpl[]{gandalf, troop2},
				new PhysicalCardImpl[]{aragorn, troop3},
				new PhysicalCardImpl[]{gimli, patrol});

		// Assigned to a Troop, and the skirmish not yet begun: every bonus in force.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		// 2 + 2 for the sword
		assertEquals(4, scn.GetStrength(bounder));
		// 7 + 2 for Glamdring + 1 for the staff
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		// 8 + 2 for Anduril + 1 for the bow
		assertEquals(11, scn.GetStrength(aragorn));
		// 6 + 2 for the axe
		assertEquals(8, scn.GetStrength(gimli));

		// Gimli's skirmish first: skirmishing a minion that is no Troop, he keeps his axe's +2, and the others,
		// waiting for their Troops, keep theirs.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(bounder));
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(11, scn.GetStrength(aragorn));
		scn.FreepsPass();
		scn.ShadowPass();

		// Skirmishing a Troop: Glamdring adds nothing; the staff, which is no weapon, still adds its +1, and the
		// damage bonus Glamdring gives is no strength bonus and stays.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(aragorn));
		scn.FreepsPass();
		scn.ShadowPass();

		// An artifact hand weapon and a ranged weapon add nothing either.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(bounder));
		scn.FreepsPass();
		scn.ShadowPass();

		// An ally is a character too.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AStrengthBonusAWeaponGivesByItsOwnAbilityIsNotGainedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		// 6 + 2 for Gwemegil
		assertEquals(8, scn.GetStrength(arwen));

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(1, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(arwen));

		assertTrue(scn.FreepsActionAvailable(gwemegil));
		// With no card in hand the exertion is the only cost that can be paid, and it is paid unasked.
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// The +1 comes from the weapon's own ability: not gained while she skirmishes the Troop.
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AStrengthBonusAWeaponGivesByItsOwnAbilityIsGainedAgainstAMinionThatIsNoTroop() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, gwemegil);

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, patrol);
		scn.FreepsResolveSkirmish(arwen);
		// 6 + 2 for Gwemegil
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(0, scn.GetWoundsOn(arwen));

		assertTrue(scn.FreepsActionAvailable(gwemegil));
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// The ability's +1 is real: against a minion that is no Troop it is gained.
		assertEquals(9, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
