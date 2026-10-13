package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coat", "2_105");
					// Legolas, Greenleaf: an Elf archer companion; his archery ability is used only on purpose
					put("legolas", "1_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his maneuver ability is used only on purpose

					put("marksman", "1_176");   // Goblin Marksman: an archer and nothing else
					put("marksman2", "1_176");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("party2", "1_158");
					// Uruk-hai Sword: hand weapon, strength +2, on an Uruk-hai; a trigger on a skirmish won
					put("sword", "1_160");
					put("sword2", "1_160");
					// The Witch-king, Lord of Angmar: strength 14, fierce, +2 for each other Nazgul (none here)
					put("twk", "1_237");
					// The Pale Blade: hand weapon, strength +3, on The Witch-king, "He is damage +1"
					put("blade", "1_221");
					// Black Steed: a mount, no weapon, strength +2 and +2 more at a plains site (site 2 is plains)
					put("steed", "1_208");
					// Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3; his abilities are used only
					// on purpose
					put("ted", "18_75");
					// Long Battle Bow: ranged weapon, strength +1, on a [men] minion; makes bearer an archer
					put("bow", "11_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli is the Ring-bearer, so that the one Frodo who can stand beside him, Frodo, Frenzied Fighter (who can
	// never be the Ring-bearer), is the Frodo who bears the coat or not.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coat", "2_105");
					// Sam, Son of Hamfast: a [shire] Hobbit of another title; his abilities are used only on purpose
					put("sam", "1_311");
					// Frodo, Frenzied Fighter: a Frodo who is not the Ring-bearer; his skirmish ability is used only
					// on purpose
					put("frodo", "13_149");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithrilcoatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Mithril-coat
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Armor
		 * Game Text: Bearer must be Frodo.<br>The minion archery total is -1.<br>Each minion skirmishing Frodo does not gain strength bonuses from weapons and loses all <b>damage</b> bonuses.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coat");

		assertEquals("Mithril-coat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnlyOnAFrodoAndNotOnTheRingBearerOrAnotherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var coat = scn.GetFreepsCard("coat");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(coat);

		scn.StartGame();

		// Gimli the Ring-bearer and Sam, a [shire] Hobbit: no Frodo to bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(coat));

		scn.MoveCompanionsToTable(frodo);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(coat));

		// Frodo is the only one it may go on, so it goes on him with no choice asked.
		scn.FreepsPlayCard(coat);
		assertAttachedTo(coat, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheMinionArcheryTotalIsOneLowerAndTheFellowshipTotalIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(coat);

		var marksman = scn.GetShadowCard("marksman");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		// Laid early, as they will stand in the archery phase of this turn.
		scn.MoveMinionsToTable(marksman, marksman2);

		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(coat);
		assertAttachedTo(coat, frodo);

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MinionsSkirmishingFrodoLoseTheirWeaponsStrengthAndEveryDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, coat);

		var party = scn.GetShadowCard("party");
		var party2 = scn.GetShadowCard("party2");
		var sword = scn.GetShadowCard("sword");
		var sword2 = scn.GetShadowCard("sword2");
		var twk = scn.GetShadowCard("twk");
		var blade = scn.GetShadowCard("blade");
		var steed = scn.GetShadowCard("steed");
		var ted = scn.GetShadowCard("ted");
		var bow = scn.GetShadowCard("bow");

		scn.StartGame();
		scn.MoveMinionsToTable(party, party2, twk, ted);
		scn.AttachCardsTo(party, sword);
		scn.AttachCardsTo(party2, sword2);
		scn.AttachCardsTo(twk, blade, steed);
		// Ted's bow makes him an archer: 1 to the minion archery total, which the coat takes off again.
		scn.AttachCardsTo(ted, bow);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, party}, new PhysicalCardImpl[]{aragorn, party2});
		scn.ShadowAssignToMinions(frodo, twk, ted);

		// Assigned to Frodo, and his skirmish not yet begun: every bonus in force.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		// 9 + 2 for the sword
		assertEquals(11, scn.GetStrength(party));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		// 14 + 3 for the blade + 2 for the steed + 2 more for the steed at a plains site
		assertEquals(21, scn.GetStrength(twk));
		assertEquals(1, scn.GetKeywordCount(twk, Keyword.DAMAGE));
		// 3 + 1 for the bow
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(11, scn.GetStrength(party2));
		assertEquals(1, scn.GetKeywordCount(party2, Keyword.DAMAGE));

		// Aragorn's skirmish first: the minion skirmishing him keeps both, and so do the ones waiting for Frodo.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(party2));
		assertEquals(1, scn.GetKeywordCount(party2, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(party));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		scn.FreepsPass();
		scn.ShadowPass();
		// Aragorn loses to the sword's bearer: the wound he takes and the sword's trigger come at one moment.
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(frodo);

		// Skirmishing Frodo: the hand weapons and the ranged weapon add nothing, the printed damage bonus and
		// the one a weapon gives are gone, and the mount, which is no weapon, still counts.
		assertEquals(9, scn.GetStrength(party));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));
		// 14 + 2 + 2 for the steed
		assertEquals(18, scn.GetStrength(twk));
		assertEquals(0, scn.GetKeywordCount(twk, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
