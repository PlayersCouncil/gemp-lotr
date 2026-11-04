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

public class Card_15_039_Tests
{
	// Every skirmish here is fought at site 2 of the Fellowship path, Ettenmoors: "Skirmish: Exert your companion or
	// minion to make that character strength +2", a skirmish special ability either player may use.

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("called", "15_39");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, Ring-bound, no race; "Skirmish: Discard a card from hand to make Sméagol strength +1"
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, Ring-bound; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit; a fellowship ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound Hobbit; +2 only bearing a weapon
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, never used
					put("swordarm", "1_116");   // Swordarm of the White Tower: a card for Sméagol to discard

					put("gollum", "6_40");      // Gollum, Old Villain: [gollum] minion, strength 5, vitality 4; "Skirmish: Discard a card from hand to make Gollum strength +1 (or +2 ...)"
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum; a trigger on playing a Nazgûl and an assignment ability, neither used
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its trigger is on its play only, and it is cheated in
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("shaman", "3_59");      // Isengard Shaman: [isengard] Orc; "Response: If an [isengard] Orc is about to take a wound, remove (2) to prevent that wound"
					put("longdark", "1_188");   // The Long Dark: [moria] support condition; "Skirmish: Discard this condition to make your [moria] Orc strength +2"
					put("drums", "1_168");      // Drums in the Deep: [moria] skirmish event, twilight 0; "Make a [moria] Orc strength +2"
					put("savagery", "1_139");   // Savagery to Match Their Numbers: a card for Gollum to discard
					put("savagery2", "1_139");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CalledtoMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Called to Mordor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be Sméagol.
		 * While you can spot 2 Ring-bound Hobbits, each time an opponent uses a skirmish special ability in a skirmish involving Gollum or Sméagol, you may wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("called");

		assertEquals("Called to Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void CanBePlayedOnlyOnSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable("sam", "aragorn");
		scn.MoveCardsToHand(called, smeagol);

		scn.StartGame();

		// Frodo, Sam and Aragorn: no Sméagol to bear it.
		assertInHand(called, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(called));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);

		// Sméagol is the one bearer it can have, and it goes on him unasked.
		assertTrue(scn.FreepsPlayAvailable(called));
		scn.FreepsPlayCard(called);

		assertAttachedTo(called, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSkirmishAbilityTheOpponentUsesInGollumsSkirmishLetsTheFreePeoplesPlayerWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var savagery = scn.GetShadowCard("savagery");
		var savagery2 = scn.GetShadowCard("savagery2");
		scn.MoveCompanionsToTable(sam, aragorn, smeagol);
		scn.AttachCardsTo(smeagol, called);
		scn.MoveCardsToHand(savagery, savagery2);
		assertInHand(savagery, savagery2);

		scn.StartGame();
		assertAttachedTo(called, smeagol);
		scn.MoveMinionsToTable(gollum, runner);
		scn.SkipToAssignments();
		// Gollum fights Aragorn; Sméagol fights nobody, and the Goblin Runner is left out of every skirmish.
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(runner));

