package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Azog, Commander of the Wolves Army.  From his definition (no sheet has the Hobbit sets):
					// "Maneuver: Exert Azog, at a battleground, to play a [moria] minion from your discard
					// pile; its twilight cost is -2."
					put("azog", "32_28");
					// Demolition Troll: a [moria] Troll minion, cost 5, site 4; a skirmish ability only.
					put("troll", "32_31");
					// Two [moria] Orc minions, neither a Troll.  Goblin Patrol Troop: cost 6, site 4, no text.
					// Goblin Marksman: cost 3, site 4; Archer is its whole text.
					put("troop", "1_177");
					put("marksman", "1_176");
				}},
				new HashMap<>()
				{{
					// The Hobbit path.  Bag End: a fellowship ability that plays a [dwarven] follower.
					put("site1", "30_49");
					// The Troll Hoard: forest, underground, Shadow number 3; an optional trigger for the Free
					// Peoples player at the start of a fellowship phase there, which no turn of this file has.
					put("site2", "30_50");
					put("site3", "30_51");     // Rivendell, Shadow number 1
					put("site4", "30_52");     // Goblin Town
					put("site5", "30_53");     // Mirkwood
					put("site6", "30_54");     // Esgaroth
					put("site7", "30_55");     // Erebor
					put("site8", "30_56");     // Smaug's Den
					put("site9", "30_57");     // Battle of the Five Armies
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RivendellStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Rivendell
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 3
		 * Shadow Number: 1
		 * Direction: none
		 * Game Text: (No sheet has the Hobbit sets and the definition carries no game text: these are the
		 * 	definition's own fields.  It gives the site no keyword and no arrow; as site 3 it is a sanctuary by
		 * 	the game's rule.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
	}

	// From the definitions, there being no text to go by: no Troll may be played while the fellowship is at
	// Rivendell.  Azog's ability plays a card from the discard pile, and a play that an action is used for is a
	// cost of it (Ketura on Q-0062): with a Troll the only card he could bring back, the ability is not offered.
	// At the site before, on the same board, it is.
	@Test
	public void ATrollCannotBePlayedFromTheDiscardPileWhileTheFellowshipIsAtRivendell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var azog = scn.GetShadowCard("azog");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(azog);
		scn.MoveCardsToDiscard(troll);
		// Azog's ability needs a battleground and neither site is one.  In play Watchful Orc 30_40 makes every
		// site a battleground for the turn (the old test spent its ability on that); here the keyword is given.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.BATTLEGROUND));
		scn.SetTwilight(20);

		// The control.  At The Troll Hoard, with the Troll alone in the discard pile, Azog may bring it back.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals("30_50", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(azog));

		// He does not, and nobody is assigned: the fellowship moves on to Rivendell in the regroup phase.
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals("30_51", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(azog));
		assertInZone(Zone.DISCARD, troll);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(azog));

		// Two Orcs beside the Troll in the discard pile, and the ability is there again: for them.
		scn.MoveCardsToDiscard(troop, marksman);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(azog));
		// 20 set + 1 companion and 3 for The Troll Hoard + 1 companion and 1 for Rivendell
		assertEquals(26, scn.GetTwilight());

		scn.ShadowUseCardAction(azog);

		assertEquals(1, scn.GetWoundsOn(azog));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, marksman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCards(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, troll, marksman);
		// 26 - 6 to play + 2 off by Azog's ability - 2 roaming (its site number is 4 and Rivendell is site 3)
		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
