package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sapling", "9_35");
					put("sapling2", "9_35");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, cost 2, vitality 3, not unique; Knight is its whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a Man who is [rohan]; Valiant is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] Man who is an ally, vitality 1; a maneuver ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally; a fellowship ability used only on purpose
					put("marksman1", "1_176");  // Goblin Marksman: strength 7; Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("assassin", "2_93");    // Tower Assassin: vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGondorRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sapling", "9_35");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, cost 4
					put("guard", "5_83");       // Household Guard: a Man who is [rohan]; Valiant is its whole text
					put("marksman1", "1_176");  // Goblin Marksman: strength 7; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				// Isildur, Bearer of Heirlooms: a [gondor] Man who can be the Ring-bearer, and so is Ring-bound.
				// His own text speaks only of his skirmishes and of knights; neither is on this table.
				"9_33",
				// The Great Ring answers no wound while it is not worn (The Ruling Ring has a response to its
				// bearer's wounds of its own, which would stand beside the Sapling's).
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void SaplingoftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Sapling of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot an unbound [gondor] Man.
		 * 	Response: If a [gondor] Man is about to take a wound, discard this artifact to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sapling");

		assertEquals("Sapling of the White Tree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUnboundGondorManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapling = scn.GetFreepsCard("sapling");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		// An unbound [gondor] companion who is no Man, an unbound Man who is not [gondor], and a [gondor] Man
		// who is an ally (only a companion can be unbound).
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sapling, knight);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(sapling));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(sapling));

		scn.FreepsPlayCard(sapling);
		assertInZone(Zone.SUPPORT, sapling);
		assertNotAttachedTo(sapling, knight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARingBearerWhoIsAGondorManIsNotUnboundButAragornPlayedBesideHimIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGondorRingBearerScenario();

		var sapling = scn.GetFreepsCard("sapling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(sapling, aragorn);

		scn.StartGame();
		assertEquals(Culture.GONDOR, scn.GetRingBearer().getBlueprint().getCulture());
		assertEquals(Race.MAN, scn.GetRingBearer().getBlueprint().getRace());
		assertFalse(scn.FreepsPlayAvailable(sapling));

		scn.FreepsPlayCard(aragorn);
		assertTrue(scn.FreepsPlayAvailable(sapling));

		scn.FreepsPlayCard(sapling);
		assertInZone(Zone.SUPPORT, sapling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedToPreventAWoundToAGondorManAndNotOfferedForAnyoneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapling = scn.GetFreepsCard("sapling");
		var sapling2 = scn.GetFreepsCard("sapling2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, knight, pippin, guard);
		scn.MoveCardsToSupportArea(sapling, sapling2);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");

		scn.SkipToArcheryWounds();
		assertEquals(3, scn.GetShadowArcheryTotal());

		// A [gondor] Hobbit and a [rohan] Man each take an arrow, and neither copy is offered.
		scn.FreepsChooseCard(pippin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sapling));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sapling2));
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.FreepsChooseCard(guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sapling));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sapling2));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, sapling);
		assertInZone(Zone.SUPPORT, sapling2);

		// The third arrow is for Aragorn: each copy offers itself.
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling2));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseOptionalTrigger(sapling);
		// The copy used is the one discarded; the other stays.
		assertInZone(Zone.DISCARD, sapling);
		assertInZone(Zone.SUPPORT, sapling2);
		// The arrow's wound is not placed: he still has the two he had.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DecliningLeavesTheWoundAndTheSaplingWhichMayAnswerALaterWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapling = scn.GetFreepsCard("sapling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(sapling);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable("marksman1", "marksman2");

		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, sapling);

		// The second arrow, for a Gondorian Knight: it is prevented.
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, sapling);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AGondorManWhoIsAnAllyIsSavedFromAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapling = scn.GetFreepsCard("sapling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var grimir = scn.GetFreepsCard("grimir");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant, grimir);
		scn.MoveCardsToSupportArea(sapling);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(assassin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(assassin));
		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(merchant, grimir));
		scn.ShadowChooseCard(merchant);

		// Gondorian Merchant has 1 vitality: this wound would kill him.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, sapling);
		assertInZone(Zone.SUPPORT, merchant);
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PreventsOnlyTheOneWoundWhenADamagePlus1MinionDealsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sapling = scn.GetFreepsCard("sapling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(sapling);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(raiders);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);
		// 8 against 9: Aragorn loses, and damage +1 makes it two wounds.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		// The first wound is prevented; the second is placed (preventing all his wounds would read 1, none 3).
		assertInZone(Zone.DISCARD, sapling);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARingBearerWhoIsAGondorManIsSavedFromAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGondorRingBearerScenario();

		var isildur = scn.GetRingBearer();
		var sapling = scn.GetFreepsCard("sapling");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(sapling);

		scn.StartGame();
		scn.AddWoundsToChar(isildur, 1);
		scn.MoveMinionsToTable("marksman1");

		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertFalse(scn.RBWearingOneRing());
		scn.FreepsChooseCard(isildur);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, sapling);
		// He keeps the one wound he had, and the arrow's is not placed.
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
