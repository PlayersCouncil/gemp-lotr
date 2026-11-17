package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_208_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vengeance", "4_208");
					put("vengeance2", "4_208");
					put("tracker", "4_193");    // Uruk Runner: [isengard] tracker, strength 8, vitality 2; stronger in a fierce skirmish only
					put("scout", "2_47");       // Uruk Scout: [isengard] tracker, strength 7, Damage +1; its response answers an event that spots or exerts a ranger
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] tracker, strength 5; its response answers an unbound Hobbit killed
					put("orcpursuer", "5_108"); // Orc Pursuer: [sauron] Orc tracker, strength 6; its trigger answers its own play
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no tracker, strength 5, Damage +1 and nothing else
					put("spear", "4_196");      // Uruk Spear: [isengard] hand weapon on an Uruk-hai, strength +2; a maneuver ability used only on purpose
					put("poison", "8_28");      // Spider Poison: a Shadow possession a companion bears; its bearer exerts as he is assigned
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("nostranger", "1_108"); // No Stranger to the Shadows: [gondor] condition on a ranger, no strength
					put("worker", "15_135");    // Rohan Worker: a follower that changes no number
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a possession on a Dwarf that changes no number
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally who may exert to take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengeanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Vengeance
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an [isengard] tracker strength +2 for each card borne by the character it is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vengeance");

		assertEquals("Vengeance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheTrackerStrengthPlusTwoForEachCardBorneByTheCharacterItIsSkirmishingForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetShadowCard("vengeance");
		var vengeance2 = scn.GetShadowCard("vengeance2");
		var scout = scn.GetShadowCard("scout");
		var tracker = scn.GetShadowCard("tracker");
		var spear = scn.GetShadowCard("spear");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var worker = scn.GetFreepsCard("worker");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Aragorn bears three cards: a Free Peoples condition, a follower and a Shadow possession.
		scn.AttachCardsTo(aragorn, nostranger, worker, poison);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(vengeance, vengeance2);
		assertInHand(vengeance, vengeance2);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, tracker);
		scn.AttachCardsTo(scout, spear);
		assertAttachedTo(nostranger, aragorn);
		assertAttachedTo(worker, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(spear, scout);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInHand(vengeance, vengeance2);
		assertFalse(scn.ShadowPlayAvailable(vengeance));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout}, new PhysicalCardImpl[]{gimli, tracker});
		// Spider Poison: Aragorn exerts as he is assigned.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 7 + 2 for the spear
		assertEquals(9, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(tracker));
		scn.ShadowPlayCard(vengeance);
		// Any [isengard] tracker may be chosen: the Uruk Runner, whose skirmish is still to come, is offered too.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, tracker));
		scn.ShadowChooseCard(scout);

		// The three cards Aragorn bears count; the spear on the Uruk himself, The One Ring on Frodo and the armor
		// on Gimli do not: 9 + 2 x 3
		assertEquals(15, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(tracker));
		assertInDiscard(vengeance);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 15 against 8: Aragorn loses (not overwhelmed, short of 16) and takes 2 wounds for Damage +1.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Gone with Aragorn's skirmish.
		assertEquals(9, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(tracker));

		// Gimli bears one card: 8 + 2
		scn.ShadowPlayCard(vengeance2);
		scn.ShadowChooseCard(tracker);
		assertEquals(10, scn.GetStrength(tracker));
		assertEquals(9, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnIsengardTrackerInNoSkirmishMayBeChosenAndGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetShadowCard("vengeance");
		var scout = scn.GetShadowCard("scout");
		var tracker = scn.GetShadowCard("tracker");
		var orcpursuer = scn.GetShadowCard("orcpursuer");
		var savage = scn.GetShadowCard("savage");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, nostranger);
		assertAttachedTo(nostranger, aragorn);
		scn.MoveCardsToHand(vengeance);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, tracker, orcpursuer, savage, pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		// The Uruk Pursuer, an [isengard] tracker, is left out of every skirmish.
		scn.ShadowAssignToMinions(aragorn, tracker, orcpursuer, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(tracker));
		assertEquals(5, scn.GetStrength(pursuer));
		scn.ShadowPlayCard(vengeance);

		// As printed, "an [isengard] tracker": the two in the skirmish and the one in no skirmish; not a [sauron]
		// tracker or an [isengard] Uruk-hai who is no tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, tracker, pursuer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orcpursuer, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(pursuer);

		// The Pursuer skirmishes nobody, so there are no cards to count: he gains nothing, and the two trackers in
		// the skirmish are unchanged.
		assertEquals(5, scn.GetStrength(pursuer));
		assertEquals(7, scn.GetStrength(scout));
		assertEquals(8, scn.GetStrength(tracker));
		assertInDiscard(vengeance);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnAllyTheTrackerIsStrengthPlusTwoForTheOneCardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetShadowCard("vengeance");
		var scout = scn.GetShadowCard("scout");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, armor);
		assertAttachedTo(armor, thrarin);
		scn.MoveCardsToHand(vengeance);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes until the regroup phase.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, scout);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(scout));
		scn.ShadowPlayCard(vengeance);

		// 7 + 2 for the armor
		assertEquals(9, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AHinderedCardBorneByTheCharacterTheTrackerIsSkirmishingIsCountedWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vengeance = scn.GetShadowCard("vengeance");
		var tracker = scn.GetShadowCard("tracker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);
		// Two cards that change no strength of Aragorn's: a condition on a ranger and a follower.
		scn.AttachCardsTo(aragorn, nostranger, worker);
		assertAttachedTo(nostranger, aragorn);
		assertAttachedTo(worker, aragorn);
		scn.MoveCardsToHand(vengeance);

		scn.StartGame();
		scn.MoveMinionsToTable(tracker);
		// A hindered card is a card still, and still borne
		scn.HinderCard(nostranger);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, tracker);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(nostranger));
		assertAttachedTo(nostranger, aragorn);
		assertAttachedTo(worker, aragorn);
		assertEquals(8, scn.GetStrength(tracker));

		scn.ShadowPlayCard(vengeance);
		// 8 + 2 for each of the two cards Aragorn bears
		assertEquals(12, scn.GetStrength(tracker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
