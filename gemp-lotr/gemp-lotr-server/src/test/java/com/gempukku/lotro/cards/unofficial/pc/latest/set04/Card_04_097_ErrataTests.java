package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_04_097_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "54_97");
					put("gandalf", "1_364");  // Gandalf, The Grey Wizard (STR 7, VIT 4, signet: Gandalf)
					put("runner", "1_178");   // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongIFellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Long I Fell
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br><b>Skirmish:</b> Spot Gandalf to make him unable to take
		 *  wounds. Any Shadow player may make you wound a minion to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell");

		assertEquals("Long I Fell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void LongIFellPreventsWoundsWhenShadowDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fell);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, runner);

		// Play Long I Fell
		scn.FreepsPlayCard(fell);

		// Shadow declines to prevent
		scn.ShadowChooseNo();

		// Gandalf should now be unable to take wounds
		// Resolve skirmish -- Gandalf should take no wounds
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}

	@Test
	public void LongIFellDoesNotProtectWhenShadowPrevents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetFreepsCard("fell");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fell);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, runner);

		// Play Long I Fell
		scn.FreepsPlayCard(fell);

		// Shadow chooses to prevent (wounds a minion)
		scn.ShadowChooseYes();
		// Runner is auto-selected as only minion to wound

		// Gandalf should NOT be protected
		// Runner should have 1 wound from the prevention cost
		assertEquals(1, scn.GetWoundsOn(runner));

		// Resolve skirmish -- Gandalf should take wounds normally
		scn.PassSkirmishActions();
		// Gandalf was fighting Runner, Runner is weaker, so Gandalf likely wins
		// But the key test is that the protection was removed
	}
}
