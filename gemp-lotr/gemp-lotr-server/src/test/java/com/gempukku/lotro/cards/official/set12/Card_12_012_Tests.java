package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_012_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("proud", "12_12");

					// Farin, Dwarven Emissary: Dwarf companion, no damage bonus of his own; "While skirmishing an Orc,
					// Farin is strength +2" (nobody skirmishes here).
					put("farin", "1_11");
					// Grimir, Dwarven Elder: Dwarf ally, no damage bonus; his fellowship ability is not used.
					put("grimir", "1_17");
					// Aragorn, Ranger of the North: a companion who is no Dwarf; his maneuver ability is not used.
					put("aragorn", "1_89");

					// [dwarven] possessions on "Bearer must be a Dwarf", no damage bonus among them.
					put("axe", "1_9");          // Dwarven Axe: hand weapon, cost 0; answers a minion losing to bearer
					put("armor", "1_8");        // Dwarven Armor: armor, cost 0; bearer not overwhelmed unless tripled
					put("handaxe", "2_10");     // Hand Axe: hand weapon borne beside another; an archery ability
					put("bracers", "12_5");     // Dwarven Bracers: bracers; a Response to a wound to bearer
					// Ring of Retribution: a [dwarven] ARTIFACT on a Dwarf, no damage bonus; its Response answers a win.
					put("ring", "9_9");
					// Elven Cloak: [elven] possession, "Bearer must be a companion"; the minion archery total -1.
					put("cloak", "1_42");
					// Stout and Strong: a [dwarven] condition, support area; its trigger answers a Dwarf winning.
					put("stout", "4_57");
					// Old Toby: [shire] pipeweed possession, support area, nothing needed to play it.
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ProudandAbleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Proud and Able
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each Dwarf is <b>damage +1</b> for each possession he bears.<br><b>Fellowship:</b> Discard this condition to play a [dwarven] possession from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("proud");

		assertEquals("Proud and Able", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfIsDamagePlusOneForEachPossessionHeBearsCountedAsTheyArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proud = scn.GetFreepsCard("proud");
		var farin = scn.GetFreepsCard("farin");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(farin);
		scn.AttachCardsTo(farin, axe);
		scn.MoveCardsToHand(proud, armor);

		scn.StartGame();

		// One possession, and no Proud and Able.
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));

		scn.FreepsPlayCard(proud);
		assertInZone(Zone.SUPPORT, proud);
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));

		// A second possession: damage +2.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, farin);
		assertEquals(2, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachDwarfCountsThePossessionsHeBearsAndNoOtherCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proud = scn.GetFreepsCard("proud");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var ring = scn.GetFreepsCard("ring");
		var handaxe = scn.GetFreepsCard("handaxe");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Farin: two possessions and an artifact. Grimir, an ally: one possession. Aragorn, no Dwarf: one possession.
		scn.AttachCardsTo(farin, axe, armor, ring);
		scn.AttachCardsTo(grimir, handaxe);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.MoveCardsToSupportArea(proud);

		// Each Dwarf by his own possessions, companion or ally; the artifact is no possession; Aragorn is no Dwarf.
		assertEquals(2, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsItToPlayADwarvenPossessionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proud = scn.GetFreepsCard("proud");
		var farin = scn.GetFreepsCard("farin");
		var axe = scn.GetFreepsCard("axe");
		var bracers = scn.GetFreepsCard("bracers");
		var ring = scn.GetFreepsCard("ring");
		var stout = scn.GetFreepsCard("stout");
		var toby = scn.GetFreepsCard("toby");
		var shadowHandaxe = scn.GetShadowCard("handaxe");
		scn.MoveCompanionsToTable(farin);
		scn.MoveCardsToSupportArea(proud);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe, bracers, ring, stout, toby, shadowHandaxe);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, proud);
		assertInDiscard(axe, bracers, ring, stout, toby, shadowHandaxe);
		assertTrue(scn.FreepsActionAvailable(proud));

		scn.FreepsUseCardAction(proud);
		assertInDiscard(proud);

		// [dwarven] possessions of two classes; not the [dwarven] artifact, nor the [dwarven] condition, nor the
		// [shire] possession, each of which could be played; nor the other player's Hand Axe in his own pile.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, bracers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, stout, toby, shadowHandaxe));
		scn.FreepsChooseCard(bracers);

		assertAttachedTo(bracers, farin);
		assertInDiscard(axe, ring, stout, toby, shadowHandaxe);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoDwarvenPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proud = scn.GetFreepsCard("proud");
		var farin = scn.GetFreepsCard("farin");
		var ring = scn.GetFreepsCard("ring");
		var stout = scn.GetFreepsCard("stout");
		var toby = scn.GetFreepsCard("toby");
		var shadowHandaxe = scn.GetShadowCard("handaxe");
		scn.MoveCompanionsToTable(farin);
		scn.MoveCardsToSupportArea(proud);

		scn.StartGame();
		scn.MoveCardsToDiscard(ring, stout, toby, shadowHandaxe);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, proud);
		assertInDiscard(ring, stout, toby, shadowHandaxe);
		assertFalse(scn.FreepsActionAvailable(proud));
	}

	@Test
	public void NotOfferedWhileTheDwarvenPossessionInTheDiscardPileHasNoBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var proud = scn.GetFreepsCard("proud");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(proud);

		scn.StartGame();
		scn.MoveCardsToDiscard(axe);
		scn.FreepsRefreshDecision();

		// The Dwarven Axe wants a Dwarf, and there is none: the play that is the cost cannot be made.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.SUPPORT, proud);
		assertInDiscard(axe);
		assertFalse(scn.FreepsActionAvailable(proud));
	}
}
