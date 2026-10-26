package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horror", "14_13");
					// Two [orc] characters of the Shadow player's, of two titles and races, to exert for toil.
					// Denizen of Khazad-dûm: [orc] Orc, vitality 2; strength +2 at an underground site, which reaches no reading.
					put("denizen", "11_115");
					// Half-troll of Far Harad: [orc] Half-troll, vitality 2; its only text is a start-of-assignment trigger.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc with no text, vitality 3: a character of his of another culture.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorrorofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Horror of Harad
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Half-troll
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Ambush</b> (1). <b>Damage +1</b>. <b>Fierce</b>. <b>Lurker</b>. <b>Muster</b>. <b>Toil 2</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horror");

		assertEquals("Horror of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.HALF_TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertTrue(scn.HasKeyword(card, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToilTakesTwoOffHisCostForEachOrcCharacterExertedAsHeIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(horror);
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		scn.FreepsPass();
		// Site 4 is his site number: he is not roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 4 - 2 - 2: with both [orc] characters exerted he costs nothing; the [moria] Troop does not count.
		// Toil 1, or only one of them counted, would want 2.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(horror));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPlayCard(horror);
		// The pool is 2 short of his 4: one exertion is owed, and the second is his to make or not.
		// Toil 1 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));

		scn.ShadowChooseCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, horror);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 2 - (4 - 2); toil 3 would leave 1.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
