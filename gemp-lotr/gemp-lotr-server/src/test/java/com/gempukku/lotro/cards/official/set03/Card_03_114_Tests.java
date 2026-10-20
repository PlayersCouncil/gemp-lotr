package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trolls", "3_114");
					put("gaffer", "1_291");     // The Gaffer, Sam's Father: Hobbit ally, vitality 2; a fellowship ability only
					put("bounder", "1_286");    // Bounder: Hobbit ally, vitality 2, home 2F; a skirmish ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally, no Hobbit; an archery ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion, no ally; his texts are not used here
					// Master Proudfoot: a Free Peoples card that adds twilight.  "Fellowship: If the twilight pool has
					// fewer than 3 twilight tokens, add (2) to reveal the top 3 cards of your draw deck. Take all
					// [shire] cards revealed into hand and discard the rest."
					put("proudfoot", "1_301");
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; "Shadow: Exert this minion to add (1)."
					put("axe", "3_55");         // Isengard Axe: possession, cost 0, on an [isengard] Orc; "When you play this weapon, add (1)."
					put("hollowing", "3_54");   // Hollowing of Isengard: [isengard] condition, support area; "Shadow: Discard this condition to add (3)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwilightScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trolls", "3_114");
					put("gaffer", "1_291");     // The Gaffer, Sam's Father: Hobbit ally, vitality 2; a fellowship ability only
					put("bounder", "1_286");    // Bounder: Hobbit ally, vitality 2; a skirmish ability only
					put("runner", "1_178");     // Goblin Runner: cost 1, site 4; "When you play this minion, you may add (2)", which the game adds without asking
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThreeMonstrousTrollsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Three Monstrous Trolls
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Tale</b>. Plays to your support area.<br><b>Response:</b> If a Shadow card is about to add any number of twilight tokens, exert a Hobbit ally to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trolls");

		assertEquals("Three Monstrous Trolls", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseExertsAHobbitAllyToPreventAShadowCardAddingTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwilightScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var gaffer = scn.GetFreepsCard("gaffer");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(trolls);
		scn.MoveAlliesToTable(gaffer, bounder);
		scn.MoveCardsToHand(runner);
		assertInZone(Zone.SUPPORT, trolls);

		scn.StartGame();
		scn.FreepsPass();

		// The move added 1 for Frodo and 2 for Ettenmoors: exactly the Runner's price there, 1 + 2 roaming.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowPlayAvailable(runner));

		scn.ShadowPlayCard(runner);
		// The Runner is paid for, and his (2) is about to be added.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trolls));

		scn.FreepsAcceptOptionalTrigger();
		// Frodo is a Hobbit and no ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaffer, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));

		scn.FreepsChooseCard(gaffer);
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// 0 and not 2: nothing was added.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, trolls);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionAMinionAndAConditionAddingTwilightAreEachAnsweredAndAnotherHobbitAllyMayPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var trolls = scn.GetFreepsCard("trolls");
		var gaffer = scn.GetFreepsCard("gaffer");
		var bounder = scn.GetFreepsCard("bounder");
		var orophin = scn.GetFreepsCard("orophin");
		var sam = scn.GetFreepsCard("sam");
		var forger = scn.GetShadowCard("forger");
		var axe = scn.GetShadowCard("axe");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(trolls, hollowing);
		scn.MoveAlliesToTable(gaffer, bounder, orophin);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(axe);
		assertInHand(axe);
		assertInZone(Zone.SUPPORT, hollowing);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.FreepsPass();

		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(forger));

		// A Shadow card that is no character: the Axe, cost 0, is about to add its (1).
		scn.ShadowPlayCard(axe);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trolls));
		scn.FreepsAcceptOptionalTrigger();

		// Either Hobbit ally may exert; a Hobbit companion and an ally who is no Hobbit may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaffer, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, orophin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bounder);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInZone(Zone.ATTACHED, axe);
		// 4 and not 5: the Axe added nothing.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion's ability: the Forger exerts to add (1), and the response is offered again.  Declined, the (1) is added.
		scn.ShadowUseCardAction(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trolls));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow card that is neither a character nor an item: the condition discards itself to add (3).
		assertTrue(scn.ShadowActionAvailable(hollowing));
		scn.ShadowUseCardAction(hollowing);
		assertInDiscard(hollowing);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trolls));
		scn.FreepsAcceptOptionalTrigger();
		// Bounder, exhausted by the Axe's answer, cannot pay: the Gaffer exerts unasked.
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(1, scn.GetWoundsOn(bounder));
		// 5 and not 8: the (3) was not added.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwilightAddedByFreePeoplesCardsOrByTheMoveIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var bounder = scn.GetFreepsCard("bounder");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveAlliesToTable(bounder, proudfoot);
		scn.MoveCardsToHand(trolls, sam);
		assertInHand(trolls, sam);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.FreepsPlayCard(trolls);
		assertInZone(Zone.SUPPORT, trolls);

		// Sam's play puts his 2 in the pool: a Free Peoples card's twilight is not answered, though Bounder could exert.
		scn.FreepsPlayCard(sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(trolls));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Nor is Master Proudfoot's (2), added by a Free Peoples card's ability.
		assertTrue(scn.FreepsActionAvailable(proudfoot));
		scn.FreepsUseCardAction(proudfoot);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(trolls));
		assertEquals(4, scn.GetTwilight());
		scn.DismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The move's twilight is added by no card: 4 + 2 companions + 2 for Ettenmoors.
		scn.FreepsPass();
		assertEquals(8, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithItsOnlyHobbitAllyExhaustedTheResponseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var bounder = scn.GetFreepsCard("bounder");
		var orophin = scn.GetFreepsCard("orophin");
		var sam = scn.GetFreepsCard("sam");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(trolls);
		scn.MoveAlliesToTable(bounder, orophin);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		// Bounder, vitality 2, cannot exert; Sam and Orophin, who could, cannot pay.
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetVitality(bounder));
		scn.FreepsPass();

		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(forger));
		scn.ShadowUseCardAction(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(trolls));
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
