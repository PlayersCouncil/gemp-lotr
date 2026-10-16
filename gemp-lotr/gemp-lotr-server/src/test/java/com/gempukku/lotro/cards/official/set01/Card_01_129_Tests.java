package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_129_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("misadventure", "1_129");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4; no requirement to play
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2; no requirement to play
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, cost 0; a character who is no companion

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, vitality 3; damage +1 and nothing else
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability only ever used on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMisadventureofMrUnderhillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Misadventure of Mr. Underhill
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert an Uruk-hai. Plays to your support area.<br>The twilight cost of each companion played to a site which is not a sanctuary is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("misadventure");

		assertEquals("The Misadventure of Mr. Underhill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayAnUrukHaiOfEitherCultureExertsAndItGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var misadventure = scn.GetShadowCard("misadventure");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(misadventure);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.ShadowPlayAvailable(misadventure));

		scn.ShadowPlayCard(misadventure);
		// Uruk-hai of two cultures; not the [isengard] Orc, who could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertInZone(Zone.SUPPORT, misadventure);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var misadventure = scn.GetShadowCard("misadventure");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(misadventure);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, servant);
		scn.AddWoundsToChar(savage, 1);
		scn.SetTwilight(10);
		assertInHand(misadventure);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: Uruk Savage can still exert.
		assertTrue(scn.ShadowPlayAvailable(misadventure));

		scn.AddWoundsToChar(savage, 1);
		scn.ShadowRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted.  The [isengard] Orc beside it could exert.
		assertFalse(scn.ShadowPlayAvailable(misadventure));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertInHand(misadventure);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtASiteThatIsNoSanctuaryEachCompanionCostsTwoMoreAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var misadventure = scn.GetShadowCard("misadventure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var barliman = scn.GetFreepsCard("barliman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(aragorn, legolas, barliman, savage);

		// Site 1, Bag End, is no sanctuary.
		scn.StartGame();

		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(2, scn.GetTwilightCost(legolas));
		assertEquals(0, scn.GetTwilightCost(barliman));
		assertEquals(2, scn.GetTwilightCost(savage));

		scn.MoveCardsToSupportArea(misadventure);
		scn.FreepsRefreshDecision();

		// Companions of two cultures and races, each +2; an ally and a minion as printed.
		assertEquals(6, scn.GetTwilightCost(aragorn));
		assertEquals(4, scn.GetTwilightCost(legolas));
		assertEquals(0, scn.GetTwilightCost(barliman));
		assertEquals(2, scn.GetTwilightCost(savage));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(legolas);
		// 2 printed + 2
		assertEquals(4, scn.GetTwilight());
		assertInPlay(legolas);

		scn.FreepsPlayCard(barliman);
		// The ally adds his printed 0.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtASanctuaryACompanionCostsWhatIsPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var misadventure = scn.GetShadowCard("misadventure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(misadventure);

		scn.StartGame();

		// Site 2, Ettenmoors, is no sanctuary.
		scn.SkipToSite(2);
		scn.SetFreepsHand(aragorn, legolas);
		assertInZone(Zone.SUPPORT, misadventure);
		assertEquals(6, scn.GetTwilightCost(aragorn));
		assertEquals(4, scn.GetTwilightCost(legolas));

		// Site 3, Rivendell Valley, is a sanctuary.
		scn.SkipToSite(3);
		scn.SetFreepsHand(aragorn, legolas);
		assertInZone(Zone.SUPPORT, misadventure);
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(2, scn.GetTwilightCost(legolas));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(aragorn);
		// 4 printed and nothing more
		assertEquals(pool + 4, scn.GetTwilight());
		assertInPlay(aragorn);

		// Site 4, Balin's Tomb, is no sanctuary.
		scn.SkipToSite(4);
		scn.SetFreepsHand(legolas);
		assertInZone(Zone.SUPPORT, misadventure);
		assertEquals(4, scn.GetTwilightCost(legolas));

		// Site 6, Galadriel's Glade, is the other sanctuary.
		scn.SkipToSite(6);
		scn.SetFreepsHand(legolas);
		assertInZone(Zone.SUPPORT, misadventure);
		assertEquals(2, scn.GetTwilightCost(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
