package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_165_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("berserker", "15_165");
					put("berserker2", "15_165");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion, vitality 4; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4; a maneuver ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion, vitality 3; a skirmish ability
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, vitality 4; a standing strength bonus only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] ally; a fellowship ability used only on purpose
					put("cloak", "1_42");       // Elven Cloak: [elven] possession on a companion; the minion archery total -1 only
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] Shadow minion; a regroup ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MercilessBerserkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Merciless Berserker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Shadow:</b> Exert this minion to exert X companions and add X threats, where X is the number of Free Peoples cultures you spot over 2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("berserker");

		assertEquals("Merciless Berserker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHimToExertXCompanionsAndAddXThreatsForEachFreePeoplesCultureSpottedOver2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var berserker2 = scn.GetShadowCard("berserker2");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		var cloak = scn.GetFreepsCard("cloak");
		// Five Free Peoples cultures, each by a card of another kind: [shire] (Frodo and Sam), [gondor] (Aragorn
		// and Boromir), [gandalf] (a companion), [dwarven] (an ally) and [elven] (a possession on Aragorn).  The
		// [gollum] minion is a Shadow card and shows no Free Peoples culture.
		scn.MoveCompanionsToTable(sam, aragorn, boromir, gandalf);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();
		// Sam is exhausted: vitality 4, 3 wounds.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(berserker, berserker2, gollum);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(berserker2));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);
		// The cost: this copy exerts, the other does not.
		assertEquals(1, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetWoundsOn(berserker2));

		// The cultures spotted: none up to the 5 there are, filled in with all of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(5), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("5");

		// X is 5 - 2 = 3: three companions, chosen by the Shadow player; not the exhausted Sam, nor the ally.
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, boromir, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, grimir, gollum, berserker, berserker2));
		scn.ShadowChooseCards(aragorn, boromir, gandalf);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other copy names 4 of the 5: X is 2.
		assertTrue(scn.ShadowActionAvailable(berserker2));
		scn.ShadowUseCardAction(berserker2);
		assertEquals(1, scn.GetWoundsOn(berserker2));
		scn.ShadowChoose("4");
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(frodo, aragorn);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// 3 + 2, with five companions to hold them.
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoFreePeoplesCulturesTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// [shire] and [gondor] only, beside a second [gondor] companion and the [gollum] minion.
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker, gollum);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(berserker));
		assertEquals(0, scn.GetThreats());
		// It has a cost, so it is offered though X can be no more than 0.
		assertTrue(scn.ShadowActionAvailable(berserker));
		scn.ShadowUseCardAction(berserker);
		assertEquals(1, scn.GetWoundsOn(berserker));

		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// 2 - 2 = 0: no companion is exerted and no threat added.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var berserker = scn.GetShadowCard("berserker");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(sam, aragorn, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		// Vitality 2, 1 wound: exhausted.
		scn.AddWoundsToChar(berserker, 1);
		assertEquals(1, scn.GetWoundsOn(berserker));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(berserker));
		assertEquals(0, scn.GetThreats());
	}
}
