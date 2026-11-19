package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("belt", "12_2");
					put("belt2", "12_2");

					// Dwarves of each type, and characters who are none.
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, not unique, no text but "To play, spot a Dwarf"
					put("guard2", "1_7");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F; a fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose

					// What a Dwarf may bear beside the Belt (an armor).
					put("handaxe", "2_10");     // Hand Axe: [dwarven] hand weapon, "Bearer must be a Dwarf"; an archery ability used only on purpose
					put("cloak", "1_42");       // Elven Cloak: [elven] cloak, "Bearer must be a companion"; the minion archery total is -1
					put("cloak2", "1_42");
					put("ring", "9_8");         // Ring of Guile: [dwarven] ARTIFACT, "Bearer must be a Dwarf", damage +1; a maneuver ability used only on purpose

					// Damage +1 minions of two cultures and races, and a third for another skirmish.
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, strength 15; Damage +1 and fierce; its other text is about being played
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8; Damage +1 is its whole text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					put("lurtzcry", "1_128");   // Lurtz's Battle Cry: Skirmish event, "Make an Uruk-hai strength -1 and damage +1"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeltofEreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Belt of Erebor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Dwarf.<br>Each minion skirmishing bearer loses all <b>damage</b> bonuses.<br><b>Skirmish:</b> Discard this possession to transfer one of bearer's possessions to another eligible bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("belt");

		assertEquals("Belt of Erebor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedOnADwarfCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var belt = scn.GetFreepsCard("belt");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(belt);
		assertInHand(belt);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(belt));
		scn.FreepsPlayCard(belt);

		// A Dwarf companion and a Dwarf ally; not the Elf or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(belt, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingBearerLosesAllDamageBonusesAndOneSkirmishingAnotherCompanionKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var belt = scn.GetFreepsCard("belt");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cuguard = scn.GetShadowCard("cuguard");
		var troll = scn.GetShadowCard("troll");
		var sentry = scn.GetShadowCard("sentry");
		var lurtzcry = scn.GetShadowCard("lurtzcry");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, belt);
		scn.MoveCardsToHand(lurtzcry);
		assertAttachedTo(belt, gimli);
		assertInHand(lurtzcry);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(cuguard, troll, sentry);

		// The Guard and the Troll both go to Gimli, the Troll put there by the Shadow player; the Sentry to Aragorn.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, cuguard}, new PhysicalCardImpl[]{aragorn, sentry});
		scn.ShadowAssignToMinions(gimli, troll);

		// Assigned to the bearer but not yet skirmishing him: every bonus still there.
		assertEquals(1, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// In Aragorn's skirmish, fought first: the Sentry skirmishes another companion and keeps its bonus, and the
		// two waiting for Gimli keep theirs.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// In Gimli's skirmish, both minions lose their bonus.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, cuguard));
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, troll));
		assertEquals(0, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// "All": a bonus given during the skirmish is lost too, while the rest of the event lands
		// (CR 4.2 "damage bonus": a character that loses all damage bonuses cannot gain one).
		assertEquals(9, scn.GetStrength(cuguard));
		scn.FreepsPass();
		scn.ShadowPlayCard(lurtzcry);
		assertTrue(scn.ShadowHasCardChoiceAvailable(cuguard, sentry));
		scn.ShadowChooseCard(cuguard);
		assertEquals(8, scn.GetStrength(cuguard));
		assertEquals(0, scn.GetKeywordCount(cuguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByAnAllyAtHisHomeSiteTheMinionSkirmishingHimLosesItsDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var belt = scn.GetFreepsCard("belt");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, belt);
		assertAttachedTo(belt, grimir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();

		// Site 3 of the Fellowship block, Grimir's home: he may skirmish.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(grimir));
		scn.FreepsAssignToMinions(grimir, savage);
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(grimir);
		assertTrue(scn.IsCharSkirmishingAgainst(grimir, savage));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsTheBeltToTransferOneOfBearersPossessionsToAnotherEligibleBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var belt = scn.GetFreepsCard("belt");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var handaxe = scn.GetFreepsCard("handaxe");
		var cloak = scn.GetFreepsCard("cloak");
		var cloak2 = scn.GetFreepsCard("cloak2");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, belt, handaxe, cloak, ring);
		scn.AttachCardsTo(aragorn, cloak2);
		// A second Belt, on the Dwarf Guard: the copy used is the one discarded.
		var belt2 = scn.GetFreepsCard("belt2");
		scn.AttachCardsTo(guard, belt2);
		assertAttachedTo(belt2, guard);
		assertAttachedTo(belt, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(cloak, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(cloak2, aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);

		// The fellowship moves to site 3, Grimir's home: he is at the same site as the others.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(3, scn.GetCurrentSiteNumber());
		// The phase before, with the bearer able to pay: not offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(belt));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.FreepsActionAvailable(belt));

		scn.FreepsUseCardAction(belt);
		assertInZone(Zone.DISCARD, belt);
		assertAttachedTo(belt2, guard);

		// One of Gimli's possessions, of either culture or class: not his artifact, nor a possession of Aragorn's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(handaxe, cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, cloak2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(handaxe);

		// Another Dwarf who may bear it, a companion or an ally at the fellowship's site; not Gimli himself, nor a
		// character who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertAttachedTo(handaxe, guard);
		assertAttachedTo(belt2, guard);
		assertAttachedTo(cloak, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(cloak2, aragorn);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherPossessionTheAbilityIsOfferedInAnotherCompanionsSkirmishAndTransfersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var belt = scn.GetFreepsCard("belt");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.AttachCardsTo(gimli, belt, ring);
		assertAttachedTo(belt, gimli);
		assertAttachedTo(ring, gimli);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// Gimli is in no skirmish, and bears no possession but the Belt: offered all the same.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.FreepsActionAvailable(belt));

		scn.FreepsUseCardAction(belt);
		assertInZone(Zone.DISCARD, belt);
		assertAttachedTo(ring, gimli);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADwarfAllyAwayFromHisHomeSiteIsAnEligibleBearerToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var belt = scn.GetFreepsCard("belt");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var grimir = scn.GetFreepsCard("grimir");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(gimli, guard, guard2);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, belt, handaxe);
		assertAttachedTo(belt, gimli);
		assertAttachedTo(handaxe, gimli);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// Site 2: Grimir's home is site 3.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		// The Hand Axe is the only possession to transfer, and goes without asking.
		scn.FreepsUseCardAction(belt);
		assertInZone(Zone.DISCARD, belt);

		// The two Dwarf Guards with the fellowship, and Grimir at his home site 3 and not at site 2: a card's
		// "transfer" is not the Fellowship-phase transfer, and the same-site rule is that action's alone
		// (Ketura's ruling on Q-0377).
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guard2, grimir));
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(handaxe, grimir);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
