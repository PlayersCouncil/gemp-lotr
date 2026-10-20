package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_166_Tests
{

	// Cave Troll of Moria, Scourge of the Black Pit: the [moria] card of the title.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hammer", "1_166");
					// Cave Troll of Moria, Scourge of the Black Pit: [moria] Troll, damage +1 and fierce in print;
					// his requirement and discount are about his own play, and he is cheated in
					put("troll", "1_165");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion of another title, no text
					// Troll of the Deep, Cave Troll: a Troll of another title ([orc]); a skirmish ability used only
					// on purpose
					put("deep", "19_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Cave Troll of Moria, Savage Menace: the [orc] card of the title.  The two share a unique title and never
	// stand on one board.
	protected VirtualTableScenario GetOrcTrollScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hammer", "1_166");
					// Cave Troll of Moria, Savage Menace: [orc] Troll, damage +1, fierce and toil 2 in print; an
					// optional trigger when an [orc] lurker wins a skirmish (he is none)
					put("troll", "12_85");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion of another title, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CaveTrollsHammerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Cave Troll's Hammer
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +3
		 * Game Text: Bearer must be Cave Troll of Moria.
		 * 	Cave Troll of Moria is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hammer");

		assertEquals("Cave Troll's Hammer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void CannotBePlayedUntilCaveTrollOfMoriaIsInPlayAndThenMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetShadowCard("hammer");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var deep = scn.GetShadowCard("deep");
		scn.MoveCardsToHand(hammer);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, deep);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [moria] minion and a Troll, neither of them Cave Troll of Moria.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hammer));

		scn.MoveMinionsToTable(troll);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(deep, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(hammer));

		// He is the only card it may go on: it goes on him with nothing asked.
		scn.ShadowPlayCard(hammer);

		assertAttachedTo(hammer, troll);
		// Damage +1 printed, and +1 from the Hammer.
		assertEquals(2, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(deep, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedOnTheOrcCultureCaveTrollOfMoriaAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOrcTrollScenario();

		var hammer = scn.GetShadowCard("hammer");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(hammer);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hammer));

		scn.ShadowPlayCard(hammer);

		assertAttachedTo(hammer, troll);
		assertEquals(2, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
