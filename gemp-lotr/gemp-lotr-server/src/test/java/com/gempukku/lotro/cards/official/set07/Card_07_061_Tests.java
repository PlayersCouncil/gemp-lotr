package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hobbitses", "7_61");
					put("hobbitses2", "7_61");
					put("hobbitses3", "7_61");

					// Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4.  His trigger answers a
					// Nazgul being played and his assignment ability needs a threat or a companion of cost 0.
					put("wraithgollum", "103_66");
					// Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4.  "Skirmish: Discard a card
					// from hand to make Sméagol strength +1."
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, strength 3, vitality 4; an assignment ability only
					put("shiresmeagol", "103_116");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is not Gollum; strength 8, vitality 8,
					// enduring, fierce, and damage +1 only in a fierce skirmish
					put("shelob", "8_26");

					// Úlairë Lemenya: [wraith] Nazgûl, strength 9, vitality 2; her text is about [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, fierce; his text needs 3 burdens
					put("khamul", "103_61");
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					// Morgul Hound: a [wraith] Orc, strength 5; its text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					// Strength Born of Fear: a [sauron] condition in the support area; its skirmish ability costs (3)
					put("fear", "1_278");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose

					// Cards for the Free Peoples hand: with 4 or more there he has initiative, with fewer the Shadow has.
					put("strike", "1_3");       // Axe Strike
					put("armor", "1_8");        // Dwarven Armor
					put("dwarf1", "1_7");       // Dwarf Guard
					put("dwarf2", "1_7");
					put("dwarf3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HobbitsesAreDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hobbitses Are Dead
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot Gollum or Sméagol to make a Nazgûl, [sauron] minion, or [gollum] minion strength +2.
		 * If you have initiative, you may play this event from your discard pile; place it under your draw deck
		 * instead of discarding it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hobbitses");

		assertEquals("Hobbitses Are Dead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandItMakesANazgulASauronMinionOrAGollumMinionStrengthPlusTwoForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var hobbitses = scn.GetShadowCard("hobbitses");
		var gollum = scn.GetShadowCard("gollum");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var troop = scn.GetShadowCard("troop");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(hobbitses);
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, soldier, guard, lemenya, hound, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier}, new PhysicalCardImpl[]{gimli, lemenya});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(pool >= 1);
		assertInZone(Zone.HAND, hobbitses);
		assertTrue(scn.ShadowPlayAvailable(hobbitses));

		scn.ShadowPlayCard(hobbitses);

		// A [gollum] minion, a [sauron] Orc, a [sauron] Uruk-hai and a Nazgûl, of whom only the Soldier is in this
		// skirmish.  Not a [wraith] Orc, a [moria] Orc, or a [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, soldier, guard, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, troop, fear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(9, scn.GetStrength(lemenya));

		scn.ShadowChooseCard(lemenya);
		assertEquals(11, scn.GetStrength(lemenya));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(pool - 1, scn.GetTwilight());

		// The Free Peoples hand is empty, so the Shadow has initiative: played from hand, the event is discarded
		// all the same.
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, hobbitses);
		assertNotSame(hobbitses, scn.GetShadowBottomOfDeck());

		// Aragorn, 8, beats the Soldier, 7.  In Gimli's skirmish, the next, Lemenya is 9 again.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(soldier));
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(lemenya));
	}

	@Test
	public void NotPlayableWithShelobAloneAndPlayableOnceGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hobbitses = scn.GetShadowCard("hobbitses");
		var hobbitses2 = scn.GetShadowCard("hobbitses2");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(hobbitses);
		scn.MoveCardsToDiscard(hobbitses2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Shelob is a [gollum] minion, and neither Gollum nor Sméagol: the event cannot be played from hand, nor,
		// with initiative, from the discard pile.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowPlayAvailable(hobbitses));
		assertFalse(scn.ShadowActionAvailable(hobbitses2));

		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hobbitses));
		assertTrue(scn.ShadowActionAvailable(hobbitses2));

		scn.ShadowPlayCard(hobbitses);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		assertEquals(8, scn.GetStrength(shelob));
		scn.ShadowChooseCard(shelob);
		assertEquals(10, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheWraithGollumIsSpottedForItAndIsNotAMinionItCanStrengthen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hobbitses = scn.GetShadowCard("hobbitses");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var soldier = scn.GetShadowCard("soldier");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(hobbitses);

		scn.StartGame();
		scn.MoveMinionsToTable(wraithgollum, soldier, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hobbitses));
		scn.ShadowPlayCard(hobbitses);

		// This Gollum is a [wraith] minion and no Nazgûl.  Khamûl is a Nazgûl of the [raider] culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wraithgollum));
		assertEquals(12, scn.GetStrength(khamul));
		scn.ShadowChooseCard(khamul);
		assertEquals(14, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(wraithgollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(aragorn, shiresmeagol);

		var hobbitses = scn.GetShadowCard("hobbitses");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(hobbitses);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hobbitses));
		scn.ShadowPlayCard(hobbitses);

		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shiresmeagol));
		scn.ShadowChooseCard(guard);
		assertEquals(11, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(shiresmeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void HobbitsesAreDeadCanBePlayedFromDiscardWithInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var strike = scn.GetFreepsCard("strike");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(strike, armor, scn.GetFreepsCard("dwarf1"), scn.GetFreepsCard("dwarf2"), scn.GetFreepsCard("dwarf3"));

		var hobbitses = scn.GetShadowCard("hobbitses");
		var hobbitses2 = scn.GetShadowCard("hobbitses2");
		var hobbitses3 = scn.GetShadowCard("hobbitses3");
		var soldier = scn.GetShadowCard("soldier");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToDiscard(hobbitses, hobbitses2);
		scn.MoveCardsToHand(hobbitses3);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, shelob);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(smeagol);

		// Sméagol's own ability takes the Free Peoples hand from 5 to 4: he still has initiative, and the event
		// lying in the Shadow discard pile is not offered.
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(strike);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowActionAvailable(hobbitses));
		assertFalse(scn.ShadowActionAvailable(hobbitses2));
		// The copy in hand is played all the same: initiative is asked of the play from the pile, not of the event.
		assertTrue(scn.ShadowPlayAvailable(hobbitses3));
		scn.ShadowPass();

		// From 4 to 3: the Shadow has initiative, and each copy in the pile may be played.
		scn.FreepsUseCardAction(smeagol);
		scn.FreepsChooseCard(armor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, hobbitses, hobbitses2);
		assertTrue(scn.ShadowActionAvailable(hobbitses));
		assertTrue(scn.ShadowActionAvailable(hobbitses2));
		int pool = scn.GetTwilight();
		assertNotSame(hobbitses, scn.GetShadowBottomOfDeck());

		scn.ShadowUseCardAction(hobbitses);

		// Sméagol is the one spotted, a [gollum] card that is no minion.  The Soldier is in the skirmish and
		// Shelob, a [gollum] minion, in none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(smeagol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(8, scn.GetStrength(shelob));
		scn.ShadowChooseCard(shelob);
		assertEquals(10, scn.GetStrength(shelob));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(pool - 1, scn.GetTwilight());

		// The copy played lies beneath the Shadow draw deck; the other is still in the discard pile, and still
		// to be played from it.
		assertInZone(Zone.DECK, hobbitses);
		assertSame(hobbitses, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.DISCARD, hobbitses2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hobbitses));
		assertTrue(scn.ShadowActionAvailable(hobbitses2));
	}

	@Test
	public void FromTheDiscardPileItIsPlayedOnlyInASkirmishAndOnlyWhenItsCostCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hobbitses = scn.GetShadowCard("hobbitses");
		var gollum = scn.GetShadowCard("gollum");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToDiscard(hobbitses);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, soldier);

		// The assignment phase, with initiative, Gollum spotted and twilight in the pool: not offered.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowActionAvailable(hobbitses));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// In the skirmish it costs (1) from the discard pile as it does from hand.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(hobbitses));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(hobbitses));

		scn.ShadowUseCardAction(hobbitses);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, soldier));
		scn.ShadowChooseCard(soldier);
		assertEquals(0, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(soldier));
		assertSame(hobbitses, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
