package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_027_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentback", "59_27");
					put("gandalf1", "1_72");
					put("gandalf2", "7_36");
					put("radagast1", "9_26");
					put("radagast2", "9_26");

					put("saruman", "3_69");
					put("staff", "4_174");
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 9
		* Title: Sent Back
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Gandalf
		* Twilight Cost: 2
		* Type: condition
		* Subtype: Support Area
		* Game Text: <b>Skirmish:</b> Discard this condition to discard each minion skirmishing your Wizard.
		 * Place that Wizard in your dead pile.
	    * <b>Fellowship</b>: Play a Wizard (even if another copy of that Wizard is in your dead pile).
		*/

		//Pre-game setup
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback");

		assertFalse(sentback.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sentback.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, sentback.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, sentback.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(sentback, Keyword.SUPPORT_AREA));
		assertEquals(2, sentback.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityDiscardsAllAssignedMinionsAndKillsWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback");
		var gandalf = scn.GetFreepsCard("gandalf1");
		var radagast = scn.GetFreepsCard("radagast1");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCompanionToTable(gandalf, radagast);

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(saruman, runner1, runner2);
		scn.AttachCardsTo(saruman, staff);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(staff); // permit saruman to skirmish

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(radagast, saruman, runner1, runner2);

		scn.FreepsResolveSkirmish(radagast);

		assertTrue(scn.FreepsActionAvailable(sentback));
		assertEquals(Zone.SHADOW_CHARACTERS, saruman.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, runner1.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, runner2.getZone());
		assertEquals(Zone.FREE_CHARACTERS, radagast.getZone());
		assertEquals(Zone.SUPPORT, sentback.getZone());
		scn.FreepsUseCardAction(sentback);

		// Radagast should be the only viable choice.  Saruman is viable in the original,
		// but the errata requires you to target "your Wizard" rather than "a Wizard"
		assertNotEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(Zone.DISCARD, saruman.getZone());
		assertEquals(Zone.DISCARD, runner1.getZone());
		assertEquals(Zone.DISCARD, runner2.getZone());
		assertEquals(Zone.DEAD, radagast.getZone());
		assertEquals(Zone.DISCARD, sentback.getZone());
	}

	@Test
	public void FellowshipAbilityCanResurrectDeadWizard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback");
		var grey = scn.GetFreepsCard("gandalf1");
		var white = scn.GetFreepsCard("gandalf2");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToDeadPile(grey);
		scn.MoveCardsToHand(white);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(sentback));
		assertEquals(Zone.DEAD, grey.getZone());
		assertEquals(Zone.HAND, white.getZone());
		scn.FreepsUseCardAction(sentback);
		assertEquals(Zone.DEAD, grey.getZone());
		assertEquals(Zone.FREE_CHARACTERS, white.getZone());
	}

	@Test
	public void SentBackHasNoRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback");
		var grey = scn.GetFreepsCard("gandalf1");
		var white = scn.GetFreepsCard("gandalf2");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToDeadPile(grey);
		scn.MoveCardsToHand(white);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(sentback));

		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.FreepsActionAvailable(sentback));
	}
}
