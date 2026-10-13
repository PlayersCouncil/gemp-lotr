package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("great", "10_1");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, damage +1, vitality 3; a skirmish ability only
					put("axe", "1_14");         // Gimli's Battle Axe: bearer must be Gimli, he is damage +1 (so +2); a trigger only when he wins a skirmish
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3, no damage bonus of his own; a fellowship ability only
					put("ring", "9_8");         // Ring of Guile: artifact, bearer must be a Dwarf, bearer is damage +1; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion of vitality 2 with no damage bonus
					put("huorn", "9_25");       // Huorn: a [gandalf] companion who is damage +1 and no Dwarf, vitality 4; a response that needs an Ent

					// Gimli, Counter of Foes: "Damage +1. For each wound on Gimli, he is strength +1 and damage +1."
					// Never on a table with the other Gimli.
					put("counter", "8_5");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, vitality 3; "Damage +1." is its whole text
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion with no race, cost 2, vitality 4; a regroup ability only
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition played to the support area for 1; a skirmish ability only
					put("troll", "8_102");      // Great Hill Troll: [sauron] Troll, cost 9, vitality 4; its trigger on being played is for sites 5K to 9K
					put("assassin", "8_95");    // Gorgoroth Assassin: [sauron] Orc, besieger, cost 2, vitality 2; an assignment ability only
					put("towers", "7_281");     // Great Siege-towers: [sauron] condition; "Shadow: Play a besieger to place a [sauron] token here."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatDayGreatHourStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Great Day, Great Hour
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If an opponent plays a minion, exert a Dwarf who is damage +X to exert that minion X times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("great");

		assertEquals("Great Day, Great Hour", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWhoIsDamagePlusTwoExertsToExertTheMinionJustPlayedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(gimli, guard, huorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(great);

		var gollum = scn.GetShadowCard("gollum");
		var patrol = scn.GetShadowCard("patrol");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(gollum, fear);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol);

		// A Shadow card that is no minion is played: nothing is offered.
		scn.ShadowPlayCard(fear);
		assertInZone(Zone.SUPPORT, fear);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.FreepsPlayAvailable(great));
		var pool = scn.GetTwilight();
		scn.FreepsPlayCard(great);

		// A Dwarf with a damage bonus, companion or ally; not the Dwarf who has none, nor Huorn, who has one and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, huorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gollum));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		// Gimli with his axe is damage +2: Gollum, of vitality 4, exerts twice. The minion already on the table is left alone.
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(great);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyWhoIsDamagePlusOneExertsToExertTheMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(great);

		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsChooseCard(grimir);

		// X is the bonus of the Dwarf who exerted, not the greatest on the table.
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(great);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedForADwarfWithNoDamageBonusUntilHeHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var guard = scn.GetFreepsCard("guard");
		var huorn = scn.GetFreepsCard("huorn");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(guard, huorn);
		scn.MoveCardsToHand(great);

		var savage = scn.GetShadowCard("savage");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(savage, gollum);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The Dwarf Guard is a Dwarf who could exert and has no damage bonus; Huorn has the bonus and is no Dwarf.
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInHand(great);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The same Dwarf with a damage bonus is the Dwarf the event wants.
		scn.AttachCardsTo(guard, ring);
		scn.ShadowPlayCard(gollum);
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyDwarfWithADamageBonusIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(gimli, guard, huorn);
		scn.MoveCardsToHand(great);

		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Gimli cannot exert. The Dwarf Guard and Huorn could.
		scn.AddWoundsToChar(gimli, 2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertInHand(great);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionWithLessVitalityThanXExertsOnlyAsOftenAsItCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(great);

		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToHand(assassin);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		scn.ShadowPlayCard(assassin);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		// X is 2 and the Assassin has 2 vitality: he exerts once, is exhausted, and is not killed by a second.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertInDiscard(great);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionPlayedByAnotherCardsAbilityIsAMinionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(great);
		scn.MoveCompanionsToTable(gimli);

		var assassin = scn.GetShadowCard("assassin");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToHand(assassin);
		scn.MoveCardsToSupportArea(towers);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(assassin);
		// "Shadow: Play a besieger to place a [sauron] token here."
		scn.ShadowUseCardAction(towers);

		assertInZone(Zone.SHADOW_CHARACTERS, assassin);
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertInDiscard(great);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GimliCounterOfFoesExertsTheMinionOnceThoughHisOwnExertionMakesHimDamagePlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var counter = scn.GetFreepsCard("counter");
		scn.MoveCardsToHand(great);
		scn.MoveCompanionsToTable(counter);

		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		scn.ShadowPlayCard(troll);

		assertEquals(0, scn.GetWoundsOn(counter));
		assertEquals(1, scn.GetKeywordCount(counter, Keyword.DAMAGE));

		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);

		// Damage +1 unwounded, and +1 more for the wound of the exertion that paid; X was read before it
		// (Q-0256): the Troll, of vitality 4, exerts once.
		assertEquals(2, scn.GetKeywordCount(counter, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(counter));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void XIsTheDamageBonusTheDwarfHadBeforeHeExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var great = scn.GetFreepsCard("great");
		var counter = scn.GetFreepsCard("counter");
		scn.MoveCardsToHand(great);
		scn.MoveCompanionsToTable(counter);

		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(gollum);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPass();

		scn.ShadowPlayCard(gollum);

		// Unwounded, this Gimli is damage +1; the exertion that pays for the event makes him +2.
		assertEquals(0, scn.GetWoundsOn(counter));
		assertEquals(1, scn.GetKeywordCount(counter, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(great));
		scn.FreepsPlayCard(great);
		assertEquals(1, scn.GetWoundsOn(counter));

		// He was damage +1 when he was chosen: Gollum, of vitality 4, exerts once.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
