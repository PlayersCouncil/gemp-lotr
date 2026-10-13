package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wake", "17_45");

					put("brand", "17_47");      // Primitive Brand: [men] possession, a hand weapon borne by a [men] minion; bearer is strength +2
					put("armor", "17_41");      // Ceremonial Armor: [men] possession played to the support area; a response to a wound, not met here
					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; a skirmish ability never used here
					put("haradrim", "11_74");   // Detachment of Haradrim: [men] Man, strength 12, vitality 3; a maneuver ability never used here
					put("runner", "1_178");     // Goblin Runner: a minion to lie stacked on a possession
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntheWildMensWakeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: In the Wild Men's Wake
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a [men] possession in your support area and a [men] minion. Make that minion strength +1 for each minion stacked on that possession. You may discard that possession to make that minion <b>damage</b> <b>+1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wake");

		assertEquals("In the Wild Men's Wake", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAMenPossessionInTheSupportAreaAndNotOneThatAMinionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var brand = scn.GetShadowCard("brand");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var haradrim = scn.GetShadowCard("haradrim");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wake);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(haradrim);
		// The only [men] possession in play is borne by a minion: it is in no support area.
		scn.AttachCardsTo(haradrim, brand);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, haradrim);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertAttachedTo(brand, haradrim);
		assertFalse(scn.ShadowPlayAvailable(wake));

		// Two [men] possessions lying in the support area, one with a minion stacked on it and one with none.
		scn.MoveCardsToSupportArea(armor, oath);
		scn.StackCardsOn(armor, runner);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wake));

		var pool = scn.GetTwilight();
		// 12 + 2 for the Primitive Brand
		assertEquals(14, scn.GetStrength(haradrim));

		scn.ShadowPlayCard(wake);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brand));
		scn.ShadowChooseCard(armor);

		// The Detachment is the only [men] minion: it is strength +1 for the one minion stacked on the Armor.
		assertEquals(15, scn.GetStrength(haradrim));
		// The possession need not be discarded.
		scn.ShadowChooseNo();
		assertInZone(Zone.SUPPORT, armor, oath);
		assertAttachedTo(brand, haradrim);
		assertEquals(15, scn.GetStrength(haradrim));
		assertInZone(Zone.DISCARD, wake);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
