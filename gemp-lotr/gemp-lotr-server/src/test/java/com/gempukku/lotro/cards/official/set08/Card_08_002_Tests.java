package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battle", "8_2");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, damage +1; a skirmish ability only
					put("axe", "1_14");         // Gimli's Battle Axe: bearer must be Gimli, he is damage +1 (so +2); an optional trigger when he wins a skirmish
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, no damage bonus of his own; a fellowship ability only
					put("ring", "9_8");         // Ring of Guile: artifact, bearer must be a Dwarf, bearer is damage +1; a maneuver ability used only on purpose
					put("huorn", "9_25");       // Huorn: a [gandalf] companion who is damage +1 and no Dwarf; a response that needs an Ent

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; "Damage +1." is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BattleinEarnestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Battle in Earnest
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot a Dwarf who is damage +X to place X wounds on minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battle");

		assertEquals("Battle in Earnest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWhoIsDamagePlusTwoPlacesTwoWoundsOnMinionsOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var huorn = scn.GetFreepsCard("huorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, huorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(battle);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(battle));
		scn.FreepsPlayCard(battle);

		// A Dwarf with a damage bonus, companion or ally; not Huorn, who has the bonus and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(huorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// Minions of two cultures and races; no companion.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, huorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// The Runner has 1 vitality: the wound kills him.
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);

		// The second wound: the Savage is the one minion left, and takes it unasked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(battle);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void XIsTheDamageBonusOfTheDwarfSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(battle);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		scn.FreepsPlayCard(battle);
		scn.FreepsChooseCard(grimir);

		// Grimir is damage +1, not the +2 Gimli has: one wound.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(battle);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void BothWoundsMayBePlacedOnOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToHand(battle);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsPlayCard(battle);

		// "Place X wounds on minions", not "wound X minions": the Savage is offered for the second wound too.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(battle);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(battle));
		scn.FreepsPlayCard(battle);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(battle);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoDwarfThoughACompanionHasADamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveCardsToHand(battle);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// Huorn is damage +1, and a Tree.
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertInHand(battle);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(battle));
	}

	@Test
	public void CannotBePlayedWithADwarfWhoHasNoDamageBonusUntilHeHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(battle);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// Grimir is a Dwarf of no damage bonus; Huorn has the bonus and is no Dwarf.
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertInHand(battle);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(battle));

		// The same Dwarf with a damage bonus of his own is the Dwarf the event wants.
		scn.AttachCardsTo(grimir, ring);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(battle));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
