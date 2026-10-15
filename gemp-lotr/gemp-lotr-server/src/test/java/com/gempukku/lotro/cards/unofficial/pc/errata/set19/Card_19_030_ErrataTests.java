package com.gempukku.lotro.cards.unofficial.pc.errata.set19;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_030_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "69_30");

					// Tales of three kinds, none of them [shire].
					// Thorin's Harp: a [dwarven] condition in the support area, cost 0; a maneuver ability used only on purpose
					put("harp1", "18_3");
					put("harp2", "18_3");
					// The Saga of Elendil: a [gondor] condition a [gondor] companion bears (vitality +1)
					put("saga", "1_114");
					// Scroll of Isildur: a [gondor] artifact in the support area; to play, spot a [gondor] Man
					put("scroll", "9_36");
					// Banner of the White Tree: a tale that is a Maneuver event, never in play
					put("banner1", "3_39");
					put("banner2", "3_39");
					put("lord", "1_21");          // Lord of Moria: a [dwarven] support-area condition that is no tale; it concerns Dwarves only

					put("sam", "1_311");          // Sam, Son of Hamfast: a [shire] Hobbit companion, cost 2, strength 3
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a [shire] companion with no race; an assignment ability only
					put("merry", "103_85");       // Merry, Master Holbytla: a Hobbit companion who is [rohan]
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, strength 8
					put("gaffer", "1_291");       // The Gaffer, Sam's Father: a [shire] Hobbit who is an ally
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
		 * Game Text: <b>Fellowship.</b>
		 * 	Pippin is strength +1 for each tale you can spot (limit X, where X is the number of [shire] companions
		 * 	you can spot).
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
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void StrengthPlus1ForEachTaleInPlayOfWhateverCultureOrType() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var harp = scn.GetFreepsCard("harp1");
		var saga = scn.GetFreepsCard("saga");
		var scroll = scn.GetFreepsCard("scroll");
		var bannerInHand = scn.GetFreepsCard("banner1");
		var bannerInDiscard = scn.GetFreepsCard("banner2");
		var lord = scn.GetFreepsCard("lord");

		// Four [shire] companions with Frodo, so the limit is out of the way: 4.
		scn.MoveCompanionsToTable(pippin, sam, smeagol, aragorn);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(harp, saga, scroll, bannerInHand);
		scn.MoveCardsToDiscard(bannerInDiscard);

		scn.StartGame();

		// A condition that is no tale in play, a tale in hand and a tale in the discard pile: nothing to spot.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));

		// A [dwarven] tale in the support area.
		scn.FreepsPlayCard(harp);
		assertInZone(Zone.SUPPORT, harp);
		assertEquals(4, scn.GetStrength(pippin));

		// A [gondor] tale borne by Aragorn, the only [gondor] companion.
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, aragorn);
		assertEquals(5, scn.GetStrength(pippin));

		// A tale that is an artifact.
		scn.FreepsPlayCard(scroll);
		assertInZone(Zone.SUPPORT, scroll);
		assertEquals(6, scn.GetStrength(pippin));

		// Pippin alone.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInZone(Zone.HAND, bannerInHand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusIsLimitedToTheNumberOfShireCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gaffer = scn.GetFreepsCard("gaffer");
		var harp1 = scn.GetFreepsCard("harp1");
		var harp2 = scn.GetFreepsCard("harp2");
		var saga = scn.GetFreepsCard("saga");
		var scroll = scn.GetFreepsCard("scroll");

		// Four tales in play. The [shire] companions are Frodo and Pippin himself; beside them a Hobbit companion
		// who is [rohan] (Merry), a companion who is neither (Aragorn) and a [shire] Hobbit who is an ally.
		scn.MoveCompanionsToTable(pippin, merry, aragorn);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToSupportArea(harp1, harp2, scroll);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		// 4 tales, limit 2.
		assertEquals(5, scn.GetStrength(pippin));

		// A [shire] companion who is no Hobbit: limit 3.
		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsRefreshDecision();
		assertEquals(6, scn.GetStrength(pippin));

		// Sam is played from hand: limit 4, and there are 4 tales.
		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(7, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
