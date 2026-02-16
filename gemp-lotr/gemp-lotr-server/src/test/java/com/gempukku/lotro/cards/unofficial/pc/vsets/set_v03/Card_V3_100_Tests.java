package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "103_100");     // Sauron, Lord of All Middle-earth
					put("runner", "1_178");       // Cheap minion for phase progression
					put("ithilstone", "9_47");        // Artifact

					put("aragorn", "1_89");
					put("anduril", "7_79");       // Artifact
					put("bilbo", "1_284");        // [Shire] ally - should NOT count
					put("guard1", "1_7");         // Dwarf Guard
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sauron, Lord of All Middle-earth
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 18
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 24
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: Sauron is twilight cost -1 for each artifact and companion you can spot (limit -6).
		 * 		While you can spot 6 companions, Sauron is <b>fierce</b>.  While you can spot 7 companions, Sauron is
		 * 		<b>relentless</b> <i>(he participates in 1 extra round of skirmishes after fierce.)</i>.
		 * 		Each time a companion loses a skirmish to Sauron, kill that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sauron");

		assertEquals("Sauron", card.getBlueprint().getTitle());
		assertEquals("Lord of All Middle-earth", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAIA, card.getBlueprint().getRace());
		assertEquals(18, card.getBlueprint().getTwilightCost());
		assertEquals(24, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}



	@Test
	public void SauronCostReducedByArtifactsAndCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var guard1 = scn.GetFreepsCard("guard1");
		var bilbo = scn.GetFreepsCard("bilbo");

		var ithilstone = scn.GetShadowCard("ithilstone");

		// Frodo (RB) + Aragorn + guard1 = 3 companions
		// Bilbo should not count
		// Anduril + Ithil Stone = 2 artifacts
		// Total: 5 discount
		scn.MoveCompanionsToTable(aragorn, guard1);
		scn.AttachCardsTo(aragorn, anduril);
		scn.MoveCardsToSupportArea(ithilstone, bilbo);
		scn.MoveCardsToHand(sauron);

		scn.StartGame();

		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowDeclineOptionalTrigger(); //ithil stone
		assertEquals(18 - 5, scn.GetTwilightCost(sauron));

		var twilight = scn.GetTwilight();
		scn.ShadowPlayCard(sauron);

		//-18 base cost, -2 roaming, +5 discount
		assertEquals(twilight - 18 - 2 + 5, scn.GetTwilight());
	}

	@Test
	public void SauronCostReductionCapsAtSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");

		// Frodo + Aragorn + 5 guards = 7 companions
		// Anduril = 1 artifact
		// Total would be 8, but capped at 6
		scn.MoveCompanionsToTable(aragorn, guard1, guard2, guard3, guard4, guard5);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();

		assertEquals(18 - 6, scn.GetTwilightCost(sauron));
	}

	@Test
	public void SauronGainsFierceWithSixCompanionsAndRelentlessWithSeven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");

		// Frodo + Aragorn + 3 guards = 5 companions
		scn.MoveCompanionsToTable(aragorn, guard1, guard2, guard3);
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();

		assertFalse(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.RELENTLESS));

		scn.MoveCompanionsToTable(guard4);

		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.RELENTLESS));

		scn.MoveCompanionsToTable(guard5);

		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sauron, Keyword.RELENTLESS));
	}

	@Test
	public void SauronKillsCompanionWhoLosesSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(sauron);

		// Aragorn base 8 + 5 = 13, enough to not be overwhelmed by 24
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 5));

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst(); //skirmish end or Sauron trigger

		// Aragorn loses (13 vs 24), should be killed not just wounded
		assertInZone(Zone.DEAD, aragorn);
	}
}
