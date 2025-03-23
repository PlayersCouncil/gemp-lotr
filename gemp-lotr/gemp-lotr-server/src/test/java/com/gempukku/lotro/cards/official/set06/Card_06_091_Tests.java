package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_091_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("blood", "6_91");
					put("eowyn", "5_122");
					put("mount", "4_287");

					put("twk", "8_84");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
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
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BloodHasBeenSpilledDiscardsAMountOnARohanCompanionToAddOverwhelmProtection() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var eowyn = scn.GetFreepsCard("eowyn");
		var mount = scn.GetFreepsCard("mount");
		scn.FreepsMoveCharToTable(eowyn);
		scn.FreepsAttachCardsTo(eowyn, mount);
		scn.FreepsMoveCardToSupportArea(blood);

		var twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCharToTable(twk);

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
		assertEquals(Phase.REGROUP, scn.getPhase());

	}
}
