package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wake", "17_45");

					put("brand", "17_47");      // Primitive Brand: [men] possession, a hand weapon borne by a [men] minion; bearer is strength +2
					put("armor", "17_41");      // Ceremonial Armor: [men] possession played to the support area; a response to a wound, not met here
					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; a skirmish ability never used here
					put("haradrim", "11_74");   // Detachment of Haradrim: [men] Man, strength 12, vitality 3; a maneuver ability never used here
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, strength 7; a [men] minion to lie stacked on a possession
					put("hillsman2", "17_50");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, strength 8, vitality 2; a maneuver ability only
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), strength 8.
					// "Each [men] minion bearing a possession is strength +2" (nobody bears one where he is laid); a skirmish ability never used here.
					put("saruman", "17_48");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, no text
					put("hides", "4_19");       // Hides: a [dunland] possession in the support area; it answers [dunland] Men about to take wounds
					put("companies", "12_58");  // Countless Companies: a [men] condition in the support area; it answers [men] possessions discarded
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntheWildMensWakeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: In the Wild Men's Wake
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a [men] possession in your support area and a [men] minion. Make that minion strength +1 for each minion stacked on that possession. You may discard that possession to make that minion <b>damage</b> <b>+1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("wake");

		assertEquals("In the Wild Men's Wake", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAMenPossessionInTheSupportAreaAndNotOneThatAMinionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var brand = scn.GetShadowCard("brand");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var haradrim = scn.GetShadowCard("haradrim");
		var hillsman = scn.GetShadowCard("hillsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wake);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(haradrim);
		// The only [men] possession in play is borne by a minion: it is in no support area.
		scn.AttachCardsTo(haradrim, brand);
		assertAttachedTo(brand, haradrim);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, haradrim);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertAttachedTo(brand, haradrim);
		assertFalse(scn.ShadowPlayAvailable(wake));

		// Two [men] possessions lying in the support area, one with a minion stacked on it and one with none.
		scn.MoveCardsToSupportArea(armor, oath);
		scn.StackCardsOn(armor, hillsman);
		assertInZone(Zone.SUPPORT, armor, oath);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wake));

		// 12 + 2 for the Primitive Brand
		assertEquals(14, scn.GetStrength(haradrim));

		scn.ShadowPlayCard(wake);
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brand));
		scn.ShadowChooseCard(armor);

		// The Detachment is the only [men] minion: it is strength +1 for the one minion stacked on the Armor.
		assertEquals(15, scn.GetStrength(haradrim));
		// The possession need not be discarded.
		assertEquals(0, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		scn.ShadowChooseNo();
		assertInZone(Zone.SUPPORT, armor, oath);
		assertAttachedTo(brand, haradrim);
		assertEquals(15, scn.GetStrength(haradrim));
		assertEquals(0, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		assertInZone(Zone.DISCARD, wake);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMenConditionAndAPossessionOfAnotherCultureInTheSupportAreaAreNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var armor = scn.GetShadowCard("armor");
		var hides = scn.GetShadowCard("hides");
		var companies = scn.GetShadowCard("companies");
		var haradrim = scn.GetShadowCard("haradrim");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wake);
		scn.MoveCardsToSupportArea(hides, companies);
		assertInHand(wake);
		assertInZone(Zone.SUPPORT, hides, companies);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(haradrim);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, haradrim);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wake));

		scn.MoveCardsToSupportArea(armor);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wake));
	}

	@Test
	public void WithNoMenMinionToSpotItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var armor = scn.GetShadowCard("armor");
		var brigand = scn.GetShadowCard("brigand");
		var hillsman = scn.GetShadowCard("hillsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wake);
		scn.MoveCardsToSupportArea(armor);
		assertInHand(wake);
		assertInZone(Zone.SUPPORT, armor);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Armor itself is a [men] card, but no minion; the Brigand is a minion of another culture.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wake));

		scn.MoveMinionsToTable(hillsman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wake));
	}

	@Test
	public void ThatMinionIsStrengthPlusOneForEachMinionOnThatPossessionAndDamagePlusOneWhenItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var warrior = scn.GetShadowCard("warrior");
		var haradrim = scn.GetShadowCard("haradrim");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wake);
		scn.MoveCardsToSupportArea(armor, oath);
		scn.StackCardsOn(armor, hillsman, hillsman2);
		scn.StackCardsOn(oath, warrior);
		assertInHand(wake);
		assertEquals(2, scn.GetStackedCards(armor).size());
		assertEquals(1, scn.GetStackedCards(oath).size());

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(haradrim, saruman, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, haradrim);
		// Saruman and the Brigand are in no skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(haradrim));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(0, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(wake));
		scn.ShadowPlayCard(wake);

		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, oath));
		scn.ShadowChooseCard(armor);
		// A [men] minion of either race, in the skirmish or in none; not a minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(haradrim, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(haradrim);

		// Two minions on the Armor (the one on the Oath does not count): 12 + 2.
		assertEquals(14, scn.GetStrength(haradrim));
		assertEquals(8, scn.GetStrength(saruman));
		scn.ShadowChooseYes();

		assertInDiscard(armor, wake);
		assertInZone(Zone.SUPPORT, oath);
		assertEquals(1, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		// The count was taken as the event resolved: the bonus stays with the Armor gone.
		assertEquals(14, scn.GetStrength(haradrim));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionInALaterSkirmishMayBeChosenAndBothBonusesLastOnlyThisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wake = scn.GetShadowCard("wake");
		var oath = scn.GetShadowCard("oath");
		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var haradrim = scn.GetShadowCard("haradrim");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(wake);
		scn.MoveCardsToSupportArea(oath);
		scn.StackCardsOn(oath, hillsman);
		assertInHand(wake);
		assertEquals(1, scn.GetStackedCards(oath).size());

		scn.StartGame();
		scn.SetTwilight(3);
		scn.MoveMinionsToTable(haradrim, warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, haradrim}, new PhysicalCardImpl[]{boromir, warrior});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(warrior));
		scn.ShadowPlayCard(wake);
		// The Oath is the one [men] possession in the support area, taken unasked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(haradrim, warrior));
		scn.ShadowChooseCard(warrior);
		scn.ShadowChooseYes();

		// One minion on the Oath: 8 + 1, and damage +1, in the skirmish the event was played in.
		assertInDiscard(oath);
		assertEquals(9, scn.GetStrength(warrior));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(boromir);

		// Each skirmish is a phase of its own: both are gone in the Warrior's own skirmish.
		assertEquals(8, scn.GetStrength(warrior));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
