package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "3_96");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6; a skirmish ability never used here
					put("armor", "1_8");        // Dwarven Armor: Free Peoples possession; about being overwhelmed only, changes no number
					put("ring", "17_4");        // Ring of Artifice: Free Peoples artifact on a Dwarf; damage +1, and strength +1 only for a [dwarven] artifact in the discard pile (none here)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "3_96");
					put("pillager2", "3_96");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability, never used here

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability, never used here
					put("sword", "1_112");      // Ranger's Sword: Free Peoples possession on Aragorn, strength +2; damage +1
					put("saga", "1_114");       // The Saga of Elendil: Free Peoples condition on a [gondor] companion; a skirmish ability, never used here
					put("evil", "1_283");       // You Bring Great Evil: Shadow condition on a companion; acts only at a sanctuary
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6; a skirmish ability, never used here
					put("armor", "1_8");        // Dwarven Armor: Free Peoples possession on a Dwarf; about being overwhelmed only, changes no number
					put("sting", "1_313");      // Sting: Free Peoples possession on Frodo, strength +2; a fellowship or regroup ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4; his maneuver ability lets him skirmish
					put("heart", "1_10");       // Dwarven Heart: Free Peoples condition on a Dwarf; a start-of-turn exertion, laid after the turn has started
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: For each Free Peoples card borne by a character this minion is skirmishing, that character is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillager");

		assertEquals("Orc Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AHinderedFreePeoplesCardBorneByTheCharacterItSkirmishesIsCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		// A possession and an artifact, items of two classes, on one character.
		scn.AttachCardsTo(gimli, armor, ring);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);

		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertFalse(scn.IsHindered(armor));

		scn.SkipToAssignments();
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsAssignAndResolve(gimli, pillager);

		// 6 - 2: a possession and an artifact are both Free Peoples cards.
		assertEquals(4, scn.GetStrength(gimli));

		// A card that is hindered is still borne, and "a Free Peoples card" with nothing more said of it
		// reaches a hindered card.
		scn.HinderCard(armor);
		assertTrue(scn.IsHindered(armor));
		assertAttachedTo(armor, gimli);
		// 6 - 2
		assertEquals(4, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionSkirmishingHimIsStrengthMinusOneForEachFreePeoplesCardHeBearsAndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, sword, saga);
		scn.AttachCardsTo(gimli, armor);

		var pillager = scn.GetShadowCard("pillager");
		var soldier = scn.GetShadowCard("soldier");
		var evil = scn.GetShadowCard("evil");

		scn.StartGame();
		scn.MoveMinionsToTable(pillager, soldier);
		scn.AttachCardsTo(aragorn, evil);

		assertAttachedTo(sword, aragorn);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(evil, aragorn);
		assertAttachedTo(armor, gimli);
		// 8 + 2 for Ranger's Sword
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pillager}, new PhysicalCardImpl[]{gimli, soldier});

		// Gimli's skirmish first.  He bears a Free Peoples card and is skirmishing, but another minion; Aragorn is
		// assigned to the Pillager and not yet skirmishing him.  Neither is made weaker.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(10, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Aragorn's skirmish: two Free Peoples cards borne, a possession and a condition; the Shadow condition he
		// bears is not counted.  10 - 2.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachCopyWeakensOnlyTheCharacterItSkirmishesAndTheOneRingIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sting = scn.GetFreepsCard("sting");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(gimli, armor);

		var pillager1 = scn.GetShadowCard("pillager");
		var pillager2 = scn.GetShadowCard("pillager2");

		scn.StartGame();
		scn.MoveMinionsToTable(pillager1, pillager2);

		assertAttachedTo(ring, frodo);
		assertAttachedTo(sting, frodo);
		assertAttachedTo(armor, gimli);
		// 3 + 1 for The Ruling Ring + 2 for Sting
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, pillager1}, new PhysicalCardImpl[]{gimli, pillager2});
		scn.FreepsResolveSkirmish(frodo);

		// Only Sting is a Free Peoples card: The One Ring is neither Free Peoples nor Shadow (CR 4.2 "kinds of
		// cards").  6 - 1, from the copy he skirmishes alone: the other copy, waiting for Gimli, takes nothing
		// off him, and Gimli is not yet skirmishing.
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingHimIsStrengthMinusOneForEachFreePeoplesCardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveAlliesToTable(thrarin);

		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		// Laid after the start of the turn, which would have Dwarven Heart exert him.
		scn.AttachCardsTo(thrarin, armor, heart);
		scn.MoveMinionsToTable(pillager);

		assertAttachedTo(armor, thrarin);
		assertAttachedTo(heart, thrarin);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(4, scn.GetStrength(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, pillager);
		scn.FreepsResolveSkirmish(thrarin);

		// A possession and a condition: 4 - 2.
		assertEquals(2, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
