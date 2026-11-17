package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vicious", "17_92");

					put("mountainorc", "11_129"); // Mountain Orc: [orc] Orc, strength 9; its trigger is on being assigned
					put("goblin", "11_137");      // Scurrying Goblin: [orc] Orc, strength 5; its trigger is on being played
					put("dreg", "12_92");         // Orc Dreg: [orc] Orc, strength 10; its trigger is on being assigned
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, printed strength 9, +2 at an underground site
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8
					put("runner", "1_178");       // Goblin Runner: a [moria] Orc, strength 5; its trigger is on being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ViciousWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vicious Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +4
		 * Vitality: +2
		 * Game Text: Bearer must be an [orc] Orc with strength 9 or less. Bearer is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vicious");

		assertEquals("Vicious Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedOnlyOnAnOrcOrcOfStrength9OrLessWhoIsThenFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vicious = scn.GetShadowCard("vicious");
		var mountainorc = scn.GetShadowCard("mountainorc");
		var goblin = scn.GetShadowCard("goblin");
		var dreg = scn.GetShadowCard("dreg");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(vicious);
		scn.SetTwilight(10);
		// The fellowship moves to Balin's Tomb, site 4: an underground site.
		scn.FreepsPass();
		scn.MoveMinionsToTable(mountainorc, goblin, dreg, denizen, halftroll, runner);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());

		assertEquals(9, scn.GetStrength(mountainorc));
		assertEquals(5, scn.GetStrength(goblin));
		assertEquals(10, scn.GetStrength(dreg));
		// Printed 9, but 11 here: it is the strength in play that is read.
		assertEquals(11, scn.GetStrength(denizen));
		assertFalse(scn.HasKeyword(mountainorc, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));

		assertTrue(scn.ShadowPlayAvailable(vicious));
		scn.ShadowPlayCard(vicious);
		// [orc] Orcs of two titles at 9 or less are offered; an [orc] Orc of 10 or of 11, an [orc] minion who
		// is no Orc and an Orc who is not [orc] are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mountainorc, goblin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dreg, denizen, halftroll, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(mountainorc);

		assertAttachedTo(vicious, mountainorc);
		assertTrue(scn.HasKeyword(mountainorc, Keyword.FIERCE));
		// Another [orc] Orc who does not bear it is not made fierce.
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
