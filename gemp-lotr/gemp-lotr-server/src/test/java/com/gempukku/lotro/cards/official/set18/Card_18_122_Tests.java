package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shagrat", "18_122");
					// Shadow possessions, each on a minion that bears it with no text in the way
					put("spear", "17_117");     // Spear of the White Hand: [uruk-hai] hand weapon on an [uruk-hai] Uruk-hai; no other text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, Damage +1 only; the other minion
					put("axe", "10_102");       // Uruk Axe: [sauron] hand weapon on a [sauron] minion; +2 on an Uruk-hai
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; text only on a weapon PLAYED on him
					put("banner", "6_98");      // Banner of the Eye: [sauron] hand weapon on a [sauron] Orc; text only on a skirmish won

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; skirmish ability only
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, cost 0, on a Dwarf; changes no number
					put("handaxe", "2_10");     // Hand Axe: [dwarven] hand weapon on a Dwarf; an archery ability used only on purpose
					put("ring", "17_4");        // Ring of Artifice: [dwarven] ARTIFACT on a Dwarf, no possession
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; its draw only on its own play
					put("cart", "1_73");        // Gandalf's Cart: [gandalf] possession in the support area; a Fellowship ability only
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] condition in the support area, no possession
					put("baruk", "5_5");        // Baruk Khazad: Maneuver event, "Exert a Dwarf to wound a minion"
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("fell", "4_97");        // Long I Fell: Skirmish event; the Shadow player may make the Free Peoples player wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShagratStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Shagrat, Tower Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>While you can spot 7 possessions, Shagrat is <b>fierce</b> and cannot take wounds (except during skirmish phases).
		*/

		var scn = GetScenario();

		var shagrat = scn.GetFreepsCard("shagrat");

		assertEquals("Shagrat", shagrat.getBlueprint().getTitle());
		assertEquals("Tower Captain", shagrat.getBlueprint().getSubtitle());
		assertTrue(shagrat.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shagrat.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, shagrat.getBlueprint().getCulture());
		assertEquals(CardType.MINION, shagrat.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, shagrat.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(shagrat, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(shagrat, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(shagrat, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(shagrat, Keyword.HUNTER));
		assertEquals(5, shagrat.getBlueprint().getTwilightCost());
		assertEquals(13, shagrat.getBlueprint().getStrength());
		assertEquals(3, shagrat.getBlueprint().getVitality());
		assertEquals(5, shagrat.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceOnlyWhileSevenPossessionsOfEitherSideCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var cart = scn.GetFreepsCard("cart");
		var stairs = scn.GetFreepsCard("stairs");
		var shagrat = scn.GetShadowCard("shagrat");
		var spear = scn.GetShadowCard("spear");
		var guard = scn.GetShadowCard("guard");
		var axe = scn.GetShadowCard("axe");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, handaxe, ring);
		scn.MoveCardsToSupportArea(toby, cart, stairs);
		scn.MoveCardsToHand(armor);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, toby, cart, stairs);
		assertInHand(armor);

		scn.StartGame();
		// Laid in the fellowship phase, so that a Free Peoples possession played there brings the count up (Q-0228).
		scn.MoveMinionsToTable(shagrat, guard, chieftain);
		scn.AttachCardsTo(shagrat, spear);
		scn.AttachCardsTo(guard, axe);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(spear, shagrat);
		assertAttachedTo(axe, guard);
		assertAttachedTo(banner, chieftain);

		// Six possessions: two Free Peoples ones in a support area, one borne by a companion, and three Shadow ones
		// borne by minions, Shagrat among them; beside them an artifact and a condition, neither a possession.
		assertFalse(scn.HasKeyword(shagrat, Keyword.FIERCE));

		// A seventh, borne by a companion, played by the Free Peoples player.
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.HasKeyword(shagrat, Keyword.FIERCE));
		// "Shagrat": not the other minions.
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileSevenPossessionsCanBeSpottedShagratCannotTakeWoundsOutsideSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var cart = scn.GetFreepsCard("cart");
		var stairs = scn.GetFreepsCard("stairs");
		var baruk = scn.GetFreepsCard("baruk");
		var shagrat = scn.GetShadowCard("shagrat");
		var spear = scn.GetShadowCard("spear");
		var guard = scn.GetShadowCard("guard");
		var axe = scn.GetShadowCard("axe");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, handaxe, ring);
		scn.MoveCardsToSupportArea(toby, cart, stairs);
		scn.MoveCardsToHand(baruk);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, toby, cart, stairs);
		assertInHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat, guard, chieftain);
		scn.AttachCardsTo(shagrat, spear);
		scn.AttachCardsTo(guard, axe);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(spear, shagrat);
		assertAttachedTo(axe, guard);
		assertAttachedTo(banner, chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		// Seven possessions, the artifact and the condition beside them.
		assertTrue(scn.HasKeyword(shagrat, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(shagrat));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Shagrat may not be wounded in the maneuver phase; the other minions may.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, chieftain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shagrat));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(shagrat));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSixPossessionsShagratTakesAWoundOutsideSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var cart = scn.GetFreepsCard("cart");
		var stairs = scn.GetFreepsCard("stairs");
		var baruk = scn.GetFreepsCard("baruk");
		var shagrat = scn.GetShadowCard("shagrat");
		var spear = scn.GetShadowCard("spear");
		var guard = scn.GetShadowCard("guard");
		var axe = scn.GetShadowCard("axe");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor, handaxe, ring);
		scn.MoveCardsToSupportArea(cart, stairs);
		scn.MoveCardsToHand(baruk);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, cart, stairs);
		assertInHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat, guard, chieftain);
		scn.AttachCardsTo(shagrat, spear);
		scn.AttachCardsTo(guard, axe);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(spear, shagrat);
		assertAttachedTo(axe, guard);
		assertAttachedTo(banner, chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		// Six possessions, with the artifact and the condition beside them.
		assertFalse(scn.HasKeyword(shagrat, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(shagrat));

		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shagrat, guard, chieftain));
		scn.FreepsChooseCard(shagrat);

		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DuringASkirmishPhaseShagratInNoSkirmishTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var cart = scn.GetFreepsCard("cart");
		var stairs = scn.GetFreepsCard("stairs");
		var fell = scn.GetFreepsCard("fell");
		var shagrat = scn.GetShadowCard("shagrat");
		var spear = scn.GetShadowCard("spear");
		var guard = scn.GetShadowCard("guard");
		var axe = scn.GetShadowCard("axe");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.AttachCardsTo(gimli, armor, handaxe, ring);
		scn.MoveCardsToSupportArea(toby, cart, stairs);
		scn.MoveCardsToHand(fell);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertInZone(Zone.SUPPORT, toby, cart, stairs);
		assertInHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat, guard, chieftain);
		scn.AttachCardsTo(shagrat, spear);
		scn.AttachCardsTo(guard, axe);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(spear, shagrat);
		assertAttachedTo(axe, guard);
		assertAttachedTo(banner, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, chieftain}, new PhysicalCardImpl[]{gimli, shagrat},
				new PhysicalCardImpl[]{frodo, guard});

		scn.FreepsResolveSkirmish(gandalf);
		// Seven possessions: Shagrat is fierce and waits for Gimli's skirmish, in none now.
		assertTrue(scn.HasKeyword(shagrat, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(shagrat));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();

		// During a skirmish phase he may be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shagrat, chieftain, guard));
		scn.FreepsChooseCard(shagrat);

		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
