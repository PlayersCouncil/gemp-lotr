package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("traveler1", "15_69");
					put("traveler2", "15_69");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text

					// The three unique [gondor] support-area conditions that hold [gondor] tokens; each has a skirmish
					// ability used only on purpose, and a play trigger that a cheat does not set off
					put("leaders", "7_112");    // Noble Leaders
					put("heirs", "13_69");      // Heirs of Gondor
					put("garrison", "6_52");    // Garrison of Osgiliath
					// Suspended Palaces: a [dwarven] support-area condition that holds [dwarven] tokens; a fellowship
					// ability used only on purpose
					put("palaces", "6_10");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site 4, so roaming at site 2; strength 13, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site 5, so roaming at site 2; strength 5; Damage +1
					// Goblin Man: [isengard] Orc, site 2, so not roaming there; strength 6; discarded underground, and a
					// Shadow ability used only on purpose
					put("goblinman", "2_42");
					// Uruk Pursuer: [isengard] Uruk-hai, site 5, so roaming at site 2; strength 5, vitality 3; fierce;
					// its other text is about unbound Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SilentTravelerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Silent Traveler
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>At the start of each skirmish involving this companion and a roaming minion, you may reinforce a [gondor] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("traveler1");

		assertEquals("Silent Traveler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfHisSkirmishWithARoamingMinionHeMayReinforceAGondorTokenAndNotInOtherSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler1 = scn.GetFreepsCard("traveler1");
		var traveler2 = scn.GetFreepsCard("traveler2");
		var knight = scn.GetFreepsCard("knight");
		var leaders = scn.GetFreepsCard("leaders");
		var heirs = scn.GetFreepsCard("heirs");
		var garrison = scn.GetFreepsCard("garrison");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(traveler1, traveler2, knight);
		scn.MoveCardsToSupportArea(leaders, heirs, garrison, palaces);
		// One [gondor] token on each of two [gondor] conditions, none on the third, and a [dwarven] token on the
		// Palaces.
		scn.AddTokensToCard(leaders, 1);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(palaces, 1);
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, goblinman);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{traveler1, troop},
				new PhysicalCardImpl[]{traveler2, goblinman},
				new PhysicalCardImpl[]{knight, savage});

		// A roaming minion, in the skirmish of a companion who is no Silent Traveler: nothing is offered.
		scn.FreepsResolveSkirmish(knight);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A Silent Traveler against a minion who is not roaming: nothing is offered.
		scn.FreepsResolveSkirmish(traveler2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The other Silent Traveler against a roaming minion: his own copy offers, the copy outside the skirmish
		// does not.
		scn.FreepsResolveSkirmish(traveler1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(traveler1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(traveler2));
		scn.FreepsChooseOptionalTrigger(traveler1);

		// A card that has a [gondor] token already; not the [gondor] condition with none, nor the card with a
		// [dwarven] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaders, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison, palaces));
		scn.FreepsChooseCard(heirs);

		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedItLeavesTheTokenAndTheFierceSkirmishOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler1 = scn.GetFreepsCard("traveler1");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(traveler1);
		scn.MoveCardsToSupportArea(leaders);
		scn.AddTokensToCard(leaders, 1);
		assertEquals(1, scn.GetCultureTokensOn(leaders));

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(pursuer, Keyword.ROAMING));
		scn.FreepsAssignToMinions(traveler1, pursuer);

		scn.FreepsResolveSkirmish(traveler1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(traveler1));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 5 + 1 for hunter against a minion who is no hunter, against 5: he wins, and the fierce Pursuer lives.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(traveler1, pursuer);

		// A second skirmish of his against a roaming minion: offered again.  One card to reinforce: no choice.
		scn.FreepsResolveSkirmish(traveler1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(traveler1));
		scn.FreepsChooseOptionalTrigger(traveler1);
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorTokenToReinforceItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traveler1 = scn.GetFreepsCard("traveler1");
		var garrison = scn.GetFreepsCard("garrison");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(traveler1);
		scn.MoveCardsToSupportArea(garrison, palaces);
		// A [gondor] condition with no token, and a token of another culture.
		scn.AddTokensToCard(palaces, 1);
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		scn.FreepsAssignToMinions(traveler1, troop);

		scn.FreepsResolveSkirmish(traveler1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(palaces));
	}
}
