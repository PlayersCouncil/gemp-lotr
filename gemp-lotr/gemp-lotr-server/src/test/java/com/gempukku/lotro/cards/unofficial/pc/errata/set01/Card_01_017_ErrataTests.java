package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_017_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimir", "51_17");

					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6, vitality 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man companion, strength 8

					put("blow", "1_5");           // Cleaving Blow: [dwarven] skirmish event, cost 1: a Dwarf is strength +2 and damage +1
					put("strike", "1_3");         // Axe Strike: [dwarven] skirmish event, cost 0
					put("halls", "1_26");         // Their Halls of Stone: [dwarven] skirmish event, cost 0
					put("fury", "1_4");           // Battle Fury: [dwarven] skirmish event, cost 0: exert a Dwarf to make that Dwarf strength +3
					put("balin", "1_19");         // Here Lies Balin, Son of Fundin: [dwarven] maneuver event: exert a Dwarf to wound Orcs
					put("nobody", "1_23");        // Nobody Tosses a Dwarf: [dwarven] response event, cost 0, to a Dwarf winning a skirmish
					put("swordarm", "1_116");     // Swordarm of the White Tower: [gondor] skirmish event: a [gondor] companion is strength +2
					put("armor", "1_8");          // Dwarven Armor: [dwarven] possession

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Grimir, Dwarven Elder
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Dwarf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Skirmish: Spot a [dwarven] companion and exert Grimir twice to play a [dwarven] skirmish
		 * event from your discard pile, then remove it from the game.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimir");

		assertEquals("Grimir", card.getBlueprint().getTitle());
		assertEquals("Dwarven Elder", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void IsPlayedForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(grimir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(grimir));

		scn.FreepsPlayCard(grimir);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsGrimirTwiceToPlayADwarvenSkirmishEventFromTheDiscardPileWhichIsThenRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blow = scn.GetFreepsCard("blow");
		var strike = scn.GetFreepsCard("strike");
		var halls = scn.GetFreepsCard("halls");
		var balin = scn.GetFreepsCard("balin");
		var nobody = scn.GetFreepsCard("nobody");
		var swordarm = scn.GetFreepsCard("swordarm");
		var armor = scn.GetFreepsCard("armor");
		var shadowFury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		// Aragorn is here so that Swordarm of the White Tower has someone to be played for.
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Two [dwarven] events that are not skirmish events lie beside the two that are.  The game refuses a
		// maneuver event in a skirmish phase whoever plays it; a response event it would let through, so that
		// one is kept out by this card's words alone.
		scn.MoveCardsToDiscard(blow, strike, balin, nobody, swordarm, armor);
		// [dwarven] skirmish events that are not in this player's discard pile: one in hand, one in the other
		// player's pile.
		scn.MoveCardsToHand(halls);
		scn.MoveCardsToDiscard(shadowFury);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		var pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(grimir));

		scn.FreepsUseCardAction(grimir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(blow, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, nobody, swordarm, armor, halls, shadowFury));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		// Cleaving Blow is played: it costs 1 and makes a Dwarf strength +2.
		scn.FreepsChooseCardBPFromSelection(blow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		scn.FreepsChooseCard(gimli);

		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(pool + 1, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(gimli));
		assertInZone(Zone.REMOVED, blow);
		assertInZone(Zone.DISCARD, strike, balin, nobody, swordarm, armor, shadowFury);
		assertInZone(Zone.HAND, halls);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheEventsOwnCostIsPaidWhenItIsPlayedThisWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var fury = scn.GetFreepsCard("fury");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDiscard(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(grimir));

		// Battle Fury is the only event there and, once Grimir has exerted twice, Gimli the only Dwarf who can
		// exert for it: neither is asked for.
		scn.FreepsUseCardAction(grimir);
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		// 6 + 3
		assertEquals(9, scn.GetStrength(gimli));
		assertInZone(Zone.REMOVED, fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	@Ignore("F-0184: offered when the only event in the discard pile cannot be played once Grimir has exerted twice; he exerts and nothing is played")
	public void IsNotOfferedWhenGrimirsOwnExertionsWouldLeaveTheOnlyEventUnplayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var fury = scn.GetFreepsCard("fury");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDiscard(fury);
		// Vitality 3 with 2 wounds: Gimli can be spotted and cannot exert.  Battle Fury needs a Dwarf to exert,
		// and Grimir is the only one who can: after his two exertions nobody is left to pay for it.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimir));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInZone(Zone.DISCARD, fury);
	}

	@Test
	public void NeedsACompanionOfTheDwarvenCultureToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blow = scn.GetFreepsCard("blow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDiscard(blow);
		// Grimir is himself a [dwarven] character, but an ally.  Aragorn is made a Dwarf who is not [dwarven].
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.DWARF));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimir));

		// The same companion, now of the [dwarven] culture as well.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(grimir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenGrimirCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var blow = scn.GetFreepsCard("blow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDiscard(blow);
		// Vitality 3 with 1 wound: one exertion is left to him, not two.
		scn.AddWoundsToChar(grimir, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimir));

		scn.RemoveWoundsFromChar(grimir, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(grimir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoDwarvenSkirmishEventInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blow = scn.GetFreepsCard("blow");
		var halls = scn.GetFreepsCard("halls");
		var balin = scn.GetFreepsCard("balin");
		var nobody = scn.GetFreepsCard("nobody");
		var swordarm = scn.GetFreepsCard("swordarm");
		var armor = scn.GetFreepsCard("armor");
		var shadowFury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Every near miss and nothing else: a [dwarven] maneuver event, a [dwarven] response event, a [gondor]
		// skirmish event and a [dwarven] possession in the discard pile, and [dwarven] skirmish events in hand
		// and in the other player's discard pile.
		scn.MoveCardsToDiscard(balin, nobody, swordarm, armor, shadowFury);
		scn.MoveCardsToHand(halls);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimir));

		scn.MoveCardsToDiscard(blow);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(grimir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
