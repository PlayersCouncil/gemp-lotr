package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "8_95");
					put("assassin2", "8_95");
					// Great Siege-towers: a [sauron] engine condition in the support area. A Shadow ability that
					// plays a besieger and a skirmish ability, neither used.
					put("towers", "7_281");
					// Grond, Hammer of the Underworld: a [sauron] engine that is a possession, in the support area.
					// A Shadow ability and a regroup ability, neither used.
					put("grond", "8_103");
					// Great Peril of Fire: a [sauron] support-area condition that is no engine; a maneuver ability only.
					put("peril", "7_280");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability only
					put("haldir", "1_48");        // Haldir, Elf of the Golden Wood: [elven] Elf, unbound, no archer; +2 only at sites 6 to 8
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Assignment:</b> Spot a [sauron] engine or a site you control to assign this minion to an unbound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("assassin");

		assertEquals("Gorgoroth Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingASauronEngineItAssignsItselfToAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, haldir, sam);

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(towers);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));
		assertFalse(scn.IsCharAssigned(assassin));

		scn.ShadowUseCardAction(assassin);
		// Two unbound companions of other cultures and races; not the Ring-bearer, nor Sam, who is Ring-bound.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, haldir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(haldir);

		// This minion, and not the other copy.
		assertTrue(scn.IsCharAssignedAgainst(haldir, assassin));
		assertFalse(scn.IsCharAssigned(assassin2));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ASauronEngineThatIsAPossessionAlsoLetsItAssignItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, haldir);

		var assassin = scn.GetShadowCard("assassin");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, assassin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ASiteTheShadowPlayerControlsAlsoLetsItAssignItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, haldir);

		var assassin = scn.GetShadowCard("assassin");
		var peril = scn.GetShadowCard("peril");
		scn.MoveCardsToSupportArea(peril);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin);

		// Player 1's second turn: each player has moved to site 2, so site 1 is behind every marker.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, assassin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWithASauronConditionThatIsNoEngineOrASiteTheOtherPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, haldir);

		var assassin = scn.GetShadowCard("assassin");
		var peril = scn.GetShadowCard("peril");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(peril);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin);

		// Player 1's second turn: each player has moved to site 2, so site 1 is behind every marker.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Site 1 is in the Free Peoples player's hands, not the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));

		// An engine beside them, and it is offered.
		scn.MoveCardsToSupportArea(towers);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));
	}

	@Test
	public void NotOfferedWithNoUnboundCompanionToAssignItTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var assassin = scn.GetShadowCard("assassin");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(towers);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		// The engine is spotted, but Frodo and Sam are Ring-bound.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));
	}

	@Test
	public void OnceItIsAssignedItIsNotOfferedAgainThoughAnUnboundCompanionIsUnassigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(aragorn, haldir);

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(towers);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));
		assertTrue(scn.ShadowActionAvailable(assassin2));
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(haldir);
		assertTrue(scn.IsCharAssignedAgainst(haldir, assassin));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(assassin2));

		// An assigned minion cannot be assigned again, so the first Assassin's ability would do nothing: it is not
		// offered, though the engine is still spotted and Aragorn is unassigned. The second Assassin's still is.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));
		assertTrue(scn.ShadowActionAvailable(assassin2));
	}
}
