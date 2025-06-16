package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blood", "6_91");
					put("eowyn", "5_122");
					put("mount", "4_287");

					put("twk", "8_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodHasBeenSpilledStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Blood Has Been Spilled
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.
		 * <b>Skirmish:</b> Discard a mount borne by a [rohan] Man to prevent
		 * that Man from being overwhelmed unless his or her strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blood");

		assertEquals("Blood Has Been Spilled", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BloodHasBeenSpilledDiscardsAMountOnARohanCompanionToAddOverwhelmProtection() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var eowyn = scn.GetFreepsCard("eowyn");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionToTable(eowyn);
		scn.AttachCardsTo(eowyn, mount);
		scn.MoveCardsToSupportArea(blood);

		var twk = scn.GetShadowCard("twk");
		scn.MoveMinionsToTable(twk);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, twk);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.FreepsActionAvailable(blood));
		assertEquals(Zone.ATTACHED, mount.getZone());
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(16, scn.GetStrength(twk)); //14 + 2 enduring due to the horse
		scn.FreepsUseCardAction(blood);

		assertEquals(Zone.DISCARD, mount.getZone());
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(16, scn.GetStrength(twk)); //14 + 2 enduring due to the horse

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(Zone.FREE_CHARACTERS, eowyn.getZone());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

	}
}
