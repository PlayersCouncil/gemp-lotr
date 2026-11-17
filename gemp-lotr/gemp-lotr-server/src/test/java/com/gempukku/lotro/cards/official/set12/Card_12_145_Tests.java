package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_145_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shingle", "12_145");
					put("shingle2", "12_145");

					// Intimidating Uruk: [uruk-hai] Uruk-hai, strength 10, printed damage +1; its text answers only its play
					// at a battleground, and it is cheated in.
					put("intimidating", "11_189");
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard, no damage bonus.  Each companion is
					// resistance -1; a skirmish ability, never used.
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, printed damage +1 and no other text.
					put("guard", "10_81");

					// Aragorn, Ranger of the North: a [gondor] Man, resistance 6; a maneuver ability, never used.
					put("aragorn", "1_89");
					// Farin, Emissary of Erebor: a [dwarven] Dwarf, resistance 6.  Muster at a battleground or underground
					// site only, and none is reached.
					put("farin", "11_7");
					// Gimli, Lively Combatant: a [dwarven] Dwarf, resistance 7; draws when he wins a skirmish, and none is
					// fought.
					put("gimli", "11_8");
					// Bounder: a Hobbit ally (resistance 0, as every ally has).  A skirmish ability, never used.
					put("bounder", "1_286");
					// Boromir, Lord of Gondor and Arwen, Daughter of Elrond: companions whose text reaches no test here.
					put("boromir", "1_96");
					put("arwen", "1_30");
					// Sam, Son of Hamfast: a companion of cost 2 with no requirement to play.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShingleinaStormStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Shingle in a Storm
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 6 companions, each [uruk-hai] minion is <b>damage +1</b>.<br><b>Assignment:</b> Discard this condition to assign an [uruk-hai] minion to a companion who has resistance 2 or less.
		*/

		var scn = GetScenario();

		var shingle = scn.GetFreepsCard("shingle");

		assertEquals("Shingle in a Storm", shingle.getBlueprint().getTitle());
		assertNull(shingle.getBlueprint().getSubtitle());
		assertFalse(shingle.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shingle.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, shingle.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, shingle.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(shingle, Keyword.SUPPORT_AREA));
		assertEquals(0, shingle.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUrukHaiMinionIsDamagePlus1OnceASixthCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gimli"), scn.GetFreepsCard("arwen"));
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sam);
		assertInHand(sam);

		var shingle = scn.GetShadowCard("shingle");
		var intimidating = scn.GetShadowCard("intimidating");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(shingle);

		scn.StartGame();
		// Laid early in the turn for the reading (Q-0228).
		scn.MoveMinionsToTable(intimidating, saruman, guard);
		scn.FreepsRefreshDecision();

		// Frodo and four companions, and Bounder, an ally: five companions.  Each minion at its printed bonus.
		assertInZone(Zone.SUPPORT, shingle);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetKeywordCount(intimidating, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(shingle, Keyword.DAMAGE));

		scn.FreepsPlayCard(sam);

		// The sixth companion: each [uruk-hai] minion, Uruk-hai or Wizard, +1; the [sauron] Uruk-hai is not.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(2, scn.GetKeywordCount(intimidating, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// Nor is this [uruk-hai] card, which is no minion.
		assertEquals(0, scn.GetKeywordCount(shingle, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AssignmentAbilityDiscardsItToAssignAnUrukHaiMinionToACompanionOfResistance2OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, farin, gimli);
		scn.MoveAlliesToTable(bounder);

		var shingle = scn.GetShadowCard("shingle");
		var shingle2 = scn.GetShadowCard("shingle2");
		var intimidating = scn.GetShadowCard("intimidating");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(shingle, shingle2);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(intimidating, saruman, guard);
		// 6, 6, 7 and 10, each less 3 burdens and 1 for Saruman; an ally has resistance 0.
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(2, scn.GetResistance(farin));
		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(bounder));
		assertInZone(Zone.SUPPORT, shingle);
		assertInZone(Zone.SUPPORT, shingle2);

		// The Shadow player's archery actions, the phase before: not offered.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shingle));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(shingle));
		scn.ShadowUseCardAction(shingle);

		// The cost: this copy, and not the other.
		assertInZone(Zone.DISCARD, shingle);
		assertInZone(Zone.SUPPORT, shingle2);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, farin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, frodo, bounder));
		scn.ShadowChooseCard(farin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(intimidating, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		scn.ShadowChooseCard(intimidating);

		assertTrue(scn.IsCharAssignedAgainst(farin, intimidating));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionOfResistance2OrLessItIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);

		var shingle = scn.GetShadowCard("shingle");
		var intimidating = scn.GetShadowCard("intimidating");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(shingle);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(intimidating, saruman);
		// Each less 2 burdens and 1 for Saruman: Aragorn one over the bound.
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(7, scn.GetResistance(frodo));
		assertInZone(Zone.SUPPORT, shingle);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// The cost can be paid, so it is offered.
		assertTrue(scn.ShadowActionAvailable(shingle));
		scn.ShadowUseCardAction(shingle);

		assertInZone(Zone.DISCARD, shingle);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(intimidating));
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionItIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shingle = scn.GetShadowCard("shingle");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(shingle);

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(guard);
		assertEquals(2, scn.GetResistance(aragorn));
		assertInZone(Zone.SUPPORT, shingle);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(shingle));
		scn.ShadowUseCardAction(shingle);

		// Only the [sauron] Uruk-hai is on the table: nobody is assigned.
		assertInZone(Zone.DISCARD, shingle);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(guard));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
