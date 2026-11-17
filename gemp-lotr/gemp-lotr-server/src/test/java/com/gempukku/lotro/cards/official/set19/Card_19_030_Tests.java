package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "19_30");

					// Tales of three kinds, none of them [shire].
					// Thorin's Harp: a [dwarven] condition in the support area, cost 0; a maneuver ability used only on purpose
					put("harp", "18_3");
					// The Saga of Elendil: a [gondor] condition a [gondor] companion bears (vitality +1)
					put("saga", "1_114");
					// Scroll of Isildur: a [gondor] artifact in the support area; to play, spot a [gondor] Man
					put("scroll", "9_36");
					// Banner of the White Tree: a tale that is a Maneuver event, never in play
					put("banner1", "3_39");
					put("banner2", "3_39");
					put("lord", "1_21");          // Lord of Moria: a [dwarven] support-area condition that is no tale; it concerns Dwarves only

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Pippin, Steadfast Friend
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Fellowship</b>.<br>Pippin is strength +1 for each tale you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Steadfast Friend", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void StrengthPlus1ForEachTaleInPlayOfWhateverCultureOrType() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var harp = scn.GetFreepsCard("harp");
		var saga = scn.GetFreepsCard("saga");
		var scroll = scn.GetFreepsCard("scroll");
		var bannerInHand = scn.GetFreepsCard("banner1");
		var bannerInDiscard = scn.GetFreepsCard("banner2");
		var lord = scn.GetFreepsCard("lord");

		// Frodo and Pippin are the only [shire] companions: a bonus capped at that count would stop at +2.
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(harp, saga, scroll, bannerInHand);
		scn.MoveCardsToDiscard(bannerInDiscard);

		scn.StartGame();

		// A condition that is no tale in play, a tale in hand and a tale in the discard pile: nothing to spot.
		assertInZone(Zone.SUPPORT, lord);
		assertInHand(bannerInHand);
		assertInZone(Zone.DISCARD, bannerInDiscard);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));

		// A [dwarven] tale in the support area.
		scn.FreepsPlayCard(harp);
		assertInZone(Zone.SUPPORT, harp);
		assertEquals(4, scn.GetStrength(pippin));

		// A [gondor] tale borne by Aragorn, the only [gondor] companion.
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, aragorn);
		assertEquals(5, scn.GetStrength(pippin));

		// A tale that is an artifact: three tales, +3, past the two [shire] companions.
		scn.FreepsPlayCard(scroll);
		assertInZone(Zone.SUPPORT, scroll);
		assertEquals(6, scn.GetStrength(pippin));

		// Pippin alone.
		assertEquals(8, scn.GetStrength(aragorn));
		assertInHand(bannerInHand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
