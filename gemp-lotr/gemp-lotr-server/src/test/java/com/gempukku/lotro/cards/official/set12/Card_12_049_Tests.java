package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_049_Tests
{

	// The Fellowship path with Pass of Caradhras 1_348 (Mountain only) as site 4, and Mount Doom 15_193
	// (Battleground, Mountain, Underground) to be laid as site 5 by MoveCardToAdventurePath; site 9 is never reached.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "12_49");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, Knight his whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit knight; his skirmish ability is used only on purpose
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man who is no knight; a regroup ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");      // Pass of Caradhras: Mountain, Shadow number 2; its Shadow ability needs an [isengard] minion
					remove("site9");
					put("doom", "15_193");      // Mount Doom: Shadow number 3; sites in its region cannot be replaced
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianSteedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Gondorian Steed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Resistance: +1
		 * Game Text: Bearer must be a knight.<br>Bearer is <b>damage +1</b>.<br>Each time the fellowship moves from a mountain site, add (2).
		*/

		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");

		assertEquals("Gondorian Steed", steed.getBlueprint().getTitle());
		assertNull(steed.getBlueprint().getSubtitle());
		assertFalse(steed.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, steed.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, steed.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, steed.getBlueprint().getCardType());
		assertTrue(steed.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, steed.getBlueprint().getTwilightCost());
		assertEquals(1, steed.getBlueprint().getStrength());
		assertEquals(1, steed.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnAKnightOfEitherRaceAndMakesThatBearerAloneDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var steed = scn.GetFreepsCard("steed");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, pippin, ranger);
		scn.MoveCardsToHand(steed);

		scn.StartGame();
		assertInHand(steed);
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));

		assertTrue(scn.FreepsPlayAvailable(steed));
		scn.FreepsPlayCard(steed);
		// A [gondor] Man and a [gondor] Hobbit, both knights; not the [gondor] Man who is no knight, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		assertAttachedTo(steed, knight);
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		// The other knight does not bear it.
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMoveToAMountainAddsNothingAndTheMoveFromItAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, steed);

		scn.StartGame();
		assertAttachedTo(steed, knight);
		scn.SkipToSite(3);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		// From Rivendell Valley, which is no mountain, to Pass of Caradhras.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 2 for the site + 2 companions, and nothing from the Steed
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertEquals(4, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// 4 + 6 for The Bridge of Khazad-dûm + 2 companions + 2 for moving from the mountain
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveFromAMountainAdds2AgainWhateverElseTheMountainIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, steed);

		var doom = scn.GetShadowSite("doom");

		scn.StartGame();
		assertAttachedTo(steed, knight);
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(doom);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		// From Pass of Caradhras (a mountain and nothing else) to Mount Doom.
		scn.FreepsPass();
		assertEquals(doom, scn.GetCurrentSite());
		// 3 for Mount Doom + 3 for region 2 (a Shadows-block site on a numbered path) + 2 companions + 2
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// From Mount Doom (a battleground and underground as well) to Galadriel's Glade.
		scn.SkipToMovementDecision();
		assertEquals(10, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 10 + 3 for Galadriel's Glade + 2 companions + 2
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
