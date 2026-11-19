package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_04_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "4_73");

					// What Legolas counts, and what he must not.
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit, cost 1; strength +2 with a weapon
					put("trollslayer", "103_31"); // Pippin, Trollslayer: unbound [gondor] Hobbit, cost 2; to play, spot a [gondor] companion
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit who is Ring-bound by rule
					put("bounder", "1_286");    // Bounder: a Hobbit ally, which can never be unbound
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man; a maneuver ability only
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion

					// The Shadow player's cards.
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc minion, cost 3; +2 strength with a weapon
					put("curse", "1_241");      // Curse From Mordor: Shadow event, cost 0; spot a [sauron] Orc to discard an [elven] condition
					put("hate", "1_250");       // Hate: Maneuver event, cost 0
					put("fear", "1_278");       // Strength Born of Fear: Shadow condition, cost 1, plays to the support area
					put("enraged", "1_254");    // Mordor Enraged: Shadow condition, cost 0, plays on a [sauron] minion
					put("scimitar", "1_269");   // Orc Scimitar: Shadow possession, cost 1, borne by a [sauron] Orc

					// Free Peoples cards of the types Legolas names.
					put("sorry", "1_312");      // Sorry About Everything: Fellowship event, cost 0
					put("songs", "1_61");       // Songs of the Blessed Realm: [elven] condition, cost 1, support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Legolas, Dauntless Hunter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Archer</b>.<br>The twilight cost of each Shadow event and Shadow condition is +1 for each unbound Hobbit you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Dauntless Hunter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void ShadowEventsAndConditionsCostOneMoreForEachUnboundHobbitAndNothingElseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sorry = scn.GetFreepsCard("sorry");
		var songs = scn.GetFreepsCard("songs");
		scn.MoveCompanionsToTable(legolas, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(merry, trollslayer, sorry, songs);

		var curse = scn.GetShadowCard("curse");
		var hate = scn.GetShadowCard("hate");
		var fear = scn.GetShadowCard("fear");
		var enraged = scn.GetShadowCard("enraged");
		var scimitar = scn.GetShadowCard("scimitar");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCardsToHand(curse, hate, fear, enraged, scimitar, swordsman);

		scn.StartGame();

		// Frodo and Sam are Ring-bound Hobbits, the Bounder a Hobbit ally, Legolas and Aragorn unbound companions
		// who are not Hobbits: none counts, and every card in hand is at its printed cost.
		assertEquals(0, scn.GetTwilightCost(curse));
		assertEquals(0, scn.GetTwilightCost(hate));
		assertEquals(1, scn.GetTwilightCost(fear));
		assertEquals(0, scn.GetTwilightCost(enraged));
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertEquals(3, scn.GetTwilightCost(swordsman));
		assertEquals(0, scn.GetTwilightCost(sorry));
		assertEquals(1, scn.GetTwilightCost(songs));

		// One unbound Hobbit, a [shire] one.
		scn.FreepsPlayCard(merry);
		assertEquals(1, scn.GetTwilightCost(curse));
		assertEquals(1, scn.GetTwilightCost(hate));
		assertEquals(2, scn.GetTwilightCost(fear));
		assertEquals(1, scn.GetTwilightCost(enraged));
		// Not a Shadow possession or minion, nor a Free Peoples event or condition.
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertEquals(3, scn.GetTwilightCost(swordsman));
		assertEquals(0, scn.GetTwilightCost(sorry));
		assertEquals(1, scn.GetTwilightCost(songs));

		// Two unbound Hobbits: the second is a [gondor] one, played by spotting Aragorn.
		scn.FreepsPlayCard(trollslayer);
		assertInZone(Zone.FREE_CHARACTERS, trollslayer);
		assertEquals(2, scn.GetTwilightCost(curse));
		assertEquals(2, scn.GetTwilightCost(hate));
		assertEquals(3, scn.GetTwilightCost(fear));
		assertEquals(2, scn.GetTwilightCost(enraged));
		assertEquals(1, scn.GetTwilightCost(scimitar));
		assertEquals(3, scn.GetTwilightCost(swordsman));
		assertEquals(0, scn.GetTwilightCost(sorry));
		assertEquals(1, scn.GetTwilightCost(songs));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerPaysTheRaisedCostForAShadowConditionAndAShadowEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("legolas", "merry", "trollslayer");

		var swordsman = scn.GetShadowCard("swordsman");
		var ted = scn.GetShadowCard("ted");
		var curse = scn.GetShadowCard("curse");
		var fear = scn.GetShadowCard("fear");
		var enraged = scn.GetShadowCard("enraged");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(curse, fear, enraged, scimitar);

		scn.StartGame();
		// A Hobbit minion is no unbound Hobbit: Merry and Pippin, Trollslayer still make 2.
		scn.MoveMinionsToTable(swordsman, ted);
		scn.FreepsPass();

		// 4 companions + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(fear);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(3, scn.GetTwilight()); // 1 printed + 2 for the Hobbits

		scn.ShadowPlayCard(curse);
		assertEquals(1, scn.GetTwilight()); // 0 printed + 2 for the Hobbits

		// Mordor Enraged would cost 0 + 2 and the pool has 1; the possession costs its printed 1.
		assertFalse(scn.ShadowPlayAvailable(enraged));
		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, swordsman);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
