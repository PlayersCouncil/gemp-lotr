package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "4_174");
					put("saruman", "3_69");
					put("orc", "3_58");

					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Saruman's Staff, Wizard's Device
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Strength: 2
		 * Game Text: Plays on Saruman.
		 * He is <b>fierce</b> and <b>damage +1</b>.
		 * <b>Maneuver:</b> Make the first sentence of Saruman's game text not apply until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Saruman's Staff", card.getBlueprint().getTitle());
		assertEquals("Wizard's Device", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void SarumansStaffMakesSarumanDamagePlus1AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveMinionsToTable(saruman);
		scn.MoveCardsToHand(staff);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.HasKeyword(saruman, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(saruman, Keyword.FIERCE));

		scn.ShadowPlayCard(staff);
		assertTrue(scn.HasKeyword(saruman, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(saruman, Keyword.FIERCE));
	}

	@Test
	public void SarumansStaffManeuverActionPermitsSarumanToSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(staff));
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();

		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
	}

	@Test
	public void SarumansStaffDoesNotImpedeAbilityToUseSarumansAssignmentAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var orc = scn.GetShadowCard("orc");
		scn.MoveMinionsToTable(saruman, orc);
		scn.AttachCardsTo(saruman, staff);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionToTable(sam);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(staff);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		scn.ShadowChooseCard(saruman);
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssigned(saruman));
		assertTrue(scn.IsCharAssigned(sam));

	}
}