		// Gollum's own skirmish ability.  Declined, nobody is wounded.
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChooseCard(savagery);
		assertInDiscard(savagery);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(called));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();

		// The same ability again, in the same skirmish.
		scn.ShadowUseCardAction(gollum);
		assertInDiscard(savagery2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(called));
		scn.FreepsAcceptOptionalTrigger();

		// Any minion, in the skirmish or not; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, sam, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		// The Goblin Runner had one vitality: wounded, and so killed.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertAttachedTo(called, smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InSmeagolsSkirmishAShadowConditionsAbilityIsAnsweredAndAResponseAndTheFreePeoplesPlayersOwnAbilitiesAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var swordarm = scn.GetFreepsCard("swordarm");
		var shaman = scn.GetShadowCard("shaman");
		var runner = scn.GetShadowCard("runner");
		var longdark = scn.GetShadowCard("longdark");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.AttachCardsTo(smeagol, called);
		scn.MoveCardsToHand(swordarm);
		assertInHand(swordarm);

		scn.StartGame();
		assertAttachedTo(called, smeagol);
		scn.MoveMinionsToTable(shaman, runner);
		scn.MoveCardsToSupportArea(longdark);
		assertInZone(Zone.SUPPORT, longdark);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, shaman);
		scn.ShadowDeclineAssignments();
		// Twilight for the Shaman's response.
		scn.SetTwilight(2);
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, shaman));

		// A Shadow condition's skirmish ability, on behalf of the Goblin Runner, who is in no skirmish.
		scn.ShadowUseCardAction(longdark);
		assertInDiscard(longdark);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(called));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(shaman);

		// The Shaman's Response in Sméagol's skirmish is a special ability, and no skirmish special ability.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Sméagol's own skirmish ability: the Free Peoples player is no opponent.
		scn.FreepsUseCardAction(smeagol);
		assertInDiscard(swordarm);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// The site's ability, used by the Free Peoples player with Sam.
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayersUseOfTheSitesSkirmishAbilityIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable("sam");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, called);

		scn.StartGame();
		assertAttachedTo(called, smeagol);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The opponent uses a skirmish special ability, by a card of no side: his exertion of the Savage.
		var ettenmoors = scn.GetCurrentSite();
		scn.ShadowUseCardAction(ettenmoors);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(called));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotAnsweredInASkirmishWithoutGollumOrSmeagolNorForASkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var longdark = scn.GetShadowCard("longdark");
		var drums = scn.GetShadowCard("drums");
		var savagery = scn.GetShadowCard("savagery");
		scn.MoveCompanionsToTable("sam");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.AttachCardsTo(smeagol, called);
		scn.MoveCardsToHand(drums, savagery);
		assertInHand(drums, savagery);

		scn.StartGame();
		assertAttachedTo(called, smeagol);
		scn.MoveMinionsToTable(gollum, savage, runner);
		scn.MoveCardsToSupportArea(longdark);
		assertInZone(Zone.SUPPORT, longdark);
		scn.SkipToAssignments();
		// Gollum is left out of every skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{smeagol, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Aragorn's skirmish involves neither Gollum nor Sméagol, though Gollum himself uses his ability in it.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertFalse(scn.IsCharSkirmishing(gollum));
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChooseCard(savagery);
		assertInDiscard(savagery);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Sméagol's skirmish: a skirmish event is no special ability.
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, runner));
		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(drums);
		assertEquals(7, scn.GetStrength(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();

		// The control: a skirmish special ability in that skirmish is answered.
		scn.ShadowUseCardAction(longdark);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(called));
	}

	@Test
	public void WithOneRingBoundHobbitItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		var longdark = scn.GetShadowCard("longdark");
		// Frodo is the one Ring-bound Hobbit: Sméagol is Ring-bound and no Hobbit, Merry a Hobbit and unbound.
		scn.MoveCompanionsToTable("merry");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, called);

		scn.StartGame();
		assertAttachedTo(called, smeagol);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(longdark);
		assertInZone(Zone.SUPPORT, longdark);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, runner));
		scn.ShadowUseCardAction(longdark);

		assertInDiscard(longdark);
		assertEquals(7, scn.GetStrength(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolBearsItAndTheWraithGollumsSkirmishWithHimIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetFreepsCard("called");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var gollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		var longdark = scn.GetShadowCard("longdark");
		scn.MoveCompanionsToTable("sam");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(called);

		scn.StartGame();

		assertInHand(called);
		assertTrue(scn.FreepsPlayAvailable(called));
		scn.FreepsPlayCard(called);
		assertAttachedTo(called, smeagol);

		scn.MoveMinionsToTable(gollum, runner);
		scn.MoveCardsToSupportArea(longdark);
		assertInZone(Zone.SUPPORT, longdark);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, gollum));
		assertEquals(0, scn.GetWoundsOn(gollum));
		scn.ShadowUseCardAction(longdark);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(called));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gollum, runner));
		scn.FreepsChooseCard(gollum);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
