package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fimbul", "30_33");
					// Azog, Commander of the Wolves Army: a [moria] Orc of vitality 3, whose one ability is a
					// Maneuver one that needs a battleground.  The only Orc in play.
					put("azog", "32_28");
					// Uruk Savage: an Uruk-hai of vitality 3 whose whole text is Damage +1.  A minion who could
					// exert and is no Orc.
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FimbulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Fimbul, Orkish Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Damage +1.  (No sheet has the Hobbit sets and the definition carries no game text: these
		 * 	are the definition's own fields.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fimbul");

		assertEquals("Fimbul", card.getBlueprint().getTitle());
		assertEquals("Orkish Assassin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	// From the definition, there being no text to go by: Fimbul costs an exertion of an Orc to play, over and
	// above his twilight cost.
	@Test
	public void IsPlayedByExertingAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fimbul = scn.GetShadowCard("fimbul");
		var azog = scn.GetShadowCard("azog");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(fimbul);

		scn.StartGame();
		scn.MoveMinionsToTable(azog, savage);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fimbul));
		assertInZone(Zone.HAND, fimbul);
		assertEquals(0, scn.GetWoundsOn(azog));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.ShadowPlayCard(fimbul);

		// Azog is the only Orc, so the exertion is his and nothing is asked.  Were the Uruk Savage a candidate
		// too, the game would be waiting here for a choice between them.
		assertEquals(1, scn.GetWoundsOn(azog));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, fimbul);
		assertEquals(0, scn.GetWoundsOn(fimbul));
		// 7 in the pool - 2 to play - 2 roaming (his site number is 3 and the fellowship is at site 2)
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
