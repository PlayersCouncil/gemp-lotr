package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_073_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "54_73");

					// What Legolas counts, and what he must not.
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound Hobbit; strength +2 with a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: unbound Hobbit; protects [shire] tales
					put("fatty", "18_107");     // Fredegar Bolger: unbound Hobbit; strength +3 in region 1 beside 2 other [shire] companions
					put("trollslayer", "103_31"); // Pippin, Trollslayer: unbound [gondor] Hobbit, so a Hobbit need not be [shire]; his only text is a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit who is Ring-bound by rule
					put("bounder", "1_286");    // Bounder: a Hobbit ally, which can never be unbound
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man, given [shire] in one test

					// The Shadow player's cards.
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc minion, cost 3, site 6; +2 strength with a weapon
					put("curse", "1_241");      // Curse From Mordor: Shadow event, cost 0; spot a [sauron] Orc to discard an [elven] condition
					put("curse2", "1_241");
					put("hate", "1_250");       // Hate: Maneuver event, cost 0; exert a [sauron] Orc to wound a companion (except the Ring-bearer)
					put("enraged", "1_254");    // Mordor Enraged: Shadow condition, cost 0, plays on a [sauron] minion
					put("fear", "1_278");       // Strength Born of Fear: Shadow condition, cost 1, plays to the support area
					put("scimitar", "1_269");   // Orc Scimitar: Shadow possession, cost 1, borne by a [sauron] Orc

					// Free Peoples cards of the types Legolas names.
					put("sorry", "1_312");      // Sorry About Everything: Fellowship event, cost 0; exert a Hobbit companion to remove a burden
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
		 * Game Text: Archer.
		 * Each time a Shadow event or Shadow condition is played, remove (1) for each unbound Hobbit you can spot.
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
	public void LegolasIsPlayedFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(legolas);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(legolas));

		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachShadowEventOrConditionRemovesOneForTheOneUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(legolas, merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		var swordsman = scn.GetShadowCard("swordsman");
		var curse = scn.GetShadowCard("curse");
		var enraged = scn.GetShadowCard("enraged");
		var fear = scn.GetShadowCard("fear");
		var hate = scn.GetShadowCard("hate");
		scn.MoveCardsToHand(curse, enraged, fear, hate);

		scn.StartGame();
		// Aragorn is of [shire] as well as [gondor], but no Hobbit. Frodo and Sam are Ring-bound Hobbits, the
		// Bounder a Hobbit ally, Legolas and Aragorn unbound companions who are not Hobbits: Merry alone counts.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.SHIRE));
		scn.MoveMinionsToTable(swordsman);
		scn.FreepsPass();

		// 5 companions + 2 for Ettenmoors
		assertEquals(7, scn.GetTwilight());

		scn.ShadowPlayCard(curse);
		assertEquals(6, scn.GetTwilight()); // 0 to play + 1 for Merry

		scn.ShadowPlayCard(enraged);
		assertAttachedTo(enraged, swordsman);
		assertEquals(5, scn.GetTwilight()); // 0 to play + 1 for Merry

		// A third Shadow card in the same phase removes another 1.
		scn.ShadowPlayCard(fear);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(3, scn.GetTwilight()); // 1 to play + 1 for Merry
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(hate);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetTwilight()); // 0 to play + 1 for Merry
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TwoUnboundHobbitsRemoveTwoEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Merry is a [shire] Hobbit and Pippin, Trollslayer a [gondor] one: both count.
		scn.MoveCompanionsToTable("legolas", "merry", "trollslayer");

		var swordsman = scn.GetShadowCard("swordsman");
		var curse = scn.GetShadowCard("curse");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(curse, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.FreepsPass();

		// 4 companions + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(fear);
		assertEquals(3, scn.GetTwilight()); // 1 to play + 2 for Merry and Pippin, Trollslayer

		scn.ShadowPlayCard(curse);
		assertEquals(1, scn.GetTwilight()); // 0 to play + 2 for Merry and Pippin, Trollslayer
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThreeUnboundHobbitsRemoveThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("legolas", "merry", "pippin", "fatty");

		var swordsman = scn.GetShadowCard("swordsman");
		var curse = scn.GetShadowCard("curse");
		scn.MoveCardsToHand(curse);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.FreepsPass();

		// 5 companions + 2 for Ettenmoors
		assertEquals(7, scn.GetTwilight());

		scn.ShadowPlayCard(curse);
		assertEquals(4, scn.GetTwilight()); // 0 to play + 3 for Merry, Pippin and Fredegar
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowMinionOrPossessionAndFreePeoplesEventOrConditionRemoveNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable("legolas");
		scn.MoveCompanionsToTable(merry);
		var sorry = scn.GetFreepsCard("sorry");
		var songs = scn.GetFreepsCard("songs");
		scn.MoveCardsToHand(sorry, songs);

		var swordsman = scn.GetShadowCard("swordsman");
		var scimitar = scn.GetShadowCard("scimitar");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(swordsman, scimitar, fear);

		scn.StartGame();
		scn.SetTwilight(3);

		scn.FreepsPlayCard(sorry);
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetTwilight()); // 0 to play, and nothing removed

		scn.FreepsPlayCard(songs);
		assertInZone(Zone.SUPPORT, songs);
		assertEquals(4, scn.GetTwilight()); // 1 added to play, and nothing removed

		scn.FreepsPass();
		// 4 + 3 companions + 2 for Ettenmoors
		assertEquals(9, scn.GetTwilight());

		scn.ShadowPlayCard(swordsman);
		assertEquals(4, scn.GetTwilight()); // 3 to play + 2 roaming, and nothing removed

		scn.ShadowPlayCard(scimitar);
		assertAttachedTo(scimitar, swordsman);
		assertEquals(3, scn.GetTwilight()); // 1 to play, and nothing removed

		// A Shadow condition then removes 1 for Merry.
		scn.ShadowPlayCard(fear);
		assertEquals(1, scn.GetTwilight()); // 1 to play + 1 for Merry
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRemovalIsNoCostSoAZeroCostShadowCardMayBePlayedWithAnEmptyPool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("legolas", "merry");

		var swordsman = scn.GetShadowCard("swordsman");
		var curse = scn.GetShadowCard("curse");
		var enraged = scn.GetShadowCard("enraged");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(curse, enraged, fear);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.FreepsPass();

		scn.SetTwilight(1);
		scn.ShadowPlayCard(fear);
		assertEquals(0, scn.GetTwilight()); // 1 to play, and nothing left for Merry's 1

		assertTrue(scn.ShadowPlayAvailable(curse));
		assertTrue(scn.ShadowPlayAvailable(enraged));

		scn.ShadowPlayCard(curse);
		assertInDiscard(curse);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
