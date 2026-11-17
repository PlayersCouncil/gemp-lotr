package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "7_25");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, here only a card in hand
					put("bow", "1_41");         // Elven Bow: an [elven] possession, here only a card in hand
					put("defiance", "1_37");    // Defiance: an [elven] Skirmish event, here only a card in hand
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, vitality 1, so exhausted as he stands
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
					put("fighter1", "7_286");   // Mordor Fighter: while a threat and another [sauron] Orc can be spotted,
					put("fighter2", "7_286");   //   the Shadow has initiative whatever the Free Peoples hand holds
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Legolas, Fearless Marksman
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
		 * Game Text: <b>Archer</b>.<br><b>Archery:</b> If you have initiative, discard 4 cards from hand to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Fearless Marksman", card.getBlueprint().getSubtitle());
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
	public void ArcheryAbilityDiscardsFourCardsOfAnyKindFromHandToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas);
		// A companion, a possession, an event and two Shadow minions in hand.
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		var handRunner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(aragorn, bow, defiance, handTroop, handRunner);
		assertInHand(aragorn, bow, defiance, handTroop, handRunner);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		// Any four cards of the hand, of either side and any type.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, bow, defiance, handTroop, handRunner));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, defiance, handTroop, handRunner);

		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(bow);
		assertInDiscard(aragorn, defiance, handTroop, handRunner);

		// Either minion, of two cultures and races, the exhausted one among them; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(runner);

		// A wound, not an exertion: the Runner, at vitality 1, is killed by it.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedFromThreeCardsInHandAndFromExactlyFourItDiscardsThemAllAndStillWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(aragorn, bow, defiance);
		assertInHand(aragorn, bow, defiance);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// Three cards: the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));

		// A fourth card: the Free Peoples player has initiative, and only just.
		scn.MoveCardsToHand(handTroop);
		assertInHand(handTroop);
		scn.FreepsRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		// The initiative is checked before the cost: the whole hand goes, and the wound is still made.
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(aragorn, bow, defiance, handTroop);
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheShadowHasInitiativeWhateverTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("bow"), scn.GetFreepsCard("defiance"),
				scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"));

		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter1, fighter2);
		scn.AddThreats(1);
		scn.SkipToPhase(Phase.ARCHERY);

		// Five cards in hand, enough to pay; but each Mordor Fighter spots a threat and the other [sauron] Orc.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));

		// With the threat gone the Fighters no longer take the initiative, and the ability is offered.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
	}
}
