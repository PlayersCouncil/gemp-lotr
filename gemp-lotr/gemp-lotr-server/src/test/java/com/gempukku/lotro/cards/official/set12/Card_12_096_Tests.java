package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "12_96");
					put("emboldened", "11_119"); // Emboldened Orc: [orc] Orc, twilight cost 4; a standing strength bonus only
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll, twilight cost 3; an assignment-phase trigger only
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, twilight cost 3; stronger underground only
					put("hound", "7_192");       // Morgul Hound: [wraith] Orc; stronger only at 2 wounds on the Ring-bearer
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("gauntlets", "11_109");  // Bladed Gauntlets: [orc] armor, cost 1, borne by an [orc] minion; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Spear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be an [orc] minion.<br>When you play this possession on a minion that has a twilight cost of 4 or more, add (2).
		*/

		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");

		assertEquals("Orc Spear", spear.getBlueprint().getTitle());
		assertNull(spear.getBlueprint().getSubtitle());
		assertFalse(spear.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spear.getBlueprint().getSide());
		assertEquals(Culture.ORC, spear.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, spear.getBlueprint().getCardType());
		assertTrue(spear.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, spear.getBlueprint().getTwilightCost());
		assertEquals(2, spear.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnOrcMinionAndAddsNothingOnOneOfTwilightCost3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var emboldened = scn.GetShadowCard("emboldened");
		var halftroll = scn.GetShadowCard("halftroll");
		var denizen = scn.GetShadowCard("denizen");
		var hound = scn.GetShadowCard("hound");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(spear);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(emboldened, halftroll, denizen, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		// [orc] minions of two races are offered; an Orc of another culture and an [orc] card that is no minion are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(emboldened, halftroll, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertEquals(denizen, spear.getAttachedTo());
		// Twilight cost 3, one short: the Spear's 1 is paid and nothing is added.
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedOnAMinionOfTwilightCost4ItAdds2AndAnotherPossessionPlayedAfterItDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var emboldened = scn.GetShadowCard("emboldened");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(spear, gauntlets);

		scn.StartGame();
		scn.MoveMinionsToTable(emboldened, denizen);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(spear);
		scn.ShadowChooseCard(emboldened);
		assertEquals(emboldened, spear.getAttachedTo());
		// - 1 to play + 2 for a bearer of twilight cost 4
		assertEquals(pool + 1, scn.GetTwilight());

		scn.ShadowPlayCard(gauntlets);
		scn.ShadowChooseCard(emboldened);
		assertEquals(emboldened, gauntlets.getAttachedTo());
		// The Gauntlets' 1, and nothing from the Spear.
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
