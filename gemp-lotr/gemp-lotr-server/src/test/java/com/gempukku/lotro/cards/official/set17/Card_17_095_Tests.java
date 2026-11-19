package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "17_95");
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, strength 6; valiant and no other text
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability only
					put("mount", "4_287");       // Rider's Mount: [rohan] mount on a [rohan] Man; minions skirmishing bearer exert
					put("mount2", "4_287");
					put("chain", "4_278");       // Heavy Chain: [rohan] armor on a Man, no mount

					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1; its play trigger does not fire when cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo loses to a Goblin Runner and goes on
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Éomer, Northman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Damage +1</b>.<br>While the Ring-bearer is assigned to a skirmish, Éomer is strength +3.<br><b>Maneuver:</b> If Éomer is mounted, exert him to make him <b>defender +1</b> until the end of turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", card.getBlueprint().getTitle());
		assertEquals("Northman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileTheRingBearerIsAssignedEomerIsStrengthPlus3UntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(eomer, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{eomer, runner2});

		// Éomer alone: the other [rohan] companion is untouched.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(10, scn.GetStrength(eomer));
		assertEquals(6, scn.GetStrength(guard));

		// The Ring-bearer's own skirmish first: he is still assigned while it is fought.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(10, scn.GetStrength(eomer));
		scn.PassSkirmishActions();

		// Once his skirmish is over he is no longer assigned (CR 4.2 "skirmish phase(s)").
		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.IsCharSkirmishing(eomer));
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerAssignedGivesNoBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eomer = scn.GetFreepsCard("eomer");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(eomer, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{eomer, runner2});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharAssigned(sam));
		assertEquals(7, scn.GetStrength(eomer));
	}

	@Test
	public void MountedManeuverAbilityExertsEomerToMakeHimDefenderPlus1UntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, mount);
		scn.AttachCardsTo(guard, mount2);
		assertAttachedTo(mount, eomer);
		assertAttachedTo(mount2, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(eomer));

		scn.FreepsUseCardAction(eomer);
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		// "Him": the other mounted companion is untouched.
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still in force in the Shadow phase after a second move (site 3).
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));

		// Gone with the end of the turn.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileEomerBearsAPossessionThatIsNoMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var mount = scn.GetFreepsCard("mount");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, chain);
		// Another companion is mounted: the requirement is Éomer's own.
		scn.AttachCardsTo(guard, mount);
		assertAttachedTo(chain, eomer);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertFalse(scn.FreepsActionAvailable(eomer));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileEomerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var mount = scn.GetFreepsCard("mount");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, mount);
		assertAttachedTo(mount, eomer);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(eomer, 2);
		assertEquals(2, scn.GetWoundsOn(eomer));

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(eomer));
		assertFalse(scn.FreepsActionAvailable(eomer));
	}
}
