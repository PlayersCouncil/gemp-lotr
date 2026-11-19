package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_253_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("journey", "1_253");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2, cost 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; adds (2) only when a weapon is played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, an Orc who is not [sauron]; no text

					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; fellowship and response abilities only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; his text is his own strength
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, cost 2; Knight is his whole text
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: [elven] Elf companion, cost 2; strength against a Nazgul only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, a character who is no companion; a fellowship ability only
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, cost 1; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JourneyIntoDangerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Journey Into Danger
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, exert a [sauron] Orc. Plays to your support area.<br>While you can spot 5 companions, each companion's twilight cost is +2.
		*/

		var scn = GetScenario();

		var journey = scn.GetFreepsCard("journey");

		assertEquals("Journey Into Danger", journey.getBlueprint().getTitle());
		assertNull(journey.getBlueprint().getSubtitle());
		assertFalse(journey.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, journey.getBlueprint().getSide());
		assertEquals(Culture.SAURON, journey.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, journey.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(journey, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(journey, Keyword.SUPPORT_AREA));
		assertEquals(0, journey.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcToPlayToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetShadowCard("journey");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(journey);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, sentry, troop);
		assertInHand(journey);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(journey));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.ShadowPlayCard(journey);
		// Both [sauron] Orcs are offered; the [sauron] Uruk-hai and the [moria] Orc, each able to exert, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, journey);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetShadowCard("journey");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(journey);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, troop);
		// Vitality 2 with 1 wound: exhausted.  The Uruk-hai and the [moria] Orc beside it could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertTrue(scn.IsExhausted(soldier));
		assertFalse(scn.IsExhausted(sentry));
		assertFalse(scn.IsExhausted(troop));
		assertInHand(journey);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(journey));
		assertInHand(journey);
	}

	@Test
	public void CompanionsCostTwoMoreOnceAFifthCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetShadowCard("journey");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var knight = scn.GetFreepsCard("knight");
		var boromir = scn.GetFreepsCard("boromir");
		// Frodo and three more: four companions.  The ally Grimir is no companion.
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(journey);
		scn.MoveCardsToHand(knight, boromir);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, journey);
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(knight));
		assertEquals(3, scn.GetTwilightCost(boromir));

		// The Knight is paid for while four companions are in play: his printed 2.
		scn.FreepsPlayCard(knight);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, knight);

		// Five companions now: Boromir costs 3 + 2.
		assertEquals(5, scn.GetTwilightCost(boromir));
		scn.FreepsPlayCard(boromir);
		assertEquals(7, scn.GetTwilight()); // 2 for the Knight + 5 for Boromir
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFiveCompanionsEachCompanionInEitherHandCostsTwoMoreAndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetShadowCard("journey");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var maggot = scn.GetFreepsCard("maggot");
		var shadowBoromir = scn.GetShadowCard("boromir");
		var soldier = scn.GetShadowCard("soldier");
		// Frodo and four more: five companions.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, gandalf);
		scn.MoveCardsToHand(boromir, legolas, maggot, shadowBoromir, soldier);

		scn.StartGame();

		// Without the condition: the printed costs.
		assertEquals(3, scn.GetTwilightCost(boromir));
		assertEquals(2, scn.GetTwilightCost(legolas));
		assertEquals(1, scn.GetTwilightCost(maggot));
		assertEquals(3, scn.GetTwilightCost(shadowBoromir));
		assertEquals(2, scn.GetTwilightCost(soldier));

		scn.MoveCardsToSupportArea(journey);
		assertInZone(Zone.SUPPORT, journey);

		// A [gondor] Man and an [elven] Elf in the Free Peoples hand, and the Shadow player's Boromir: +2 each.
		assertEquals(5, scn.GetTwilightCost(boromir));
		assertEquals(4, scn.GetTwilightCost(legolas));
		assertEquals(5, scn.GetTwilightCost(shadowBoromir));
		// An ally and a minion are no companions.
		assertEquals(1, scn.GetTwilightCost(maggot));
		assertEquals(2, scn.GetTwilightCost(soldier));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
