package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "15_43");
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
					put("gollum", "15_43");
					// Gimli, Son of Glóin: strength 6, which is not the Ring-bearer's; his skirmish ability is
					// not used
					put("gimli", "1_13");
					// Sam, Son of Hamfast: Ring-bound, strength 3, and not the Ring-bearer; his abilities are not used
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: strength 3 while he bears no weapon
					put("merry", "1_303");
					// Goblin Runner: a minion for the skirmish that does not involve Gollum (its (2) is not met: it
					// is cheated in)
					put("runner", "1_178");
					// Hobbit Sword: strength +2, borne by a Hobbit
					put("sword", "1_299");
					// Hope is Kindled: [shire] skirmish event, "Make a Hobbit strength +2"
					put("hope", "12_123");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gollum, Hopeless
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: 
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: At the start of each skirmish involving Gollum, he is strength +X until the end of that skirmish, where X is the Ring-bearer's strength.
		*/

		var scn = GetScenario();

		var gollum = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", gollum.getBlueprint().getTitle());
		assertEquals("Hopeless", gollum.getBlueprint().getSubtitle());
		assertTrue(gollum.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, gollum.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, gollum.getBlueprint().getCulture());
		assertEquals(CardType.MINION, gollum.getBlueprint().getCardType());
		assertEquals(2, gollum.getBlueprint().getTwilightCost());
		assertEquals(4, gollum.getBlueprint().getVitality());
		assertEquals(3, gollum.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfASkirmishInvolvingGollumHeGainsTheRingBearersStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, sam);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, gollum);

		// Assigned, and his skirmish not yet begun: no strength is printed on him.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(0, scn.GetStrength(gollum));
		// 3, and 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));
		// Sam is Ring-bound, but not the Ring-bearer
		assertEquals(3, scn.GetStrength(sam));

		// The Ring-bearer's strength, not that of the companion he fights, nor Sam's.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(4, scn.GetStrength(gollum));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStartOfASkirmishThatDoesNotInvolveGollumLeavesHimAsHeIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gimli, merry);

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{merry, gollum});

		assertEquals(0, scn.GetStrength(gollum));
		scn.FreepsResolveSkirmish(gimli);
		// Gimli's skirmish against the Runner has begun; Gollum waits for his own.
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertEquals(0, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));

		// Gimli (6, damage +1) beats the Runner (5, vitality 1), who dies.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(0, scn.GetStrength(gollum));

		scn.FreepsResolveSkirmish(merry);
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsTheRingBearersStrengthAsHisOwnCardsMakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(frodo, sword);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		assertAttachedTo(sword, frodo);
		// 3 + 1 for The Ruling Ring + 2 for the Hobbit Sword
		assertEquals(6, scn.GetStrength(frodo));

		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, gollum);
		assertEquals(0, scn.GetStrength(gollum));

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsTakenAsTheSkirmishStartsAndARingBearerMadeStrongerAfterwardsLeavesItAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var hope = scn.GetFreepsCard("hope");
		scn.MoveCardsToHand(hope);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		assertInHand(hope);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, gollum);

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(gollum));

		// Frodo is the only Hobbit, so he is made strength +2 unasked.
		scn.FreepsPlayCard(hope);
		assertEquals(6, scn.GetStrength(frodo));
		// X was the 4 Frodo had as the skirmish started (Q-0270).
		assertEquals(4, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void HeIsStrengthenedAgainAtTheStartOfAFierceSkirmishAndTheBonusEndsWithEachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Fierce, so that he skirmishes a second time in the turn: no printed card makes a [gollum] minion fierce
		// away from site 9.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, gollum);
		assertEquals(0, scn.GetStrength(gollum));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(4, scn.GetStrength(gollum));

		// Gimli (6, damage +1) beats Gollum (4) and places 2 wounds; vitality 4, he lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// The skirmish is over, and so is the bonus.
		assertEquals(0, scn.GetStrength(gollum));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, gollum);
		assertEquals(0, scn.GetStrength(gollum));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(4, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
