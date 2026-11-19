package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimir", "11_10");
					// Farin, Dwarven Emissary: a [dwarven] Dwarf companion, strength 5 (+2 while skirmishing an Orc), vitality 3
					put("farin", "1_11");
					// Thrarin, Dwarven Smith: a [dwarven] Dwarf ally, strength 4, vitality 2; his maneuver ability lets
					// him take part in skirmishes
					put("thrarin", "1_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8; his ability is not used
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6; Knight is its whole text

					// The [dwarven] cards that are no characters, one of each type: a condition, a possession and an artifact
					put("mines", "3_3");        // Mines of Khazad-dum: support-area condition; it answers [dwarven] tales only
					put("armor", "1_8");        // Dwarven Armor: possession on a Dwarf; bearer is overwhelmed only by triple strength
					put("ring", "9_8");         // Ring of Guile: artifact on a Dwarf, strength +1 and damage +1; its ability is not used
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession with no text, borne by Frodo

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9; damage +1 is its whole text
					put("warrior", "4_165");    // Orthanc Warrior: strength 7, vitality 2; damage +1 is its whole text
					// Orkish Skirmisher: [orc] Orc, strength 9; each time he wins a skirmish, he may exert twice to
					// wound a companion
					put("skirmisher", "18_90");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Grimir, Dwarven Emissary
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Response:</b> If a [dwarven] companion is about to take a wound in a skirmish, discard 2 [dwarven] cards from play (except characters) to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimir");

		assertEquals("Grimir", card.getBlueprint().getTitle());
		assertEquals("Dwarven Emissary", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void TwoDwarvenCardsThatAreNoCharactersPreventTheFirstWoundAndTheOneLeftCannotPayForTheSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var mines = scn.GetFreepsCard("mines");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(grimir, farin);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(mines);
		scn.AttachCardsTo(farin, armor, ring);
		scn.AttachCardsTo(frodo, sword);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(armor, farin);
		assertAttachedTo(ring, farin);
		assertAttachedTo(sword, frodo);
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimir, raiders);
		scn.FreepsResolveSkirmish(grimir);

		// 9 against 7: the Raiding Party wins, and its damage +1 makes two wounds, each in a window of its own.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(grimir));
		scn.FreepsChooseOptionalTrigger(grimir);

		// A condition, a possession and an artifact of the culture are offered; the [dwarven] companions and ally
		// are characters, and the Hobbit Sword is no [dwarven] card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mines, armor, ring));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, farin, thrarin, sword, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(mines, ring);

		assertInDiscard(mines, ring);
		assertAttachedTo(armor, farin);

		// The first wound was prevented. The second finds one such card left: no offer, and it is taken.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertAttachedTo(armor, farin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsAWoundAnotherDwarvenCompanionTakesForLosingASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var farin = scn.GetFreepsCard("farin");
		var mines = scn.GetFreepsCard("mines");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(grimir, farin);
		scn.MoveCardsToSupportArea(mines);
		scn.AttachCardsTo(grimir, ring);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(ring, grimir);
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, troop);
		scn.FreepsResolveSkirmish(farin);

		// 13 against Farin's 7 (5, +2 against an Orc): one wound, and short of overwhelming him (14).
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(grimir));
		scn.FreepsChooseOptionalTrigger(grimir);

		assertInDiscard(mines, ring);
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsAWoundGrimirTakesInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var gknight = scn.GetFreepsCard("gknight");
		var mines = scn.GetFreepsCard("mines");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(grimir, gknight);
		scn.MoveCardsToSupportArea(mines);
		scn.AttachCardsTo(grimir, armor);

		var skirmisher = scn.GetShadowCard("skirmisher");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, troop);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(armor, grimir);
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gknight));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{grimir, troop}, new PhysicalCardImpl[]{gknight, skirmisher});

		// The Skirmisher, 9, beats the Gondorian Knight, 6, and exerts twice to wound Grimir, whose own skirmish
		// is still to come: a wound taken during a skirmish.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(grimir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(grimir));
		scn.FreepsChooseOptionalTrigger(grimir);

		assertInDiscard(mines, armor);
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedForAnArcheryWoundOnGrimir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var mines = scn.GetFreepsCard("mines");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(grimir);
		scn.MoveCardsToSupportArea(mines);
		scn.AttachCardsTo(grimir, ring);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(ring, grimir);
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.SkipToArcheryWounds();

		// The Marksman's archery wound, placed by the Free Peoples player on Grimir, with the cost payable.
		scn.FreepsChooseCard(grimir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimir));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(ring, grimir);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundOnACompanionOfAnotherCultureOrOnADwarvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var mines = scn.GetFreepsCard("mines");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(grimir, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(mines);
		scn.AttachCardsTo(grimir, ring);

		var troop = scn.GetShadowCard("troop");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, warrior);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(ring, grimir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// Thrarin's own ability lets him skirmish; it exerts him (vitality 2).
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{thrarin, warrior});

		// 13 against Aragorn's 8: one wound on a [gondor] companion.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, mines);

		// 7 against Thrarin's 4: a wound on a [dwarven] ally, which kills him.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimir));
		assertInZone(Zone.DEAD, thrarin);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(ring, grimir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneDwarvenCardInPlayThatIsNoCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var mines = scn.GetFreepsCard("mines");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(grimir, farin);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(mines);
		scn.AttachCardsTo(frodo, sword);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(sword, frodo);
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimir, troop);
		scn.FreepsResolveSkirmish(grimir);

		// 13 against 7: one wound. Only Mines of Khazad-dum could pay; the [dwarven] characters and the
		// [shire] Hobbit Sword cannot make up the second card.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(grimir));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertInZone(Zone.SUPPORT, mines);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
