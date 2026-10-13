package com.gempukku.lotro.cards.unofficial.pc.errata.set06;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_06_023_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warband", "56_23");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: unique Elf companion; strength +2 only at sites 6 to 8
					put("elf1", "1_53");        // Lórien Elf: Elf companion, not unique, cost 1, "To play, spot an Elf."
					put("elf2", "1_53");
					put("orophin", "1_56");     // Orophin, Lórien Bowman: Elf ally (an archery ability)
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: [gondor] Man companion, Defender +1 and no other text: a companion who is no Elf
					put("bow", "1_41");         // Elven Bow: ranged weapon, bearer must be an Elf, bearer is an archer
					put("bow2", "4_62");        // Elven Bow: the same text, printed again
					put("longbow", "6_21");     // Naith Longbow: ranged weapon, strength +1, makes no archer; an archery ability
					put("sword", "4_64");       // Elven Sword: hand weapon, strength +1; a skirmish ability

					put("troll", "1_165");      // Cave Troll of Moria: strength 15, vitality 4, damage +1, fierce
					put("marksman1", "1_176");  // Goblin Marksman: archer, strength 7, vitality 1, no other text
					put("marksman2", "1_176");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NaithWarbandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Naith Warband
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	To play, spot 3 Elf companions. While Naith Warband bears a ranged weapon, it takes no more than 1 wound during each skirmish phase and does not add to the fellowship archery total.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warband");

		assertEquals("Naith Warband", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnlyOnceThreeElfCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warband = scn.GetFreepsCard("warband");
		var haldir = scn.GetFreepsCard("haldir");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(haldir, elf1, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(warband, elf2);

		scn.StartGame();

		// Two Elf companions (one unique, one not), an Elf ally and a Man companion: not 3 Elf companions.
		assertFalse(scn.FreepsPlayAvailable(warband));

		scn.FreepsPlayCard(elf2);
		assertTrue(scn.FreepsPlayAvailable(warband));

		scn.FreepsPlayCard(warband);
		assertInZone(Zone.FREE_CHARACTERS, warband);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearingAnElvenBowHeTakesOneWoundInEachSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warband = scn.GetFreepsCard("warband");
		var bow = scn.GetFreepsCard("bow");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(warband);
		scn.AttachCardsTo(warband, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warband, troll);
		scn.FreepsResolveSkirmish(warband);
		assertEquals(0, scn.GetWoundsOn(warband));

		// Strength 15 against 9: the troll wins, and its damage +1 would make 2 wounds.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(warband));

		// The fierce skirmish is a skirmish phase of its own: one more wound, of 2.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(warband, troll);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(warband));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAHandWeaponAndABowOnAnotherElfHeTakesBothWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warband = scn.GetFreepsCard("warband");
		var sword = scn.GetFreepsCard("sword");
		var elf1 = scn.GetFreepsCard("elf1");
		var bow2 = scn.GetFreepsCard("bow2");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(warband, elf1);
		scn.AttachCardsTo(warband, sword);
		scn.AttachCardsTo(elf1, bow2);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warband, troll);
		scn.FreepsResolveSkirmish(warband);
		assertEquals(0, scn.GetWoundsOn(warband));

		// Strength 15 against 10.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(warband));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void InTheArcheryPhaseHeTakesEveryWoundAndAddsNothingToTheFellowshipArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warband = scn.GetFreepsCard("warband");
		var bow = scn.GetFreepsCard("bow");
		var elf1 = scn.GetFreepsCard("elf1");
		var bow2 = scn.GetFreepsCard("bow2");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(warband, elf1);
		scn.AttachCardsTo(warband, bow);
		scn.AttachCardsTo(elf1, bow2);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);

		scn.SkipToPhase(Phase.ARCHERY);
		// Both Elves are archers; only the Lórien Elf adds to the fellowship archery total.
		assertTrue(scn.HasKeyword(warband, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(elf1, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(warband));
		scn.FreepsChooseCard(warband);
		scn.FreepsChooseCard(warband);
		// The archery phase is not a skirmish phase: both wounds land.
		assertEquals(2, scn.GetWoundsOn(warband));

		scn.ShadowChooseCard(marksman1);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PlayingANaithLongbowOnHimTakesHimOutOfTheArcheryTotalAndLimitsHisSkirmishWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warband = scn.GetFreepsCard("warband");
		var longbow = scn.GetFreepsCard("longbow");
		var elf1 = scn.GetFreepsCard("elf1");
		var bow2 = scn.GetFreepsCard("bow2");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(warband, elf1);
		scn.AttachCardsTo(elf1, bow2);
		scn.MoveCardsToHand(longbow);

		scn.StartGame();
		// An archer with no ranged weapon, so that the Naith Longbow, which makes no archer, is what changes.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, warband, null, Keyword.ARCHER));
		scn.MoveMinionsToTable(troll);

		assertEquals(2, scn.GetFreepsArcheryTotal());
		scn.FreepsPlayCard(longbow);
		assertEquals(warband, longbow.getAttachedTo());
		assertTrue(scn.HasKeyword(warband, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warband, troll);
		scn.FreepsResolveSkirmish(warband);
		assertEquals(0, scn.GetWoundsOn(warband));

		// Strength 15 against 10: 2 wounds for the damage +1, of which he takes 1.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(warband));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhileHeBearsABowAnotherElfStillTakesEveryWoundInASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warband = scn.GetFreepsCard("warband");
		var bow = scn.GetFreepsCard("bow");
		var haldir = scn.GetFreepsCard("haldir");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(warband, haldir);
		scn.AttachCardsTo(warband, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(party);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haldir, party);
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(0, scn.GetWoundsOn(haldir));

		// Strength 9 against 6 (Haldir's bonus is for sites 6 to 8): 2 wounds for the damage +1, and the cap is Naith Warband's alone.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(warband));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
