package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("moment", "13_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; a skirmish ability only
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5 (+2 skirmishing an Orc)
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf, strength 8
					// Wish For Our Kinfolk: [dwarven] support-area condition that adds [dwarven] tokens to itself by
					// its fellowship ability (used only on purpose)
					put("wish", "8_8");
					// Garrison of Osgiliath: [gondor] support-area condition that takes 2 [gondor] tokens as it is played
					put("garrison", "6_52");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AwkwardMomentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Awkward Moment
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +3 (or +4 and <b>damage +1</b> if you can spot a [dwarven] token).
		*/

		var scn = GetScenario();

		var moment = scn.GetFreepsCard("moment");

		assertEquals("Awkward Moment", moment.getBlueprint().getTitle());
		assertNull(moment.getBlueprint().getSubtitle());
		assertFalse(moment.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, moment.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, moment.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, moment.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(moment, Timeword.SKIRMISH));
		assertEquals(1, moment.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoDwarvenTokenItMakesADwarfStrengthPlusThreeForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moment = scn.GetFreepsCard("moment");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var garrison = scn.GetFreepsCard("garrison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Only [gondor] tokens can be spotted.
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(moment);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		// A Skirmish event: not at the assignment-phase decision.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(moment));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner1},
				new PhysicalCardImpl[]{farin, runner2});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(moment));
		scn.FreepsPlayCard(moment);
		// Any Dwarf: Gimli in this skirmish, Farin assigned and waiting, Grimir an ally in no skirmish; not
		// Aragorn or Frodo, who are no Dwarves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		// 6 + 3, and no damage bonus beside his printed one.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(3, scn.GetStrength(grimir));
		assertInZone(Zone.DISCARD, moment);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: Gimli is 6 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void WithADwarvenTokenItMakesTheDwarfStrengthPlusFourAndDamagePlusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moment = scn.GetFreepsCard("moment");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var wish = scn.GetFreepsCard("wish");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveAlliesToTable(grimir);
		// The [dwarven] token is on a condition, not on the Dwarf chosen.
		scn.MoveCardsToSupportArea(wish);
		scn.AddTokensToCard(wish, 1);
		scn.MoveCardsToHand(moment);

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(wish));
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner1},
				new PhysicalCardImpl[]{farin, runner2});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		scn.FreepsPlayCard(moment);
		scn.FreepsChooseCard(grimir);
		// 3 + 4, and damage +1, with nothing asked about the token.
		assertEquals(7, scn.GetStrength(grimir));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertInZone(Zone.DISCARD, moment);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in the next skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
	}

	@Test
	public void MayBePlayedWithNoDwarfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moment = scn.GetFreepsCard("moment");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(moment);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(moment));
		scn.FreepsPlayCard(moment);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, moment);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
