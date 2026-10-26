package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "8_72");
					put("cantea", "1_230");     // Úlairë Cantëa, Lieutenant of Dol Guldur: a [wraith] Nazgul; a skirmish ability used only on purpose
					put("hound", "7_192");      // Morgul Hound: another [wraith] Orc; its text needs 2 wounds on the Ring-bearer
					put("steed", "1_208");      // Black Steed: a [wraith] possession (mount) on a Nazgul, no minion; its text reads plains and underground sites
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, no [wraith] card; his text needs 3 or 5 burdens
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; a skirmish ability only
					put("stairs1", "1_24");     // Stairs of Khazad-dum: a Free Peoples condition of cost 0, to play from a hand of four
					put("stairs2", "1_24");
					put("stairs3", "1_24");
					put("stairs4", "1_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GothmogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gothmog, Morgul Commander
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you have initiative, each [wraith] minion is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gothmog");

		assertEquals("Gothmog", card.getBlueprint().getTitle());
		assertEquals("Morgul Commander", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileTheShadowHasInitiativeEachWraithMinionIsAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gothmog = scn.GetShadowCard("gothmog");
		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var khamul = scn.GetShadowCard("khamul");
		var soldier = scn.GetShadowCard("soldier");
		var stairs1 = scn.GetFreepsCard("stairs1");
		var stairs2 = scn.GetFreepsCard("stairs2");
		var stairs3 = scn.GetFreepsCard("stairs3");
		var stairs4 = scn.GetFreepsCard("stairs4");

		scn.StartGame();

		// The minions are laid early, in the fellowship phase (Q-0228), so that the threshold is crossed by a play.
		scn.MoveMinionsToTable(gothmog, cantea, hound, khamul, soldier);
		scn.AttachCardsTo(cantea, steed);
		scn.MoveCardsToHand(stairs1, stairs2, stairs3, stairs4);
		assertAttachedTo(steed, cantea);
		assertEquals(4, scn.GetFreepsHandCount());
		scn.FreepsRefreshDecision();

		// Four cards in the Free Peoples hand: he has initiative, and nobody is an archer.
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.HasKeyword(gothmog, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(cantea, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(hound, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(khamul, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(steed, Keyword.ARCHER));

		// Down to three: the Shadow has initiative.
		scn.FreepsPlayCard(stairs1);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());

		// Gothmog himself, a Nazgul and another Orc, all [wraith] minions.
		assertTrue(scn.HasKeyword(gothmog, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(cantea, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(hound, Keyword.ARCHER));
		// A Nazgul who is not [wraith], an Orc of another culture, and a [wraith] card that is no minion.
		assertFalse(scn.HasKeyword(khamul, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(steed, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
