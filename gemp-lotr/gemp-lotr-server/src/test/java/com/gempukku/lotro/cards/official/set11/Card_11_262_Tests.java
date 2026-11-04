package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_262_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizen", "11_115");     // Denizen of Khazad-dûm: [orc] Orc, vitality 2; stronger only at an underground site
					put("halftroll", "14_12");    // Half-troll of Far Harad: [orc] minion who is no Orc (a Half-troll), vitality 2; its trigger is at the start of the assignment phase
					put("hound", "7_192");        // Morgul Hound: a [wraith] Orc, vitality 2; its text needs 2 wounds on the Ring-bearer

					// Cards to hold in a hand; their texts are never used here.
					put("farin", "1_11");
					put("boromir", "1_97");
					put("gimli", "1_13");
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");       // Fangorn Glade: its text answers a move to it; the game starts there
					put("tower", "11_262");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site4", "18_138");
					put("site5", "17_148");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void WatchtowerofCirithUngolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Watch-tower of Cirith Ungol
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. <b>Maneuver:</b> Exert your [orc] minion to discard a card at random from the Free Peoples player's hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("tower");

		assertEquals("Watch-tower of Cirith Ungol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ManeuverActionExertsAShadowOrcMinionToDiscardOneCardAtRandomFromTheFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var hound = scn.GetShadowCard("hound");
		var shadowFarin = scn.GetShadowCard("farin");
		var shadowBoromir = scn.GetShadowCard("boromir");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var tower = scn.GetShadowSite("tower");
		scn.ShadowChooseCardBPFromSelection(tower);
		assertSame(tower, scn.GetCurrentSite());

		scn.MoveMinionsToTable(denizen, halftroll, hound);
		scn.MoveCardsToHand(farin, boromir, gimli);
		scn.MoveCardsToHand(shadowFarin, shadowBoromir);
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(shadowFarin, shadowBoromir);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(hound));

		scn.SkipToPhase(Phase.MANEUVER);

		// "Your": the Free Peoples player, asked for maneuver actions with the [orc] minions on the table, is not offered it.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tower));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tower));
		scn.ShadowUseCardAction(tower);

		// An [orc] Orc and an [orc] Half-troll are offered; a [wraith] Orc is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(hound));
		// One card of the three left the Free Peoples hand for his discard pile; none went to his deck.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		// The Shadow player's own hand is untouched.
		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(shadowFarin, shadowBoromir);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesHandEmptyTheActionIsOfferedAndTheExertionPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var tower = scn.GetShadowSite("tower");
		scn.ShadowChooseCardBPFromSelection(tower);
		scn.MoveMinionsToTable(denizen);
		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(denizen));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(tower));
		scn.ShadowUseCardAction(tower);

		// The lone [orc] minion exerts unasked; there is nothing to discard.
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheOnlyOrcMinionExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var denizen = scn.GetShadowCard("denizen");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		var tower = scn.GetShadowSite("tower");
		scn.ShadowChooseCardBPFromSelection(tower);
		scn.MoveMinionsToTable(denizen, hound);
		scn.MoveCardsToHand(boromir);
		// Vitality 2 with 1 wound: the Denizen cannot exert. The Hound could, but is no [orc] minion.
		scn.AddWoundsToChar(denizen, 1);
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tower));
		assertEquals(1, scn.GetFreepsHandCount());
	}
}
