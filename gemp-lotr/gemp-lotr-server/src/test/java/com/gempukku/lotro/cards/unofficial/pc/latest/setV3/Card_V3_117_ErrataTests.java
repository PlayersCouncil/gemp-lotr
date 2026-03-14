package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_117_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_117");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sting, Sharp Elven-blade
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Resistance: 1
		 * Game Text: Bearer must be a Ring-bound Hobbit.
		* 	Skirmish: If you cannot spot more than 4 companions, add a threat to make bearer strength +1 for each threat you can spot (limit +6).
		* 	Response: If The One Ring is transferred, play this on the new Ring-bearer.  You may use this ability from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Sharp Elven-blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void StingStrengthBonusCapsAt6() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata adds limit: 6 to the ForEachThreat strength bonus
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("card");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Add 7 threats so the limit of 6 can be tested
		scn.AddThreats(7);
		assertEquals(7, scn.GetThreats());

		// Frodo base 3 + Ruling Ring 1 + Sting 1 = 5 before using ability
		int baseStr = scn.GetStrength(frodo);
		assertEquals(5, baseStr);

		// Move to skirmish
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// Use Sting's ability - it adds 1 threat, so now 8 threats
		// Strength bonus is +1 for each threat, limit 6
		scn.FreepsUseCardAction(sting);

		// After activating: 8 threats exist, but limit is 6, so +6
		// Frodo base 3 + Ring 1 + Sting 1 + ability 6 = 11
		assertEquals(11, scn.GetStrength(frodo));
	}
}
