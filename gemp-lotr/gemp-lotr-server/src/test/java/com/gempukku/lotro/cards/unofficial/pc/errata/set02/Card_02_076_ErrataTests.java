package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_076_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("sam", "1_311");
					put("helpless", "52_76");
					put("toto", "10_68");
					put("nelya","1_233");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void HelplessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Helpless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Bearer's special abilities cannot be used.
		 * Maneuver: Exert a non-enduring Nazgul to transfer this to a Ring-bound companion.
		 * Response: If a burden is removed, spot or reveal a Nazgul from hand to transfer this to a Ring-bound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helpless");

		assertEquals("Helpless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HelplessBlocksSpecialAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		scn.ShadowAttachCardsTo(sam, helpless);
		scn.ShadowMoveCharToTable(nelya);

		scn.StartGame();
		//The Fellowship burden-removing special ability should be blocked
		assertFalse(scn.FreepsActionAvailable(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, nelya);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		//The Response ring-bearer special ability should also be blocked
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void HelplessDoesNotBlockAbilitiesWhileInSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		scn.ShadowMoveCardToSupportArea(helpless);
		scn.ShadowMoveCharToTable(nelya);

		scn.StartGame();
		//The Fellowship burden-removing special ability should NOT be blocked
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, nelya);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();

		//The Response ring-bearer special ability should also NOT be blocked
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void HelplessManeuverActionTransfersToRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var toto = scn.GetShadowCard("toto");
		var nelya = scn.GetShadowCard("nelya");
		scn.ShadowMoveCardToSupportArea(helpless);
		scn.ShadowMoveCharToTable(toto, nelya);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(helpless));
		assertEquals(0, scn.GetWoundsOn(toto));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(Zone.SUPPORT, helpless.getZone());

		scn.ShadowUseCardAction(helpless);
		// Exert automatically put on Nelya as the only non-enduring nazgul option
		assertEquals(0, scn.GetWoundsOn(toto));
		assertEquals(1, scn.GetWoundsOn(nelya));

		//Can go on either Sam or Frodo
		assertEquals(2, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCard(frodo);
		assertEquals(Zone.ATTACHED, helpless.getZone());
		assertEquals(frodo, helpless.getAttachedTo());
	}

	@Test
	public void HelplessResponseCanSpotNazgulToTransfer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var toto = scn.GetShadowCard("toto");
		var nelya = scn.GetShadowCard("nelya");
		scn.ShadowMoveCardToSupportArea(helpless);
		scn.ShadowMoveCharToTable(nelya);
		scn.ShadowMoveCardToDiscard(toto);

		scn.StartGame();

		//1 added from bid
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);

		assertEquals(Zone.SUPPORT, helpless.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		scn.ShadowChooseCard(sam);
		assertEquals(Zone.ATTACHED, helpless.getZone());
		assertEquals(sam, helpless.getAttachedTo());

		assertFalse(scn.FreepsActionAvailable(sam));
	}

	@Test
	public void HelplessResponseCanRevealNazgulToTransfer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var toto = scn.GetShadowCard("toto");
		var nelya = scn.GetShadowCard("nelya");
		scn.ShadowMoveCardToSupportArea(helpless);
		scn.ShadowMoveCardToHand(nelya);
		scn.ShadowMoveCardToDiscard(toto);

		scn.StartGame();

		//1 added from bid
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);

		assertEquals(Zone.SUPPORT, helpless.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		scn.ShadowChooseCard(sam);
		assertEquals(Zone.ATTACHED, helpless.getZone());
		assertEquals(sam, helpless.getAttachedTo());

		assertFalse(scn.FreepsActionAvailable(sam));
	}
}
