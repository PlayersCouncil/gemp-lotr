package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_009_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("legolas", "101_9");
					put("lorien", "51_53");
					put("bow", "1_41");
					put("aragorn", "1_89");
					put("gornbow", "1_90");

					put("shelob", "10_23");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Legolas, Keen-eyed
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Signet: Gandalf
		 * Game Text: Archer.
		 * 	Each time a minion takes a wound during the archery phase, make Legolas strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Keen-eyed", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void EachArcheryPhaseWoundPumpsLegolasUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var lorien = scn.GetFreepsCard("lorien");
		var bow = scn.GetFreepsCard("bow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gornbow = scn.GetFreepsCard("gornbow");

		scn.FreepsMoveCharToTable(legolas, lorien, aragorn);
		scn.FreepsAttachCardsTo(aragorn, "gornbow");
		scn.FreepsAttachCardsTo(lorien, "bow");

		scn.ShadowMoveCharToTable("shelob");

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsUseCardAction(gornbow);

		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(7, scn.GetStrength(legolas));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

		//+2 from the two regular archery wounds put on Shelob
		assertEquals(9, scn.GetStrength(legolas));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetStrength(legolas));
	}
}
