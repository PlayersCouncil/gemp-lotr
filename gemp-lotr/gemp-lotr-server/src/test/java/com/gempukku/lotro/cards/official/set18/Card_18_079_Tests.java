package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frenzy", "18_79");
					// Denizen of Moria: an [orc] Orc who is no archer; a bonus only at an underground site.
					put("denizen", "11_116");
					// Goblin Bowman: a [moria] Orc archer with no other text.
					put("bowman", "2_60");
					// Legolas, Greenleaf: a Free Peoples archer, no minion; an archery ability never used.
					put("legolas", "1_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrenzyofArrowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Frenzy of Arrows
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an [orc] archer minion to add 2 to the minion archery total. If you do, you may add an additional 2 to the minion archery total for each follower you can spot.
		*/

		var scn = GetScenario();

		var frenzy = scn.GetFreepsCard("frenzy");

		assertEquals("Frenzy of Arrows", frenzy.getBlueprint().getTitle());
		assertNull(frenzy.getBlueprint().getSubtitle());
		assertFalse(frenzy.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, frenzy.getBlueprint().getSide());
		assertEquals(Culture.ORC, frenzy.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, frenzy.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(frenzy, Timeword.ARCHERY));
		assertEquals(2, frenzy.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAnOrcArcherMinionWhichNoPrintedCardMakes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var frenzy = scn.GetShadowCard("frenzy");
		var denizen = scn.GetShadowCard("denizen");
		var bowman = scn.GetShadowCard("bowman");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		assertInZone(Zone.HAND, frenzy);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(denizen, bowman);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// An [orc] minion who is no archer, an archer minion of another culture, and an archer who is no minion:
		// none of them is "an [orc] archer minion", and no printed card makes an [orc] minion an archer.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.HasKeyword(bowman, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(denizen, Keyword.ARCHER));
		assertFalse(scn.ShadowPlayAvailable(frenzy));
		assertInZone(Zone.HAND, frenzy);
	}
}
