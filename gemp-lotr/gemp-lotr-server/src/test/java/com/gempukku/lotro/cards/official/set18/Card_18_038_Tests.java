package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "18_38");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant, no other text
					put("smeagol", "6_45");     // Sméagol, Poor Creature: a Free Peoples [gollum] companion; a skirmish ability used only on purpose
					put("alliance", "1_49");    // The Last Alliance of Elves and Men: an [elven] condition on a [gondor] Man; +0 with no Elf
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a Shadow [gollum] minion; a regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("armory", "1_173");     // Goblin Armory: a [moria] support condition; answers only [moria] weapons and Orc wounds
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] minion with no text, cost 2
					put("urukarmory", "1_157"); // Uruk-hai Armory: an [isengard] support condition; does nothing with no Uruk-hai
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Aragorn, Heir to the Throne of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Hunter 2</b>.<br>While you can spot 3 Free Peoples cultures, Aragorn is <b>defender +1</b>.<br>While you can spot 2 Shadow cultures, Aragorn is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Heir to the Throne of Gondor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void DefenderPlusOneWhileThreeFreePeoplesCulturesAndPlusTwoWithTwoShadowCulturesToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guard);

		scn.StartGame();
		// Laid early, as they could be in play later this turn (Q-0228): the Shadow player's [gollum] minion.
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		// [gondor] Aragorn and [shire] Frodo: two Free Peoples cultures. Gollum's [gollum] is a Shadow card's, which
		// spots no Free Peoples culture (CR 4.2 "spot"), and one Shadow culture is not two.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(guard);

		// [rohan] is the third.
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));

		// Gollum's [gollum] and the Troop's [moria]: two Shadow cultures, and the two bonuses add up.
		scn.MoveMinionsToTable(troop);
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DEFENDER));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CulturesOfCardsThatAreNoCharactersAreSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var alliance = scn.GetFreepsCard("alliance");
		var troop = scn.GetShadowCard("troop");
		var urukarmory = scn.GetShadowCard("urukarmory");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		// An [elven] condition he bears is the third Free Peoples culture.
		scn.AttachCardsTo(aragorn, alliance);
		assertAttachedTo(alliance, aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		// A [moria] minion alone is one Shadow culture; an [isengard] condition in the support area is the second.
		scn.MoveMinionsToTable(troop);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.MoveCardsToSupportArea(urukarmory);
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
	}

	@Test
	public void ASecondShadowCultureBroughtInByAPlayMakesHimDefenderAndAFreePeoplesGollumCardIsNoShadowCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(brigand);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(armory);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// [gondor], [shire] and Sméagol's [gollum]: three Free Peoples cultures, +1. Two [moria] cards are one Shadow
		// culture, and Sméagol's [gollum] is a Free Peoples card's: still +1 only.
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.ShadowPlayAvailable(brigand));
		scn.ShadowPlayCard(brigand);

		// [dunland] is the second Shadow culture.
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(smeagol, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
