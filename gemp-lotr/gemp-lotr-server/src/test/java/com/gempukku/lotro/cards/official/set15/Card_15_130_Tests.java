package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horseman", "15_130");
					put("horseman2", "15_130");  // not unique: a second copy
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, valiant, no other text
					// Aragorn, Last Hope of Men: a valiant Man of [gondor]; his start-of-turn draw needs another valiant companion
					put("lasthope", "102_16");
					put("eored", "12_113");      // Éored Warrior: [rohan] Man companion, not valiant; strength +3 only at a battleground or plains
					put("lorien", "54_76");      // Lórien Guardian: valiant [elven] Elf companion; a regroup ability used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, cost 4, not valiant
					put("theoden", "4_292");     // Théoden, Son of Thengel: "Regroup: Spot a [rohan] ally and exert Théoden to liberate a site"
					put("guma", "4_277");        // Guma, Plains Farmer: the [rohan] ally; discarded only if an opponent controls site 4 of the Towers block
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HorsemanoftheNorthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Horseman of the North
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot a valiant Man, this companion's twilight cost is -1.
		 * 	This companion is strength +2 while no opponent controls a site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horseman");

		assertEquals("Horseman of the North", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void TwilightCostIsOneLessOnceAValiantManIsPlayedAndNotForAValiantElfOrAManWhoIsNotValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var horseman2 = scn.GetFreepsCard("horseman2");
		var guard = scn.GetFreepsCard("guard");
		var eored = scn.GetFreepsCard("eored");
		var lorien = scn.GetFreepsCard("lorien");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eored, lorien);
		scn.MoveCardsToHand(horseman, horseman2, guard, aragorn);

		scn.StartGame();

		// A Man who is not valiant and a valiant Elf: no valiant Man to spot.
		assertFalse(scn.HasKeyword(eored, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(lorien, Keyword.VALIANT));
		assertEquals(3, scn.GetTwilightCost(horseman));
		assertEquals(3, scn.GetTwilightCost(horseman2));
		assertEquals(4, scn.GetTwilightCost(aragorn));

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(2, scn.GetTwilight());

		// Each copy is -1 for itself, not -2 for both; another companion in hand is not touched.
		assertEquals(2, scn.GetTwilightCost(horseman));
		assertEquals(2, scn.GetTwilightCost(horseman2));
		assertEquals(4, scn.GetTwilightCost(aragorn));

		scn.FreepsPlayCard(horseman);
		assertInZone(Zone.FREE_CHARACTERS, horseman);
		assertEquals(4, scn.GetTwilight()); // 2 for the Household Guard + 3 - 1 for the Horseman
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AValiantManOfAnotherCultureLowersTheTwilightCostToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var lasthope = scn.GetFreepsCard("lasthope");
		scn.MoveCompanionsToTable(lasthope);
		scn.MoveCardsToHand(horseman);

		scn.StartGame();

		assertTrue(scn.HasKeyword(lasthope, Keyword.VALIANT));
		assertEquals(Culture.GONDOR, lasthope.getBlueprint().getCulture());
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(horseman));

		scn.FreepsPlayCard(horseman);
		assertInZone(Zone.FREE_CHARACTERS, horseman);
		assertEquals(2, scn.GetTwilight()); // 3 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthPlusTwoWhileNoOpponentControlsASiteHisOwnPlayersSiteNotCounting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var horseman2 = scn.GetFreepsCard("horseman2");
		var guard = scn.GetFreepsCard("guard");
		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(horseman, horseman2, guard, theoden);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();

		// Nobody controls a site: 5 + 2 each, not 5 + 4 (one copy's bonus is its own).
		assertEquals(7, scn.GetStrength(horseman));
		assertEquals(7, scn.GetStrength(horseman2));
		assertEquals(6, scn.GetStrength(guard));

		// Player 1's third turn, at site 3: sites 1 and 2 are behind both markers.
		scn.SkipToSite(3);
		// His own player controls a site: that is no opponent's.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(7, scn.GetStrength(horseman));
		assertEquals(7, scn.GetStrength(horseman2));

		// The opponent controls one.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(5, scn.GetStrength(horseman));
		assertEquals(5, scn.GetStrength(horseman2));
		assertEquals(6, scn.GetStrength(guard));

		// By legal play: Théoden liberates the site the opponent controls (the highest-numbered one an opponent
		// controls), and the bonus is back.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(7, scn.GetStrength(horseman));
		assertEquals(7, scn.GetStrength(horseman2));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
