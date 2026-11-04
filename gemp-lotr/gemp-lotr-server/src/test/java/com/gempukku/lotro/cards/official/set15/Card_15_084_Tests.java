package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gasp", "15_84");
					put("harad1", "11_67");     // Archer of Harad: [men] archer, vitality 1
					put("harad2", "11_67");
					put("rider", "12_74");      // Mumak Rider: [men] archer, vitality 2; his text answers his own play only
					put("haradrim", "11_80");   // Ferocious Haradrim: [men] archer, vitality 3; his text answers a Free Peoples kill
					put("bowman", "2_60");      // Goblin Bowman: an archer of another culture, no other text
					put("hillman", "15_82");    // Grousing Hillman: a [men] Man who is no archer; his text needs a controlled site
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of vitality 4, to tell exhaust from exert twice
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LastGaspStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Last Gasp
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Wound 3 [men] archers to exhaust a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gasp");

		assertEquals("Last Gasp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WoundsThreeMenArchersOfThePlayersChoiceToExhaustACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gasp = scn.GetShadowCard("gasp");
		var harad1 = scn.GetShadowCard("harad1");
		var harad2 = scn.GetShadowCard("harad2");
		var rider = scn.GetShadowCard("rider");
		var haradrim = scn.GetShadowCard("haradrim");
		var bowman = scn.GetShadowCard("bowman");
		var hillman = scn.GetShadowCard("hillman");
		scn.MoveCardsToHand(gasp);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(harad1, harad2, rider, haradrim, bowman, hillman);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(harad1));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(gasp));

		scn.ShadowPlayCard(gasp);
		// Any three [men] archers; neither the [moria] archer nor the [men] Man who is no archer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(harad1, harad2, rider, haradrim));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bowman, hillman));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(harad1, harad2, rider);

		// A wound, not an exertion: the Archers of Harad, with 1 vitality, are wounded and killed.
		assertInZone(Zone.DISCARD, harad1, harad2);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(haradrim));
		assertEquals(0, scn.GetWoundsOn(bowman));
		assertEquals(0, scn.GetWoundsOn(hillman));

		// A companion, either of them; the ally is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(maggot));
		scn.ShadowChooseCard(aragorn);

		// Vitality 4: exhausted is 3 wounds, where exerting twice would be 2.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertInZone(Zone.DISCARD, gasp);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyTwoMenArchersThoughAnotherArcherAndAMenManCanBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gasp = scn.GetShadowCard("gasp");
		var rider = scn.GetShadowCard("rider");
		var haradrim = scn.GetShadowCard("haradrim");
		var bowman = scn.GetShadowCard("bowman");
		var hillman = scn.GetShadowCard("hillman");
		scn.MoveCardsToHand(gasp);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.HAND, gasp);
		scn.MoveMinionsToTable(rider, haradrim, bowman, hillman);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Three different archers must each take a wound: the Haradrim's 3 vitality does not make up for a third.
		assertInZone(Zone.HAND, gasp);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gasp));
	}

	@Test
	public void WithNoCompanionThatCanBeExhaustedItIsPlayedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gasp = scn.GetShadowCard("gasp");
		var rider = scn.GetShadowCard("rider");
		var haradrim = scn.GetShadowCard("haradrim");
		var harad1 = scn.GetShadowCard("harad1");
		scn.MoveCardsToHand(gasp);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		// Frodo, the only companion, is exhausted (vitality 4).
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(harad1, rider, haradrim);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(haradrim));
		assertTrue(scn.ShadowPlayAvailable(gasp));

		// An event whose target finds nothing is still played (F-0665): its three archers are wounded for nothing.
		scn.ShadowPlayCard(gasp);

		assertInZone(Zone.DISCARD, harad1, gasp);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(haradrim));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
