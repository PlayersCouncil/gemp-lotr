package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_076_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");
					put("helpless", "52_76");
					put("toto", "10_68");
					put("nelya","1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelplessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Helpless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Bearer's special abilities cannot be used.
		* 	Maneuver: If this is in your support area, exert a Nazgul to transfer this to a Ring-bound companion.
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
		scn.MoveCompanionToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		scn.AttachCardsTo(sam, helpless);
		scn.MoveMinionsToTable(nelya);

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
		scn.MoveCompanionToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToSupportArea(helpless);
		scn.MoveMinionsToTable(nelya);

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
		scn.MoveCompanionToTable(sam);

		var helpless = scn.GetShadowCard("helpless");
		var toto = scn.GetShadowCard("toto");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToSupportArea(helpless);
		scn.MoveMinionsToTable(toto, nelya);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(helpless));
		assertEquals(0, scn.GetWoundsOn(toto));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(Zone.SUPPORT, helpless.getZone());

		scn.ShadowUseCardAction(helpless);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(toto);
		assertEquals(1, scn.GetWoundsOn(toto));
		assertEquals(0, scn.GetWoundsOn(nelya));

		//Can go on either Sam or Frodo
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(frodo);
		assertEquals(Zone.ATTACHED, helpless.getZone());
		assertEquals(frodo, helpless.getAttachedTo());
	}

}
