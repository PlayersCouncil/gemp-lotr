package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_338_Tests
{

	// The King path with Beacon of Minas Tirith as its site 3.  Nothing on the way there asks anything of a
	// fellowship with no wound (Edoras Hall, King's Tent).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions, so that threats can be added: a threat cannot be added while there are as many
					// as there are companions.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; a fellowship ability used only on purpose
					// Pippin, Trollslayer: his requirement to play is about his own play, and he is cheated in;
					// a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Guma, Plains Farmer: an ally, vitality 2, home 4T (not on this path); a fellowship ability used
					// only on purpose
					put("guma", "4_277");
				}},
				new HashMap<>(VirtualTableScenario.KingSites)
				{{
					put("site3", "7_338");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeaconofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Beacon of Minas Tirith
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> If you cannot spot 3 threats, add a threat to heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Beacon of Minas Tirith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void WithTwoThreatsAddsAThreatToHealACompanionOfHisChoiceAndAtThreeItIsNoLongerOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var guma = scn.GetFreepsCard("guma");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam, pippin);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.SkipToSite(3);
		var beacon = scn.GetCurrentSite();

		// The wounds go on after the turn's sanctuary healing, which was asked of nobody.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(guma, 1);
		scn.AddThreats(2);
		// Nearly counting, beside the threats: a burden on the Ring-bearer.
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertEquals("Beacon of Minas Tirith", beacon.getBlueprint().getTitle());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(beacon));

		scn.FreepsUseCardAction(beacon);

		// The threat is the cost, paid before anything is chosen.
		assertEquals(3, scn.GetThreats());
		// "A companion": the two wounded companions, a Man and a Hobbit, and not the wounded ally; one must be healed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// One wound of his two is healed, and nobody else's.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetBurdens());
		// Three threats can be spotted now.  With four companions a fourth threat could still be added: it is
		// the spot that withholds the action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(beacon));
	}

	@Test
	public void NotOfferedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var beacon = scn.GetCurrentSite();

		// Two threats and two companions: fewer than 3 threats to spot, and no room for another.  A companion
		// is wounded, so the heal would have something to do.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertEquals("Beacon of Minas Tirith", beacon.getBlueprint().getTitle());
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(beacon));
	}
}
